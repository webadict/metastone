package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.entities.minions.Tribe;

public class HeroCardDesc extends CardDesc {

	public String heroPower;
	public Tribe tribe = Tribe.NONE;

	@Override
	public Card createInstance() {
		return new HeroCard(this);
	}

}
