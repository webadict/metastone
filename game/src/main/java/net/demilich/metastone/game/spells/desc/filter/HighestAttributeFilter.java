package net.demilich.metastone.game.spells.desc.filter;

import java.util.List;

import net.demilich.metastone.game.entities.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

public class HighestAttributeFilter extends EntityFilter {

	public HighestAttributeFilter(FilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity) {
		Attribute attribute = (Attribute) desc.get(FilterArg.ATTRIBUTE);
		EntityReference targetReference = (EntityReference) desc.get(FilterArg.TARGET);
		List<Entity> entities = context.resolveTarget(player, entity, targetReference);
		int highest = getHighestInList(context, entities, attribute);
		return getAttributeValue(context, entity, attribute) >= highest;
	}

	private static int getAttributeValue(GameContext context, Entity entity, Attribute attribute) {
		if (attribute == Attribute.ATTACK) {
			return context.getLogic().getEntityAttack(entity);
		}
		return entity.getAttributeValue(attribute);
	}

	private static int getHighestInList(GameContext context, List<Entity> entities, Attribute attribute) {
		int highest = Integer.MIN_VALUE;
		for (Entity entity : entities) {
			int attributeValue = getAttributeValue(context, entity, attribute);
			if (attributeValue > highest) {
				highest = attributeValue;
			}
		}
		return highest;
	}

}
