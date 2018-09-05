package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.SummonCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Summon;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.CardLocation;

public class SummonAndDoSomethingSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		for (Card card : SpellUtils.getCards(context, player, desc, source, target)) {
			for (int i = 0; i < count; i++) {
				int boardPosition = SpellUtils.getBoardPosition(context, player, desc, source, i);
				SummonCard summonCard = (SummonCard) card.clone();

				Summon summon = context.getLogic().newSummon(player.getId(), summonCard.summon(), null, boardPosition, false);
				SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
				if (spell != null) {
					SpellUtils.castChildSpell(context, player, spell, source, summon);
				}
			}
		}
	}
}
