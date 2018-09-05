package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.group.Group;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Summon;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

public class GroupOnBoardCondition extends Condition {

	public GroupOnBoardCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		Group group = (Group) desc.get(ConditionArg.GROUP);

		for (Entity entity : group.getGroup(context)) {
			boolean check = false;
			for (Summon summon : player.getSummons()) {
				if (summon.getId() == entity.getId()) {
					check = true;
				}
			}
			if (!check) {
				return false;
			}
		}

		return true;
	}

}
