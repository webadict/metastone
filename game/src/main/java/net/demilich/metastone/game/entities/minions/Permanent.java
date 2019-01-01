package net.demilich.metastone.game.entities.minions;

import net.demilich.metastone.game.cards.PermanentCard;
import net.demilich.metastone.game.entities.EntityType;

public class Permanent extends Summon {

	public Permanent(PermanentCard sourceCard) {
		super(sourceCard);
		setTribe(Tribe.NONE);
	}

	@Override
	public Permanent clone() {
		Permanent clone = (Permanent) super.clone();
        return clone;
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.PERMANENT;
	}
	
}
