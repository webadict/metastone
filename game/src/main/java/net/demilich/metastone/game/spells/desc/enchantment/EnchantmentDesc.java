package net.demilich.metastone.game.spells.desc.enchantment;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.enchantment.Enchantment;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;

public class EnchantmentDesc extends Desc<EnchantmentArg> {

	public static Map<EnchantmentArg, Object> build(Class<? extends Enchantment> enchantmentClass) {
		final Map<EnchantmentArg, Object> arguments = new EnumMap<>(EnchantmentArg.class);
		arguments.put(EnchantmentArg.CLASS, enchantmentClass);
		return arguments;
	}

	public static Map<EnchantmentArg, Object> build(
			Class<? extends Enchantment> enchantmentClass,
			Attribute attribute,
			Object value,
			AlgebraicOperation operation,
			Entity... targets
	) {
		final Map<EnchantmentArg, Object> arguments = new EnumMap<>(EnchantmentArg.class);
		arguments.put(EnchantmentArg.CLASS, enchantmentClass);
		arguments.put(EnchantmentArg.ATTRIBUTE, attribute);
		arguments.put(EnchantmentArg.VALUE, value);
		arguments.put(EnchantmentArg.OPERATION, operation);
		List<Integer> target_ids = new ArrayList<>();
		for (Entity entity : targets) {
			target_ids.add(entity.getId());
		}
		arguments.put(EnchantmentArg.CARD_IDS, target_ids);
		return arguments;
	}

	public EnchantmentDesc(Map<EnchantmentArg, Object> arguments) {
		super(arguments);
	}

	public EnchantmentDesc addArg(EnchantmentArg cardCostModififerArg, Object value) {
		EnchantmentDesc clone = clone();
		clone.arguments.put(cardCostModififerArg, value);
		return clone;
	}

	public EnchantmentDesc removeArg(EnchantmentArg cardCostModififerArg) {
		EnchantmentDesc clone = clone();
		clone.arguments.remove(cardCostModififerArg);
		return clone;
	}

	@Override
	public EnchantmentDesc clone() {
		EnchantmentDesc clone = new EnchantmentDesc(build(getManaModifierClass()));
		for (EnchantmentArg cardCostModififerArg : arguments.keySet()) {
			Object value = arguments.get(cardCostModififerArg);
			if (value instanceof CustomCloneable) {
				CustomCloneable cloneable = (CustomCloneable) value;
				clone.arguments.put(cardCostModififerArg, cloneable.clone());
			} else {
				clone.arguments.put(cardCostModififerArg, value);
			}
		}
		return clone;
	}

	public Enchantment create() {
		Class<? extends Enchantment> manaModifierClass = getManaModifierClass();
		try {
			return manaModifierClass.getConstructor(EnchantmentDesc.class).newInstance(this);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Enchantment> getManaModifierClass() {
		return (Class<? extends Enchantment>) get(EnchantmentArg.CLASS);
	}

	@Override
	public String toString() {
		String result = "[EnchantmentDesc arguments= {\n";
		for (EnchantmentArg cardCostModififerArg : arguments.keySet()) {
			result += "\t" + cardCostModififerArg + ": " + arguments.get(cardCostModififerArg) + "\n";
		}
		result += "}";
		return result;
	}

}
