package net.demilich.metastone.game.entities;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.demilich.metastone.game.cards.enchantment.Enchantment;
import net.demilich.metastone.game.entities.minions.Tribe;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.IdFactory;

public abstract class Entity extends CustomCloneable {

	private String name;
	protected AttributeMap attributes;
	private int id = IdFactory.UNASSIGNED;
	private int ownerIndex = -1;
	protected List<Enchantment> enchantmentList = new ArrayList<>();

	protected Entity() {
	    super();
	    attributes = new AttributeMap();
    }

	@Override
	public Entity clone() {
		Entity clone = (Entity) super.clone();
		return clone;
	}

	public void addEnchantment(Enchantment enchantment) {
		enchantmentList.add(enchantment);
	}

	@Deprecated
	public Object getAttribute(Attribute attribute) {
		return attributes.get(attribute);
	}

	public AttributeMap getAttributes() {
		return attributes;
	}

	@Deprecated
	public int getAttributeValue(Attribute attribute) {
		return attributes.containsKey(attribute) ? (int) attributes.get(attribute) : 0;
	}

	public int getBaseAttributeValue(Attribute attribute) {
		return attributes.containsKey(attribute) ? (int) attributes.get(attribute) : 0;
	}

	public abstract EntityType getEntityType();

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getOwner() {
		return ownerIndex;
	}

	public Tribe getTribe() {
		return hasAttribute(Attribute.TRIBE) ? (Tribe) getAttribute(Attribute.TRIBE) : Tribe.NONE;
	}

	public EntityReference getReference() {
		return EntityReference.pointTo(this);
	}

	@Deprecated
	public boolean hasAttribute(Attribute attribute) {
		Object value = attributes.get(attribute);
		if (value == null) {
			return false;
		}
		if (value instanceof Integer) {
			return ((int) value) != 0;
		}
		return true;
	}
	public boolean hasBaseAttribute(Attribute attribute) {
		Object value = attributes.get(attribute);
		if (value == null) {
			return false;
		}
		if (value instanceof Integer) {
			return ((int) value) != 0;
		}
		return true;
	}

	public boolean isDestroyed() {
		return hasBaseAttribute(Attribute.DESTROYED);
	}

	public void modifyAttribute(Attribute attribute, int value) {
		if (!attributes.containsKey(attribute)) {
			setAttribute(attribute, 0);
		}
		setAttribute(attribute, getAttributeValue(attribute) + value);
	}
	
	public void modifyHpBonus(int value, int maxHp) {
		if (value > 0) {
			modifyAttribute(Attribute.HP, value);
		}
		if (getAttributeValue(Attribute.HP) > maxHp) {
			setAttribute(Attribute.HP, maxHp);
		}
	}

	@Deprecated
	public void modifyAuraHpBonus(int value, int maxHp) {
		modifyAttribute(Attribute.AURA_HP_BONUS, value);
		modifyHpBonus(value, maxHp);
	}

	public void removeAttribute(Attribute attribute) {
		attributes.remove(attribute);
	}

	@Deprecated
	public void setAttribute(Attribute attribute) {
		attributes.put(attribute, 1);
	}

	@Deprecated
	public void setAttribute(Attribute attribute, int value) {
		attributes.put(attribute, value);
	}

	@Deprecated
	public void setAttribute(Attribute attribute, Object value) {
		attributes.put(attribute, value);
	}

	public void setBaseAttribute(Attribute attribute) {
		attributes.put(attribute, 1);
	}

	public void setBaseAttribute(Attribute attribute, int value) {
		attributes.put(attribute, value);
	}

	public void setBaseAttribute(Attribute attribute, Object value) {
		attributes.put(attribute, value);
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOwner(int ownerIndex) {
		this.ownerIndex = ownerIndex;
	}

	public void setTribe(Tribe tribe) {
		setAttribute(Attribute.TRIBE, tribe);
	}

}
