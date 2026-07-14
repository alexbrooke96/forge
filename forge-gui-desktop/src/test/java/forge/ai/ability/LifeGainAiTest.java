package forge.ai.ability;

import forge.ai.AITest;
import forge.ai.SpellApiToAi;
import forge.game.Game;
import forge.game.ability.ApiType;
import forge.game.card.Card;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class LifeGainAiTest extends AITest {

    @Test
    public void testDoesNotSacLandsForMarginalLifeGain() {
        Game game = initAndCreateGame();
        Player ai = game.getPlayers().get(1);
        Player opponent = game.getPlayers().get(0);

        Card baloth = addCard("Baloth Prime", ai);
        addCards("Forest", 8, ai);
        addCardToZone("Serra Angel", ai, ZoneType.Hand);

        game.getPhaseHandler().devModeSet(PhaseType.END_OF_TURN, opponent);
        game.getAction().checkStateEffects(true);

        AssertJUnit.assertFalse("AI at a healthy life total should not sacrifice lands for 2 life",
                canPlayGainLifeAbility(ai, baloth));
    }

    @Test
    public void testSacsLandForLifeWhenLifeIsLow() {
        Game game = initAndCreateGame();
        Player ai = game.getPlayers().get(1);
        Player opponent = game.getPlayers().get(0);

        Card baloth = addCard("Baloth Prime", ai);
        addCards("Forest", 8, ai);
        addCardToZone("Serra Angel", ai, ZoneType.Hand);
        ai.setLife(7, null);

        game.getPhaseHandler().devModeSet(PhaseType.END_OF_TURN, opponent);
        game.getAction().checkStateEffects(true);

        AssertJUnit.assertTrue("AI at a low life total should be willing to sacrifice a land for 2 life",
                canPlayGainLifeAbility(ai, baloth));
    }

    private boolean canPlayGainLifeAbility(Player ai, Card source) {
        for (SpellAbility sa : source.getSpellAbilities()) {
            if (sa.getApi() == ApiType.GainLife) {
                sa.setActivatingPlayer(ai);
                return SpellApiToAi.Converter.get(sa).canPlayWithSubs(ai, sa).willingToPlay();
            }
        }
        AssertJUnit.fail("No GainLife ability found on " + source.getName());
        return false;
    }
}
