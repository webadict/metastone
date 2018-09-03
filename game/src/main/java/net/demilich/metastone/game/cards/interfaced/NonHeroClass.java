package net.demilich.metastone.game.cards.interfaced;

public enum NonHeroClass implements HeroClassImplementation {
	ANY,
	DECK_COLLECTION,
	NEUTRAL,
	OPPONENT,
	SELF,
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
