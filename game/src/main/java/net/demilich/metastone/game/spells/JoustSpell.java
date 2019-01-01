package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.JoustEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class JoustSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        CardType cardType = (CardType) desc.get(SpellArg.CARD_TYPE, CardType.MINION);
        boolean bothPlayers = !desc.getBool(SpellArg.EXCLUSIVE);
		JoustEvent joustEvent = context.getLogic().joust(player, cardType, bothPlayers);
        Card spellOutput = joustEvent.getOwnCard();
		if (!joustEvent.isWon()) {
			SpellDesc spell1 = (SpellDesc) desc.get(SpellArg.SPELL_1);
			if (spell1 != null) {
				SpellUtils.castChildSpell(context, player, spell1, source, target, spellOutput);
			}
			
			return;
		}
		
		SpellDesc spell2 = (SpellDesc) desc.get(SpellArg.SPELL_2);
		if (spell2 != null) {
			SpellUtils.castChildSpell(context, player, spell2, source, target, spellOutput);
			return;
		}

		SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
		SpellUtils.castChildSpell(context, player, spell, source, target, spellOutput);
	}

}
