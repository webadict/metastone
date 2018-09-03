package net.demilich.metastone.game.decks;

import net.demilich.metastone.game.cards.interfaced.NonHeroClass;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MetaDeck extends Deck {

	private final List<Deck> decks;

	public MetaDeck() {
		this(new ArrayList<>());
	}

	public MetaDeck(List<Deck> decks) {
		super(NonHeroClass.DECK_COLLECTION);
		this.decks = decks;
	}

	public List<Deck> getDecks() {
		return decks;
	}

	public boolean isComplete() {
		return decks.size() > 1;
	}

	public Deck selectRandom() {
		return getDecks().get(ThreadLocalRandom.current().nextInt(getDecks().size()));
	}

}
