package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.cards.desc.GroupCardDesc;
import net.demilich.metastone.game.cards.group.Group;
import net.demilich.metastone.game.cards.group.GroupDesc;

public class GroupCard extends Card {

	private GroupDesc group;

	public GroupCard(GroupCardDesc desc) {
		super(desc);
		setGroup(desc.group);
	}

	@Override
	public Card clone() {
		GroupCard clone = (GroupCard) super.clone();
		return clone;
	}

	public Group getGroup() {
		return group.create();
	}

	public void setGroup(GroupDesc group) {
		this.group = group;
	}

	@Override
	public PlayCardAction play() {
		throw new UnsupportedOperationException("The method .play() should not be called for GroupCard");
	}

}
