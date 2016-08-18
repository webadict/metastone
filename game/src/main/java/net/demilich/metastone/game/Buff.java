package net.demilich.metastone.game;

import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.targeting.EntityReference;

public class Buff {
	
	private EntityReference hostReference;
	private final Object value;
	private final Attribute attribute;
	private final AlgebraicOperation operation;
	
	public Buff(Attribute attribute, Object value, AlgebraicOperation operation) {
		this.attribute = attribute;
		this.value = value;
		this.operation = operation;
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
