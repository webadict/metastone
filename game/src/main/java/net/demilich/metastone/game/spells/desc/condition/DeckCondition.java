package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

public class DeckCondition extends Condition {

	public DeckCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		Condition condition = (Condition) desc.get(ConditionArg.CONDITION);
		for (Card card : player.getDeck()) {
            if (!condition.isFulfilled(context, player, source, card)) {
                return false;
            }
		}
		return true;
	}

}
