package net.demilich.metastone.game.cards.desc;

import com.google.gson.*;
import net.demilich.metastone.game.cards.group.Group;
import net.demilich.metastone.game.cards.group.GroupArg;
import net.demilich.metastone.game.cards.group.GroupDesc;

import java.lang.reflect.Type;
import java.util.Map;

public class GroupDeserializer implements JsonDeserializer<GroupDesc> {

	@SuppressWarnings("unchecked")
	@Override
	public GroupDesc deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if (!(json instanceof JsonObject)) {
			throw new JsonParseException("GroupDesc parser expected an JsonObject but found " + json + " instead");
		}
		JsonObject jsonData = (JsonObject) json;
		String groupClassName = Group.class.getPackage().getName() + "." + jsonData.get("class").getAsString();
		Class<? extends Group> groupClass;
		try {
			groupClass = (Class<? extends Group>) Class.forName(groupClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new JsonParseException("GroupDesc parser encountered an invalid group class: " + groupClassName);
		}
		Map<GroupArg, Object> groupArgs = GroupDesc.build(groupClass);
		parseArgument(GroupArg.CARDS, jsonData, groupArgs, ParseValueType.STRING_ARRAY);
		return new GroupDesc(groupArgs);
	}

	private void parseArgument(GroupArg groupArg, JsonObject jsonData, Map<GroupArg, Object> groupArgs, ParseValueType valueType) {
		String argName = ParseUtils.toCamelCase(groupArg.toString());
		if (!jsonData.has(argName)) {
			return;
		}
		Object value = ParseUtils.parse(argName, jsonData, valueType);
		groupArgs.put(groupArg, value);
	}

}
