package net.demilich.metastone.game.cards.desc.multiplier;

import com.google.gson.*;
import net.demilich.metastone.game.cards.desc.ParseUtils;
import net.demilich.metastone.game.cards.desc.ParseValueType;

import java.lang.reflect.Type;
import java.util.Map;

public class MultiplierDeserializer implements JsonDeserializer<MultiplierDesc> {

	@SuppressWarnings("unchecked")
	@Override
	public MultiplierDesc deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		if (!(json instanceof JsonObject)) {
			throw new JsonParseException("ManaModifier parser expected an JsonObject but found " + json + " instead");
		}
		JsonObject jsonData = (JsonObject) json;
		String multiplierClassName = Multiplier.class.getPackage().getName() + "." + jsonData.get("class").getAsString();
		Class<? extends Multiplier> multiplierClass;
		try {
            multiplierClass = (Class<? extends Multiplier>) Class.forName(multiplierClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new JsonParseException("Multiplier parser encountered an invalid class: " + multiplierClassName);
		}
		Map<MultiplierArg, Object> arguments = MultiplierDesc.build(multiplierClass);
        parseArgument(MultiplierArg.CARD_TYPE, jsonData, arguments, ParseValueType.CARD_TYPE);
        parseArgument(MultiplierArg.CARD_TYPES, jsonData, arguments, ParseValueType.CARD_TYPE_ARRAY);
        parseArgument(MultiplierArg.VALUE, jsonData, arguments, ParseValueType.INTEGER);

		return new MultiplierDesc(arguments);
	}

	private void parseArgument(MultiplierArg arg, JsonObject jsonData, Map<MultiplierArg, Object> arguments,
			ParseValueType valueType) {
		String argName = ParseUtils.toCamelCase(arg.toString());
		if (!jsonData.has(argName)) {
			return;
		}
		Object value = ParseUtils.parse(argName, jsonData, valueType);
		arguments.put(arg, value);
	}

}
