package net.demilich.metastone.game.entities.weapons;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Attribute;
import net.demilich.metastone.game.entities.EntityType;

public class Weapon extends Actor {

	private boolean active;

	public Weapon(Card sourceCard) {
		super(sourceCard);
	}

	@Override
	public Weapon clone() {
		return (Weapon) super.clone();
	}

	public int getBaseDurability() {
		return getBaseAttributeValue(Attribute.MAX_HP);
	}

	public int getDurability() {
		return getBaseAttributeValue(Attribute.HP);
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.WEAPON;
	}

	public int getMaxDurability() {
		return getAttributeValue(Attribute.MAX_HP) + getAttributeValue(Attribute.HP_BONUS);
	}

	@Deprecated
	public int getWeaponDamage() {
		return Math.max(0, getAttributeValue(Attribute.ATTACK));
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		String result = "[" + getEntityType() + " '" + getName() + "'id:" + getId() + " ";
		result += getBaseAttack() + "/" + getDurability();
		String prefix = " ";
		for (Attribute tag : getAttributes().keySet()) {
			if (displayGameTag(tag)) {
				result += prefix + tag;
				prefix = ", ";
			}
		}
		result += " hashCode: " + hashCode();
		result += "]";
		return result;
	}

}
