package net.demilich.metastone.game.cards.desc.multiplier;

import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.logic.CustomCloneable;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.Map;

public class MultiplierDesc extends Desc<MultiplierArg> {

	public static Map<MultiplierArg, Object> build(Class<? extends Multiplier> manaModifierClass) {
		final Map<MultiplierArg, Object> arguments = new EnumMap<>(MultiplierArg.class);
		arguments.put(MultiplierArg.CLASS, manaModifierClass);
		return arguments;
	}

	public MultiplierDesc(Map<MultiplierArg, Object> arguments) {
		super(arguments);
	}

	public MultiplierDesc addArg(MultiplierArg multiplierArg, Object value) {
		MultiplierDesc clone = clone();
		clone.arguments.put(multiplierArg, value);
		return clone;
	}

	public MultiplierDesc removeArg(MultiplierArg multiplierArg) {
		MultiplierDesc clone = clone();
		clone.arguments.remove(multiplierArg);
		return clone;
	}

	@Override
	public MultiplierDesc clone() {
		MultiplierDesc clone = new MultiplierDesc(build(getMultiplierClass()));
		for (MultiplierArg multiplierArg : arguments.keySet()) {
			Object value = arguments.get(multiplierArg);
			if (value instanceof CustomCloneable) {
				CustomCloneable cloneable = (CustomCloneable) value;
				clone.arguments.put(multiplierArg, cloneable.clone());
			} else {
				clone.arguments.put(multiplierArg, value);
			}
		}
		return clone;
	}

	public Multiplier create() {
		Class<? extends Multiplier> multiplierClass = getMultiplierClass();
		try {
			return multiplierClass.getConstructor(MultiplierDesc.class).newInstance(this);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Multiplier> getMultiplierClass() {
		return (Class<? extends Multiplier>) get(MultiplierArg.CLASS);
	}

	@Override
	public String toString() {
		String result = "[CardCostModifierDesc arguments= {\n";
		for (MultiplierArg multiplierArg : arguments.keySet()) {
			result += "\t" + multiplierArg + ": " + arguments.get(multiplierArg) + "\n";
		}
		result += "}";
		return result;
	}

}
