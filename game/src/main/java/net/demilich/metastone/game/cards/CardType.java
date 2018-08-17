package net.demilich.metastone.game.cards;

public enum CardType {
	HERO,
	MINION,
	PERMANENT,
	SPELL,
	WEAPON,
	HERO_POWER,
	CHOOSE_ONE,
	GROUP;
	
	public boolean isCardType(CardType cardType) {
		if (this == CHOOSE_ONE && cardType == SPELL) {
			return true;
		} else if (this == cardType) {
			return true;
		}
		return false;
	}
}
