package net.demilich.metastone.game.cards.interfaced;

public enum HeroClass implements HeroClassImplementation {
	DRUID,
	HUNTER,
	MAGE,
	PALADIN,
	PRIEST,
	ROGUE,
	SHAMAN,
	WARLOCK,
	WARRIOR,
	;

	@Override
	public String getName() {
		return name();
	}

	public boolean isBaseClass() {
		return true;
	}
}
