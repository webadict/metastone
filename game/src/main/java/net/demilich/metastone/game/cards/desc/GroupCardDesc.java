package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.GroupCard;
import net.demilich.metastone.game.cards.group.GroupDesc;

public class GroupCardDesc extends CardDesc {

	public GroupDesc group;

	@Override
	public Card createInstance() {
		return new GroupCard(this);
	}

}
