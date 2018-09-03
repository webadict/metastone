package net.demilich.metastone.game.cards.desc;

import com.google.gson.*;
import net.demilich.metastone.game.cards.interfaced.CardSetImplementation;
import net.demilich.metastone.game.cards.interfaced.HeroClassImplementation;

import java.lang.reflect.Type;

public class CardSetDeserializer implements JsonDeserializer<CardSetImplementation> {

	@SuppressWarnings("unchecked")
	@Override
	public CardSetImplementation deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		if (!(json instanceof JsonPrimitive)) {
			throw new JsonParseException("Card set parser expected an JsonObject but found " + json + " instead");
		}
		JsonPrimitive jsonPrimitive = (JsonPrimitive) json;
		String cardSetName = jsonPrimitive.getAsString();
		CardSetImplementation cardSet;
		try {
			cardSet = CardSetImplementation.valueOf(cardSetName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JsonParseException("Aura parser encountered an invalid class: " + cardSetName);
		}
		return cardSet;
	}

}
