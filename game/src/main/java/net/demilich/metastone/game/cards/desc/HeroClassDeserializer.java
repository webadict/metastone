package net.demilich.metastone.game.cards.desc;

import com.google.gson.*;
import net.demilich.metastone.game.cards.interfaced.HeroClassImplementation;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

import java.lang.reflect.Type;
import java.util.Map;

public class HeroClassDeserializer implements JsonDeserializer<HeroClassImplementation> {

	@SuppressWarnings("unchecked")
	@Override
	public HeroClassImplementation deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		if (!(json instanceof JsonPrimitive)) {
			throw new JsonParseException("Hero class parser expected an JsonObject but found " + json + " instead");
		}
		JsonPrimitive jsonPrimitive = (JsonPrimitive) json;
		String heroClassName = jsonPrimitive.getAsString();
		HeroClassImplementation heroClass;
		try {
			heroClass = HeroClassImplementation.valueOf(heroClassName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JsonParseException("Aura parser encountered an invalid class: " + heroClassName);
		}
		return heroClass;
	}

}
