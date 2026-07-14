package forge.ai.ability;

import forge.ai.AITest;
import forge.ai.AiAbilityDecision;
import forge.ai.AiPlayDecision;
import forge.ai.SpellApiToAi;
import forge.game.Game;
import forge.game.card.Card;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.spellability.AlternativeCost;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class ChangeZoneAiTest extends AITest {

    @Test
    public void testOverloadsCyclonicRiftIntoBigBoard() {
        Game game = initAndCreateGame();
        Player ai = game.getPlayers().get(1);
        Player opponent = game.getPlayers().get(0);

        Card rift = addCardToZone("Cyclonic Rift", ai, ZoneType.Hand);
        addCards("Island", 8, ai);
        addCard("Grizzly Bears", ai);
        addCards("Serra Angel", 4, opponent);
        addCards("Island", 4, opponent);

        game.getPhaseHandler().devModeSet(PhaseType.MAIN1, ai);
        game.getAction().checkStateEffects(true);

        AssertJUnit.assertTrue("AI should be willing to overload Cyclonic Rift into a big opposing board",
                canPlaySpell(ai, findOverloadSpell(rift)));
        AssertJUnit.assertFalse("AI should hold the single-target mode when the overload is affordable and worth it",
                canPlaySpell(ai, findNormalSpell(rift)));
    }

    @Test
    public void testDoesNotOverloadCyclonicRiftOnSmallBoard() {
        Game game = initAndCreateGame();
        Player ai = game.getPlayers().get(1);
        Player opponent = game.getPlayers().get(0);

        Card rift = addCardToZone("Cyclonic Rift", ai, ZoneType.Hand);
        addCards("Island", 8, ai);
        addCard("Grizzly Bears", ai);
        addCard("Grizzly Bears", opponent);
        addCards("Island", 4, opponent);

        game.getPhaseHandler().devModeSet(PhaseType.MAIN1, ai);
        game.getAction().checkStateEffects(true);

        AssertJUnit.assertFalse("AI should not pay the overload cost to bounce a single small creature",
                canPlaySpell(ai, findOverloadSpell(rift)));
    }

    @Test
    public void testCastsNormalCyclonicRiftWhenOverloadNotAffordable() {
        Game game = initAndCreateGame();
        Player ai = game.getPlayers().get(1);
        Player opponent = game.getPlayers().get(0);

        Card rift = addCardToZone("Cyclonic Rift", ai, ZoneType.Hand);
        addCards("Island", 2, ai);
        addCard("Grizzly Bears", ai);
        addCard("Serra Angel", opponent);
        addCards("Island", 4, opponent);

        game.getPhaseHandler().devModeSet(PhaseType.MAIN1, ai);
        game.getAction().checkStateEffects(true);

        SpellAbility normal = findNormalSpell(rift);
        normal.setActivatingPlayer(ai);
        AiAbilityDecision decision = SpellApiToAi.Converter.get(normal).canPlayWithSubs(ai, normal);
        AssertJUnit.assertNotSame("The overload-hold rule should not block the single-target mode when the overload cost can't be paid",
                AiPlayDecision.AnotherTime, decision.decision());
    }

    private boolean canPlaySpell(Player ai, SpellAbility sa) {
        sa.setActivatingPlayer(ai);
        return SpellApiToAi.Converter.get(sa).canPlayWithSubs(ai, sa).willingToPlay();
    }

    private SpellAbility findOverloadSpell(Card card) {
        for (SpellAbility sa : card.getSpellAbilities()) {
            if (sa.isAlternativeCost(AlternativeCost.Overload)) {
                return sa;
            }
        }
        return null;
    }

    private SpellAbility findNormalSpell(Card card) {
        for (SpellAbility sa : card.getSpellAbilities()) {
            if (!sa.isAlternativeCost(AlternativeCost.Overload)) {
                return sa;
            }
        }
        return null;
    }
}
