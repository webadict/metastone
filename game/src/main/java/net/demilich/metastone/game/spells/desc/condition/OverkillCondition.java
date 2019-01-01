package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

public class OverkillCondition extends Condition {

	public OverkillCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		EntityReference entityReference = (EntityReference) desc.get(ConditionArg.TARGET);
		Entity entity = null;
		if (source.getOwner() != context.getActivePlayer().getId()) {
		    return false;
        }
		if (entityReference == null) {
			entity = target;
		} else {
			List<Entity> entities = context.resolveTarget(player, source, entityReference);
			if (entities == null || entities.isEmpty()) {
				return false;
			}
			entity = entities.get(0);
		}
		if (entity instanceof Actor) {
            return ((Actor) entity).getHp() < 0;
        }
		return false;
	}

}
