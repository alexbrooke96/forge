package forge.ai;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import forge.game.card.Card;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class AiCache {

    // stores result + args as vector
    private static Multimap<String, List<Object>> dataMap = Multimaps.synchronizedMultimap(HashMultimap.create());

    // per-card creature evaluation memo (Card has identity equals, so copies never collide);
    // shares the clear() lifecycle with the rest of the cache
    private static final Map<Card, int[]> creatureEvalCache = new ConcurrentHashMap<>();

    public static Map<Card, int[]> getCreatureEvalCache() {
        return creatureEvalCache;
    }

    public static boolean identity(Object a, Object b) {
        return a == b;
    }

    // the cache is global for calculations that can be shared between games
    // but that also means unwanted collisions need to be considered:
    // for that you can pass Functions that compare the args
    public static <T> T getCached(String key, Supplier<T> func, List<BiFunction<Object, Object, Boolean>> argsCheck, Object... args) {
        // TODO would like a good strategy to derive default key, but there's no clean way to obtain the method name
        for (List<Object> cached : dataMap.get(key)) {
            boolean hit = true;
            for (int i = 0; i < args.length; i++) {
                BiFunction<Object, Object, Boolean> checker = argsCheck == null ? Object::equals : argsCheck.get(i);
                if (!checker.apply(args[i], cached.get(i + 1))) {
                    hit = false;
                    break;
                }
            }
            if (hit) {
                return (T) cached.get(0);
            }
        }
        T result = func.get();
        List<Object> cached = Lists.newArrayList(result);
        cached.addAll(Arrays.asList(args));
        dataMap.put(key, cached);
        return result;
    }

    public static void clear() {
        dataMap.clear();
    }

    // separate lifecycle from clear(): per-card guard fields already invalidate entries on
    // counter/damage/tap/P-T changes, so this only needs to go when zone changes or continuous
    // effects (re)apply - tracked by the caller via game timestamp and phase
    public static void clearCreatureEvals() {
        creatureEvalCache.clear();
    }

}
