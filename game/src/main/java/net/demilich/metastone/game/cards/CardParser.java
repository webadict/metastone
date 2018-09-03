package net.demilich.metastone.game.cards;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.desc.*;
import net.demilich.metastone.game.cards.group.GroupDesc;
import net.demilich.metastone.game.cards.interfaced.CardSetImplementation;
import net.demilich.metastone.game.cards.interfaced.HeroClassImplementation;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDeserializer;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderDesc;
import net.demilich.metastone.utils.ResourceInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;

public class CardParser {

	private static Logger logger = LoggerFactory.getLogger(CardParser.class);

	private final Gson gson;

	public CardParser() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(SpellDesc.class, new SpellDeserializer());
		Type mapType = new TypeToken<Map<Attribute, Object>>() {
		}.getType();
		gsonBuilder.registerTypeAdapter(mapType, new AttributeDeserializer());
		gsonBuilder.registerTypeAdapter(ConditionDesc.class, new ConditionDeserializer());
		gsonBuilder.registerTypeAdapter(EventTriggerDesc.class, new EventTriggerDeserializer());
		gsonBuilder.registerTypeAdapter(AuraDesc.class, new AuraDeserializer());
		gsonBuilder.registerTypeAdapter(ValueProviderDesc.class, new ValueProviderDeserializer());
		gsonBuilder.registerTypeAdapter(CardCostModifierDesc.class, new CardCostModifierDeserializer());
		gsonBuilder.registerTypeAdapter(GroupDesc.class, new GroupDeserializer());
		// Interfaced Deserializers
		gsonBuilder.registerTypeAdapter(HeroClassImplementation.class,  new HeroClassDeserializer());
		gsonBuilder.registerTypeAdapter(CardSetImplementation.class,  new CardSetDeserializer());
		gson = gsonBuilder.create();
	}

	public CardDesc parseCard(ResourceInputStream resourceInputStream) throws FileNotFoundException {
		JsonElement jsonData = gson.fromJson(new InputStreamReader(resourceInputStream.inputStream), JsonElement.class);

		String id = resourceInputStream.fileName.split("(\\.json)")[0];
		jsonData.getAsJsonObject().addProperty("id", id);
		if (!jsonData.getAsJsonObject().has("name")) {
			throw new RuntimeException(resourceInputStream.fileName + " is missing 'name' attribute!");
		}
		if (!jsonData.getAsJsonObject().has("type")) {
			throw new RuntimeException(resourceInputStream.fileName + " is missing 'type' attribute!");
		}
		if (!jsonData.getAsJsonObject().has("collectible")) {
			throw new RuntimeException(resourceInputStream.fileName + " is missing 'collectible' attribute!");
		}
		if (!jsonData.getAsJsonObject().has("set")) {
			throw new RuntimeException(resourceInputStream.fileName + " is missing 'set' attribute!");
		}
		CardType type = CardType.valueOf(jsonData.getAsJsonObject().get("type").getAsString());
		switch (type) {
		case SPELL:
			if (!checkHeroClass(jsonData)) {
				throw new RuntimeException(resourceInputStream.fileName + " has missing/corrupted 'heroClass' attribute!");
			}
			if (!checkRarity(jsonData)) {
				throw new RuntimeException(resourceInputStream.fileName + " has missing/corrupted 'rarity' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("baseManaCost")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'baseManaCost' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("description")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'description' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("spell")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'spell' attribute!");
			}
			if (jsonData.getAsJsonObject().has("trigger")) {
				return gson.fromJson(jsonData, SecretCardDesc.class);
			} else if (jsonData.getAsJsonObject().has("quest")) {
				return gson.fromJson(jsonData, QuestCardDesc.class);
			} else {
				if (!jsonData.getAsJsonObject().has("targetSelection")) {
					throw new RuntimeException(resourceInputStream.fileName + " is missing 'targetSelection' attribute!");
				}
				return gson.fromJson(jsonData, SpellCardDesc.class);
			}
		case ENCHANTMENT:
			if (!jsonData.getAsJsonObject().has("description")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'description' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("enchantment")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'enchantment' attribute!");
			}
			return gson.fromJson(jsonData, EnchantmentCardDesc.class);
		case CHOOSE_ONE:
			if (!checkHeroClass(jsonData)) {
				throw new RuntimeException(resourceInputStream.fileName + " has missing/corrupted 'heroClass' attribute!");
			}
			if (!checkRarity(jsonData)) {
				throw new RuntimeException(resourceInputStream.fileName + " has missing/corrupted 'rarity' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("baseManaCost")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'baseManaCost' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("description")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'description' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("options")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'options' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("bothOptions")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'bothOptions' attribute!");
			}
			return gson.fromJson(jsonData, ChooseOneCardDesc.class);
		case MINION:
			if (!checkHeroClass(jsonData)) {
				throw new RuntimeException(resourceInputStream.fileName + " has missing/corrupted 'heroClass' attribute!");
			}
			if (!checkRarity(jsonData)) {
				throw new RuntimeException(resourceInputStream.fileName + " has missing/corrupted 'rarity' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("baseManaCost")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'baseManaCost' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("baseAttack")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'baseAttack' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("baseHp")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'baseHp' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("description")
					&& (jsonData.getAsJsonObject().has("battlecry")
					|| jsonData.getAsJsonObject().has("deathrattle")
					||jsonData.getAsJsonObject().has("attributes")
					|| jsonData.getAsJsonObject().has("trigger")
					|| jsonData.getAsJsonObject().has("passiveTrigger")
					|| jsonData.getAsJsonObject().has("deckTrigger")
					|| jsonData.getAsJsonObject().has("options"))) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'description' attribute!");
			}
			if (jsonData.getAsJsonObject().has("options")) {
				return gson.fromJson(jsonData, ChooseBattlecryCardDesc.class);
			} else {
				return gson.fromJson(jsonData, MinionCardDesc.class);
			}
		case PERMANENT:
			if (!checkHeroClass(jsonData)) {
				throw new RuntimeException(resourceInputStream.fileName + " has missing/corrupted 'heroClass' attribute!");
			}
			if (!checkRarity(jsonData)) {
				throw new RuntimeException(resourceInputStream.fileName + " has missing/corrupted 'rarity' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("description")
					&& (jsonData.getAsJsonObject().has("battlecry")
					|| jsonData.getAsJsonObject().has("deathrattle")
					||jsonData.getAsJsonObject().has("attributes")
					|| jsonData.getAsJsonObject().has("trigger")
					|| jsonData.getAsJsonObject().has("passiveTrigger")
					|| jsonData.getAsJsonObject().has("deckTrigger")
					|| jsonData.getAsJsonObject().has("options"))) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'description' attribute!");
			}
			return gson.fromJson(jsonData, PermanentCardDesc.class);
		case WEAPON:
			if (!checkHeroClass(jsonData)) {
				throw new RuntimeException(resourceInputStream.fileName + " has missing/corrupted 'heroClass' attribute!");
			}
			if (!checkRarity(jsonData)) {
				throw new RuntimeException(resourceInputStream.fileName + " has missing/corrupted 'rarity' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("baseManaCost")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'baseManaCost' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("damage")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'damage' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("durability")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'durability' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("description")
					&& (jsonData.getAsJsonObject().has("battlecry")
					|| jsonData.getAsJsonObject().has("deathrattle")
					|| jsonData.getAsJsonObject().has("attributes")
					|| jsonData.getAsJsonObject().has("trigger")
					|| jsonData.getAsJsonObject().has("passiveTrigger")
					|| jsonData.getAsJsonObject().has("deckTrigger")
					|| jsonData.getAsJsonObject().has("options")
					|| jsonData.getAsJsonObject().has("onEquip")
					|| jsonData.getAsJsonObject().has("onUnequip"))) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'description' attribute!");
			}
			return gson.fromJson(jsonData, WeaponCardDesc.class);
		case HERO_POWER:
			if (!checkHeroClass(jsonData)) {
				throw new RuntimeException(resourceInputStream.fileName + " has missing/corrupted 'heroClass' attribute!");
			}
			if (!checkRarity(jsonData)) {
				throw new RuntimeException(resourceInputStream.fileName + " has missing/corrupted 'rarity' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("baseManaCost")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'baseManaCost' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("description")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'description' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("spell")
					&& !jsonData.getAsJsonObject().has("options")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'spell' or 'options' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("targetSelection")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'targetSelection' attribute!");
			}
			return gson.fromJson(jsonData, HeroPowerCardDesc.class);	
		case HERO:
			if (!checkHeroClass(jsonData)) {
				throw new RuntimeException(resourceInputStream.fileName + " has missing/corrupted 'heroClass' attribute!");
			}
			if (!checkRarity(jsonData)) {
				throw new RuntimeException(resourceInputStream.fileName + " has missing/corrupted 'rarity' attribute!");
			}
			if (!jsonData.getAsJsonObject().has("rarity")) {
				throw new RuntimeException(resourceInputStream.fileName + " is missing 'rarity' attribute!");
			} else {
				boolean check = false;
				for (Rarity rarity : Rarity.values()) {
					if (jsonData.getAsJsonObject().get("rarity").getAsString().equalsIgnoreCase(rarity.toString())) {
						check = true;
					}
				}
				if (!check) {
					throw new RuntimeException(resourceInputStream.fileName + " has corrupted 'rarity' attribute!");
				}
			}
			return gson.fromJson(jsonData, HeroCardDesc.class);
		case GROUP:
			return gson.fromJson(jsonData, GroupCardDesc.class);
		default:
			logger.error("Unknown cardType: " + type);
			break;
		}
		return null;
	}

	public boolean checkHeroClass(JsonElement jsonData) {
		if (!jsonData.getAsJsonObject().has("heroClass")) {
			return false;
		} else {
			for (HeroClassImplementation heroClass : HeroClassImplementation.values()) {
				if (jsonData.getAsJsonObject().get("heroClass").getAsString().equalsIgnoreCase(heroClass.toString())) {
					return true;
				}
			}
			return false;
		}
	}

	public boolean checkRarity(JsonElement jsonData) {
		if (!jsonData.getAsJsonObject().has("rarity")) {
			return false;
		} else {
			boolean check = false;
			for (Rarity rarity : Rarity.values()) {
				if (jsonData.getAsJsonObject().get("rarity").getAsString().equalsIgnoreCase(rarity.toString())) {
					check = true;
				}
			}
			return check;
		}
	}
}
