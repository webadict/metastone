package net.demilich.metastone.game.entities;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.Buff;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.IdFactory;

public abstract class Entity extends CustomCloneable {

	private String name;
	protected List<Buff> buffs = new ArrayList<Buff>();
	private int id = IdFactory.UNASSIGNED;
	private int ownerIndex = -1;

	public void addBuff(Buff buff) {
		buffs.add(buff);
	}

	public void addBuff(Attribute attribute, Object value, AlgebraicOperation operation) {
		buffs.add(new Buff(attribute, value, operation));
	}
	
	@Override
	public Entity clone() {
		Entity clone = (Entity) super.clone();
		clone.name = name;
		clone.id = id;
		clone.ownerIndex = ownerIndex;
		clone.buffs = new ArrayList<Buff>();
		for (Buff buff : buffs) {
			clone.addBuff(buff.clone());
		}
		return clone;
	}

	public Object getAttribute(Attribute attribute) {
		Object obj = null;
		for (Buff buff : buffs) {
			if (buff == null) {
				continue;
			}
			if (obj instanceof List) {
				
			} else {
				obj = buff.evaluate(attribute, obj);
			}
		}
		return obj;
	}

	public Object getAttribute(Attribute attribute, Object obj) {
		for (Buff buff : buffs) {
			if (obj instanceof List) {
				
			} else {
				obj = buff.evaluate(attribute, obj);
			}
		}
		return obj;
	}

	public Map<Attribute, Object> getAttributes() {
		Map<Attribute, Object> attributes = new EnumMap<Attribute, Object>(Attribute.class);
		for (Attribute attribute : Attribute.values()) {
			if (hasAttribute(attribute)) {
				attributes.put(attribute, getAttribute(attribute));
			}
		}
		return attributes;
	}

	public int getAttributeValue(Attribute attribute) {
		if (hasAttribute(attribute)) {
			return (int) getAttribute(attribute);
		}
		return 0;
	}

	public int getAttributeValue(Attribute attribute, int value) {
		if (hasAttribute(attribute)) {
			return (int) getAttribute(attribute, value);
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
		Object obj = getAttribute(attribute);
		if (obj == null) {
			return false;
		}
		if (obj instanceof Integer) {
			return ((int) obj) != 0;
		}
		return true;
	}

	public boolean isDestroyed() {
		return hasAttribute(Attribute.DESTROYED);
	}

	public void modifyAttribute(Attribute attribute, int value) {
		buffs.add(new Buff(attribute, value, AlgebraicOperation.ADD));
	}
	
	public void modifyHpBonus(int value) {
		modifyAttribute(Attribute.HP_BONUS, value);
	}

	public void removeAttribute(Attribute attribute) {
		List<Buff> buffsCopy = new ArrayList<Buff>();
		buffsCopy.addAll(buffs);
		for (Buff buff : buffsCopy) {
			if (buff.isAttribute(attribute)) {
				buffs.remove(buff);
			}
		}
	}

	public void setAttributes(Map<Attribute, Object> attributes) {
		for (Attribute attribute : attributes.keySet()) {
			setAttribute(attribute, attributes.get(attribute));
		}
	}

	public void setAttribute(Attribute attribute) {
		buffs.add(new Buff(attribute, 1, AlgebraicOperation.SET));
	}

	public void setAttribute(Attribute attribute, Object value) {
		buffs.add(new Buff(attribute, value, AlgebraicOperation.SET));
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

}
