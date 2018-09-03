package net.demilich.metastone.game.cards.interfaced;

public enum NonBaseHeroClass implements HeroClassImplementation {
	BOSS,
	DEATH_KNIGHT,
	;

	@Override
	public String getName() {
		return name();
	}

	@Override
	public boolean isBaseClass() {
		return false;
	}
}
