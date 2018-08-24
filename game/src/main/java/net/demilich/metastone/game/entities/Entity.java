package net.demilich.metastone.game.entities;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.buff.Buff;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.IdFactory;

public abstract class Entity extends CustomCloneable {

	private String name;
	protected Map<Attribute, Object> attributes = new EnumMap<Attribute, Object>(Attribute.class);
	protected Map<Attribute, Object> cachedAttributes = new EnumMap<Attribute, Object>(Attribute.class);
	protected List<Buff> buffList = new ArrayList<>();
	private int id = IdFactory.UNASSIGNED;
	private int ownerIndex = -1;

	@Override
	public Entity clone() {
		Entity clone = (Entity) super.clone();
		clone.attributes = new EnumMap<>(getAttributes());
		clone.cachedAttributes = new EnumMap<>(getAttributes());
		return clone;
	}

	public void addBuff(Buff buff) {
		buffList.add(buff);
	}

	public void clearBuffs() {
		buffList.clear();
	}

	public Object getAttribute(Attribute attribute) {
		return attributes.get(attribute);
	}

	public Map<Attribute, Object> getAttributes() {
		return attributes;
	}

	public int getBaseAttributeValue(Attribute attribute) {
		return attributes.containsKey(attribute) ? (int) attributes.get(attribute) : 0;
	}

	public int getCachedAttributeValue(Attribute attribute) {
		return cachedAttributes.containsKey(attribute) ? (int) cachedAttributes.get(attribute) : 0;
	}

	public int getAttributeValue(GameContext context, Attribute attribute) {
		int value = getBaseAttributeValue(attribute);
		for (Buff buff : buffList) {
			if (buff.isAttribute(attribute)) {
				value = buff.getValue(context, this, this, value);
			}
		}
		return value;
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

	public EntityReference getReference() {
		return EntityReference.pointTo(this);
	}

	public boolean hasAttribute(Attribute attribute) {
		Object value = getAttribute(attribute);
		if (value == null) {
			return false;
		}
		if (value instanceof Integer) {
			return ((int) value) != 0;
		}
		return true;
	}

	public boolean isDestroyed() {
		return hasAttribute(Attribute.DESTROYED);
	}

	@Deprecated
	public void modifyAttribute(Attribute attribute, int value) {
		if (!attributes.containsKey(attribute)) {
			setAttribute(attribute, 0);
		}
		setAttribute(attribute, getBaseAttributeValue(attribute) + value);
	}

	public void modifyHpBonus(GameContext context, Player player, int value) {
	}

	public void removeAttribute(Attribute attribute) {
		attributes.remove(attribute);
	}

	public void setAttribute(Attribute attribute) {
		attributes.put(attribute, 1);
	}

	public void setAttribute(Attribute attribute, int value) {
		attributes.put(attribute, value);
	}

	public void setAttribute(Attribute attribute, Object value) {
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

	public void updateAttributeCache(GameContext context) {
		cachedAttributes.putAll(attributes);
		for (Buff buff : buffList) {
			Attribute attribute = buff.getAttribute();
			int value = getCachedAttributeValue(attribute);
			cachedAttributes.put(attribute, buff.getValue(context, this, this, value));
		}
	}

}
