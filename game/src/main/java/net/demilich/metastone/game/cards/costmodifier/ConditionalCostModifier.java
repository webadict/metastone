package net.demilich.metastone.game.cards.costmodifier;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierArg;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;

public class ConditionalCostModifier extends CardCostModifier {

    private Condition condition;

	public ConditionalCostModifier(CardCostModifierDesc desc) {
		super(desc);
		condition = (Condition) desc.get(CardCostModifierArg.CONDITION);
	}

	@Override
    public int process(GameContext context, Player player, Card card, int currentManaCost) {
	    if (condition.isFulfilled(context, player, card, null)) {
	        return super.process(context, player, card, currentManaCost);
        }
        return currentManaCost;
    }

}
