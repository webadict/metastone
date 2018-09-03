package net.demilich.metastone.game.cards.group;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public abstract class Group {

	protected GroupDesc desc;

	public Group(GroupDesc desc) {
		this.desc = desc;
	}

	public abstract Entity[] getGroup(GameContext context);

	@Override
	public String toString() {
		return "[GROUP " + getClass().getSimpleName() + "]";
	}

}
