package net.demilich.metastone.game.cards;

public enum Rarity {
	FREE,
	COMMON,
	RARE,
	EPIC,
	LEGENDARY;
	
	public boolean isRarity(Rarity rarity) {
		if (this == FREE && rarity == COMMON) {
			return true;
		}
		return this == rarity;
	}

}
