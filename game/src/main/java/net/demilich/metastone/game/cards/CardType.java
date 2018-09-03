package net.demilich.metastone.game.cards;

public enum CardType {
	CHOOSE_ONE,
	ENCHANTMENT,
	GROUP,
	HERO,
	HERO_POWER,
	MINION,
	PERMANENT,
	SPELL,
	WEAPON,
	;
	
	public boolean isCardType(CardType cardType) {
		if (this == CHOOSE_ONE && cardType == SPELL) {
			return true;
		}
		return this == cardType;
	}
}
