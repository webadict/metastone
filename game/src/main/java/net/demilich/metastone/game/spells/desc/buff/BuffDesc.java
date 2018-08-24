package net.demilich.metastone.game.spells.desc.buff;

import net.demilich.metastone.game.cards.buff.Buff;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.logic.CustomCloneable;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.Map;

public class BuffDesc extends Desc<BuffArg> {

	public static Map<BuffArg, Object> build(Class<? extends Buff> buffClass) {
		final Map<BuffArg, Object> arguments = new EnumMap<>(BuffArg.class);
		arguments.put(BuffArg.CLASS, buffClass);
		return arguments;
	}

	public BuffDesc(Map<BuffArg, Object> arguments) {
		super(arguments);
	}
	
	public BuffDesc addArg(BuffArg buffArg, Object value) {
		BuffDesc clone = clone();
		clone.arguments.put(buffArg, value);
		return clone;
	}
	
	public BuffDesc removeArg(BuffArg buffArg) {
		BuffDesc clone = clone();
		clone.arguments.remove(buffArg);
		return clone;
	}
	
	@Override
	public BuffDesc clone() {
		BuffDesc clone = new BuffDesc(build(getBuffClass()));
		for (BuffArg buffArg : arguments.keySet()) {
			Object value = arguments.get(buffArg);
			if (value instanceof CustomCloneable) {
				CustomCloneable cloneable = (CustomCloneable) value;
				clone.arguments.put(buffArg, cloneable.clone());
			} else {
				clone.arguments.put(buffArg, value);
			}
		}
		return clone;
	}

	public Buff create() {
		Class<? extends Buff> buffClass = getBuffClass();
		try {
			return buffClass.getConstructor(BuffDesc.class).newInstance(this);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Buff> getBuffClass() {
		return (Class<? extends Buff>) get(BuffArg.CLASS);
	}

	@Override
	public String toString() {
		String result = "[BuffDesc arguments= {\n";
		for (BuffArg buffArg : arguments.keySet()) {
			result += "\t" + buffArg + ": " + arguments.get(buffArg) + "\n";
		}
		result += "}";
		return result;
	}

}
