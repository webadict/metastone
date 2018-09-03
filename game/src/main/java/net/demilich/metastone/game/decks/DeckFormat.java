package net.demilich.metastone.game.decks;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.interfaced.CardSetImplementation;

import java.util.ArrayList;
import java.util.List;

public class DeckFormat {

	private String name = "";
	private String filename;
	private List<CardSetImplementation> sets;

	public DeckFormat() {
		sets = new ArrayList<>();
	}

	public void addSet(CardSetImplementation cardSet) {
		sets.add(cardSet);
	}

	public boolean isInFormat(Card card) {
		if (sets.contains(card.getCardSet())) {
			return true;
		}
		return false;
	}

	public boolean isInFormat(CardSetImplementation set) {
		return sets.contains(set);
	}

	public boolean isInFormat(Deck deck) {
		for (Card card : deck.getCards()) {
			if (!isInFormat(card)) {
				return false;
			}
		}
		return true;
	}

	public List<CardSetImplementation> getCardSets() {
		return new ArrayList<>(sets);
	}

	public String getName() {
		return name;
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
