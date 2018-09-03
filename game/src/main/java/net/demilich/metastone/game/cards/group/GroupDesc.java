package net.demilich.metastone.game.cards.group;

import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.SourceDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.Map;

public class GroupDesc extends Desc<GroupArg> {

	public GroupDesc(Map<GroupArg, Object> arguments) {
		super(arguments);
	}

	public static Map<GroupArg, Object> build(Class<? extends Group> groupClass) {
		final Map<GroupArg, Object> arguments = new EnumMap<>(GroupArg.class);
		arguments.put(GroupArg.CLASS, groupClass);
		return arguments;
	}

	@Override
	public GroupDesc clone() {
		GroupDesc clone = new GroupDesc(build(getGroupClass()));
		for (GroupArg groupArg : arguments.keySet()) {
			Object value = arguments.get(groupArg);
			if (value instanceof CustomCloneable) {
				CustomCloneable cloneable = (CustomCloneable) value;
				clone.arguments.put(groupArg, cloneable.clone());
			} else {
				clone.arguments.put(groupArg, value);
			}
		}
		return clone;
	}

	public Group create() {
		Class<? extends Group> groupClass = getGroupClass();
		try {
			return groupClass.getConstructor(GroupDesc.class).newInstance(this);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Group> getGroupClass() {
		return (Class<? extends Group>) arguments.get(GroupArg.CLASS);
	}

	@Override
	public String toString() {
		String result = "[GroupDesc arguments= {\n";
		for (GroupArg groupArg : arguments.keySet()) {
			result += "\t" + groupArg + ": " + arguments.get(groupArg) + "\n";
		}
		result += "}";
		return result;
	}

}
