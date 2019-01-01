package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.minions.Tribe;

public class MinionCardDesc extends SummonCardDesc {

	public int baseAttack;
	public int baseHp;
	public Tribe tribe;

	@Override
	public Card createInstance() {
		return new MinionCard(this);
	}

}
