package net.demilich.metastone.game.cards.desc.base;

import com.google.gson.*;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.ParseUtils;
import net.demilich.metastone.game.cards.desc.ParseValueType;
import net.demilich.metastone.game.logic.CustomCloneable;

import java.lang.reflect.Type;
import java.util.Map;

abstract public class Deserializer<T extends CustomCloneable, E extends Enum> implements JsonDeserializer<Desc> {

    private Class<? extends T> clazz;

    public Deserializer(Class<? extends T> clazz) {
        this.clazz = clazz;
    }

	@SuppressWarnings("unchecked")
	@Override
	public Desc deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		if (!(json instanceof JsonObject)) {
			throw new JsonParseException(getBaseClass().toGenericString() + " parser expected an JsonObject but found " + json + " instead");
		}
		JsonObject jsonData = (JsonObject) json;

		String className = getBaseClass().getPackage().getName() + "." + jsonData.get("class").getAsString();
		Map<E, Object> arguments = createMap(getExtendedClass(className), jsonData);

		return new Desc(arguments);
	}

	protected Class<? extends T> getBaseClass() {
        return clazz;
    }

	abstract protected Map<E, Object> createMap(Class<? extends T> customClass, JsonObject jsonData);

	protected void parseArgument(E arg, JsonObject jsonData, Map<E, Object> arguments,
			ParseValueType valueType) {
		String argName = ParseUtils.toCamelCase(arg.toString());
		if (!jsonData.has(argName)) {
			return;
		}
		Object value = ParseUtils.parse(argName, jsonData, valueType);
		arguments.put(arg, value);
	}

	protected Class<? extends T> getExtendedClass(String className) {
        Class<? extends T> tClass;
        try {
            tClass = (Class<? extends T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new JsonParseException(getBaseClass().toGenericString() + " parser encountered an invalid class: " + className);
        }
        return tClass;
    }

}
