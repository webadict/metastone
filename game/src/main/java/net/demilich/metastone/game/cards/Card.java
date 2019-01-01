package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.entities.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.cards.interfaced.CardSetImplementation;
import net.demilich.metastone.game.cards.interfaced.HeroClassImplementation;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Tribe;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.targeting.CardLocation;
import net.demilich.metastone.game.targeting.CardReference;
import net.demilich.metastone.game.targeting.IdFactory;

import java.util.EnumMap;

public abstract class Card extends Entity {

	private String description = "";
	private final CardType cardType;
	private final CardSetImplementation cardSet;
	private final Rarity rarity;
	private HeroClassImplementation heroClass;
	private HeroClassImplementation[] heroClasses;
	private boolean collectible = true;
	private CardLocation location;
	private BattlecryDesc battlecry;
	private ValueProvider manaCostModifier;
	private final String cardId;

	public Card(CardDesc desc) {
		cardId = desc.id;
		setName(desc.name);
		setDescription(desc.description);
		setCollectible(desc.collectible);
		cardType = desc.type;
		cardSet = desc.set;
		rarity = desc.rarity;
		heroClass = desc.heroClass;
		if (desc.heroClasses != null) {
			heroClasses = desc.heroClasses;
		}

		setAttribute(Attribute.BASE_MANA_COST, desc.baseManaCost);
		if (desc.attributes != null) {
			attributes.putAll(desc.attributes);
		}

		if (desc.manaCostModifier != null) {
			manaCostModifier = desc.manaCostModifier.create();
		}

		if (desc.passiveTrigger != null) {
			attributes.put(Attribute.PASSIVE_TRIGGER, desc.passiveTrigger);
		}

		if (desc.deckTrigger != null) {
			attributes.put(Attribute.DECK_TRIGGER, desc.deckTrigger);
		}
	}

	@Override
	public Card clone() {
		Card clone = (Card) super.clone();
		clone.attributes = getAttributes().clone();
		return clone;
	}

	public boolean evaluateExpression(String operator, int value1, int value2) {
		switch(operator) {
		case "=":
			return value1 == value2;
		case ">":
			return value1 > value2;
		case "<":
			return value1 < value2;
		case ">=":
			return value1 >= value2;
		case "<=":
			return value1 <= value2;
		case "!=":
			return value1 != value2;
		}
		return false;
	}

	public int getBaseManaCost() {
		return getAttributeValue(Attribute.BASE_MANA_COST);
	}

	public BattlecryDesc getBattlecry() {
		return battlecry;
	}

	public String getCardId() {
		return cardId;
	}

	public CardReference getCardReference() {
		return new CardReference(getOwner(), getLocation(), getId(), getName());
	}

	public CardSetImplementation getCardSet() {
		return cardSet;
	}

	public CardType getCardType() {
		return cardType;
	}

	public HeroClassImplementation getHeroClass() {
		return heroClass;
	}

	public HeroClassImplementation[] getHeroClasses() {
		return heroClasses;
	}

	public Card getCopy() {
		Card copy = clone();
		copy.setId(IdFactory.UNASSIGNED);
		copy.setLocation(CardLocation.PENDING);
		copy.removeAttribute(Attribute.ATTACK_BONUS);
		copy.removeAttribute(Attribute.HP_BONUS);
		copy.removeAttribute(Attribute.MANA_COST_MODIFIER);
		return copy;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.CARD;
	}

	public CardLocation getLocation() {
		return location;
	}

	public int getManaCost(GameContext context, Player player) {
		int actualManaCost = getBaseManaCost();
		if (manaCostModifier != null) {
			actualManaCost -= manaCostModifier.getValue(context, player, null, this);
		}
		return actualManaCost;
	}

	public Rarity getRarity() {
		return rarity;
	}

	public boolean hasBattlecry() {
		return this.battlecry != null;
	}

	public boolean hasHeroClass(HeroClassImplementation heroClass) {
		if (getHeroClasses() != null) {
			for (HeroClassImplementation h : getHeroClasses()) {
				if (heroClass.equals(h)) {
					return true;
				}
			}
		} else if (heroClass == getHeroClass()) {
			return true;
		}
		return false;
	}

	public boolean isCollectible() {
		return collectible;
	}

	public boolean matchesFilter(String filter) {
		if (filter == null || filter == "") {
			return true;
		}
		String[] filters = filter.split(" ");
		for (String splitString : filters) {
			if (!matchesSplitFilter(splitString)) {
				return false;
			}
		}
		return true;
	}

	public boolean matchesSplitFilter(String filter) {
		filter = filter.toLowerCase();
		String[] split = filter.split("((<|>)=?)|(!?=)");
		if (split.length >= 2) {
			int value;
			try {
				value = Integer.parseInt(split[1]);
			} catch (Exception e) {
				return false;
			}
			String operator = filter.substring(split[0].length(), filter.indexOf(split[1], split[0].length() + 1));
			if ((split[0].contains("mana") || split[0].contains("cost")) &&
					evaluateExpression(operator, getBaseManaCost(), value)) {
				return true;
			}
			if (split[0].contains("attack") && hasBaseAttribute(Attribute.ATTACK) &&
					evaluateExpression(operator, getBaseAttributeValue(Attribute.ATTACK), value)) {
				return true;
			}
			if ((split[0].contains("health") || split[0].contains("hp")) && hasBaseAttribute(Attribute.MAX_HP) &&
					evaluateExpression(operator, getBaseAttributeValue(Attribute.MAX_HP), value)) {
				return true;
			}
		}
		if (getRarity().toString().toLowerCase().contains(filter)) {
			return true;
		}
		if (getTribe() != Tribe.NONE && getTribe().toString().toLowerCase().contains(filter)) {
			return true;
		}
		String cardType = getCardType() == CardType.CHOOSE_ONE ? "SPELL" : getCardType().toString();
		if (cardType.toLowerCase().contains(filter)) {
			return true;
		}
		if (getHeroClass().toString().toLowerCase().contains(filter)
				|| "class".contains(filter)) {
			return true;
		}
		String lowerCaseName = getName().toLowerCase();
		if (lowerCaseName.contains(filter)) {
			return true;
		}
		String regexName = lowerCaseName.replaceAll("[:,\'\\- ]+", "");
		if (regexName.contains(filter)) {
			return true;
		}
		if (getDescription() != null) {
			String lowerCaseDescription = getDescription().toLowerCase();
			if (lowerCaseDescription.contains(filter)) {
				return true;
			}
		}

		return false;
	}

	public abstract PlayCardAction play();

	public void setBattlecry(BattlecryDesc battlecry) {
		this.battlecry = battlecry;
	}

	public void setCollectible(boolean collectible) {
		this.collectible = collectible;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setLocation(CardLocation location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return String.format("[%s '%s' %s Manacost:%d]", getCardType(), getName(), getReference(), getBaseManaCost());
	}

}
