package net.demilich.metastone.game.cards.group;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;

public abstract class Group {

	protected GroupDesc desc;

	public Group(GroupDesc desc) {
		this.desc = desc;
	}

	public abstract Entity[] getGroup(GameContext context);

	public CardType getType() {
		return (CardType) desc.get(GroupArg.TYPE);
	}

	@Override
	public String toString() {
		return "[GROUP " + getClass().getSimpleName() + "]";
	}

}
