package net.demilich.metastone.game.decks;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.cards.interfaced.HeroClassImplementation;
import net.demilich.metastone.game.cards.interfaced.NonHeroClass;
import net.demilich.metastone.game.logic.GameLogic;

public class Deck {

	private String name = "";
	private final HeroClassImplementation heroClass;
	private final CardCollection cards = new CardCollection();
	private String description;
	private String filename;
	private boolean arbitrary;

	public Deck(HeroClassImplementation heroClass) {
		this.heroClass = heroClass;
	}

	public Deck(HeroClassImplementation heroClass, boolean arbitrary) {
		this.heroClass = heroClass;
		this.arbitrary = arbitrary;
	}

	public int containsHowMany(Card card) {
		int count = 0;
		for (Card cardInDeck : cards) {
			if (card.getCardId().equals(cardInDeck.getCardId())) {
				count++;
			}
		}
		return count;
	}

	public CardCollection getCards() {
		return cards;
	}

	public CardCollection getCardsCopy() {
		return getCards().clone();
	}

	public String getDescription() {
		return description;
	}

	public HeroClassImplementation getHeroClass() {
		return heroClass;
	}

	public String getName() {
		return name;
	}
	
	public boolean isArbitrary() {
		return arbitrary;
	}

	public boolean isComplete() {
		return cards.getCount() == GameLogic.DECK_SIZE;
	}
	
	public boolean isFull() {
		return cards.getCount() == GameLogic.MAX_DECK_SIZE;
	}

	public boolean isMetaDeck() {
		return getHeroClass() == NonHeroClass.DECK_COLLECTION;
	}
	
	public boolean isTooBig() {
		return cards.getCount() > GameLogic.DECK_SIZE;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
