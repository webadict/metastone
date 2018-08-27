package net.demilich.metastone.game.entities.minions;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.EntityType;

public class Minion extends Summon {

	public Minion(MinionCard sourceCard) {
		super(sourceCard);
		Race race = getRace();
		setRace(race);
	}

	@Override
	public Minion clone() {
		Minion clone = (Minion) super.clone();
		return clone;
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.MINION;
	}

	protected void setBaseStats(int baseAttack, int baseHp) {
		setBaseAttack(baseAttack);
		setBaseHp(baseHp);
	}
	
}
