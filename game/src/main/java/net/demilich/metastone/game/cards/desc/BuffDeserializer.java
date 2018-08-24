package net.demilich.metastone.game.cards.desc;

import com.google.gson.*;
import net.demilich.metastone.game.cards.buff.Buff;
import net.demilich.metastone.game.spells.desc.buff.BuffArg;
import net.demilich.metastone.game.spells.desc.buff.BuffDesc;

import java.lang.reflect.Type;
import java.util.Map;

public class BuffDeserializer implements JsonDeserializer<BuffDesc> {
	@SuppressWarnings("unchecked")
	@Override
	public BuffDesc deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		if (!(json instanceof JsonObject)) {
			throw new JsonParseException("Buff parser expected an JsonObject but found " + json + " instead");
		}
		JsonObject jsonData = (JsonObject) json;
		String BuffClassName = Buff.class.getPackage().getName() + "." + jsonData.get("class").getAsString();
		Class<? extends Buff> BuffClass;
		try {
			BuffClass = (Class<? extends Buff>) Class.forName(BuffClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new JsonParseException("Buff parser encountered an invalid class: " + BuffClassName);
		}
		Map<BuffArg, Object> arguments = BuffDesc.build(BuffClass);

		//parseArgument(BuffArg.VALUE, jsonData, arguments, ParseValueType.INTEGER);

		return new BuffDesc(arguments);
	}

	private void parseArgument(BuffArg arg, JsonObject jsonData, Map<BuffArg, Object> arguments, ParseValueType valueType) {
		String argName = ParseUtils.toCamelCase(arg.toString());
		if (!jsonData.has(argName)) {
			return;
		}
		Object value = ParseUtils.parse(argName, jsonData, valueType);
		arguments.put(arg, value);
	}

}
