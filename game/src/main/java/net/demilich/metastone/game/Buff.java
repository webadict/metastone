package net.demilich.metastone.game;

import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.targeting.EntityReference;

public class Buff extends CustomCloneable {
	
	private EntityReference hostReference;
	private Object value;
	private Attribute attribute;
	private AlgebraicOperation operation;
	
	public Buff(Attribute attribute, Object value, AlgebraicOperation operation) {
		this.attribute = attribute;
		this.value = value;
		this.operation = operation;
	}
	
	public Buff(Buff buff) {
		hostReference = buff.getHost();
		value = buff.getValue();
		attribute = buff.getAttribute();
		operation = buff.getOperation();
	}

	@Override
	public Buff clone() {
		Buff clone = (Buff) super.clone();
		clone.hostReference = hostReference;
		clone.value = value;
		clone.attribute = attribute;
		clone.operation = operation;
		return clone;
	}

	public Object evaluate(Attribute attribute, Object currentValue) {
		if (attribute == this.attribute) {
			if (currentValue == null) {
				currentValue = 0;
			}
			if (value instanceof Integer) {
				return operation.performOperation((int) currentValue, (int) value);
			} else {
				return value;
			}
		}
		return currentValue;
	}
	
	public Attribute getAttribute() {
		return this.attribute;
	}
	
	public EntityReference getHost() {
		return hostReference;
	}
	
	public AlgebraicOperation getOperation() {
		return operation;
	}
	
	public Object getValue() {
		return value;
	}
	
	public boolean isAttribute(Attribute attribute) {
		return this.attribute == attribute;
	}
	
	public void setHost(EntityReference host) {
		hostReference = host;
	}
}
