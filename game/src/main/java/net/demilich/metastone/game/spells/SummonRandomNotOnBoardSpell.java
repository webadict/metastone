package net.demilich.metastone.game.spells;

import java.util.ArrayList;
import java.util.List;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.SummonCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Summon;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class SummonRandomNotOnBoardSpell extends Spell {

	private static boolean alreadyOnBoard(List<Summon> summons, String id) {
		for (Summon summon : summons) {
			if (summon.getSourceCard().getCardId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card[] cards = SpellUtils.getCards(context, player, desc, source, target);
		List<String> eligibleSummons = new ArrayList<String>();
		for (Card card : cards) {
			if (card instanceof SummonCard && !alreadyOnBoard(player.getSummons(), card.getCardId())) {
				eligibleSummons.add(card.getCardId());
			}
		}
		if (eligibleSummons.isEmpty()) {
			return;
		}

		String randomMinionId = eligibleSummons.get(context.getLogic().random(eligibleSummons.size()));
		SummonCard randomSummonCard = (SummonCard) context.getCardById(randomMinionId);
		context.getLogic().summon(player.getId(), randomSummonCard.summon());
	}

}
