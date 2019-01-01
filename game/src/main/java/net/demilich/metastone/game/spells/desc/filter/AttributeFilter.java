package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.entities.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;

public class AttributeFilter extends EntityFilter {

	public AttributeFilter(FilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity) {
		Attribute attribute = (Attribute) desc.get(FilterArg.ATTRIBUTE);
		Operation operation = (Operation) desc.get(FilterArg.OPERATION);
		if (operation == Operation.HAS) {
			return context.getLogic().hasEntityAttribute(entity, attribute);
		}

		int targetValue = desc.getInt(FilterArg.VALUE);
		
		int actualValue = -1;
		if (attribute == Attribute.ATTACK) {
			context.getLogic().getEntityAttack(entity);
		} else {
			actualValue = entity.getAttributeValue(attribute);
		}

		return SpellUtils.evaluateOperation(operation, actualValue, targetValue);
	}

}
