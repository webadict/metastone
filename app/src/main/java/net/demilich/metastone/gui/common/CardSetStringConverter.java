package net.demilich.metastone.gui.common;

import javafx.util.StringConverter;
import net.demilich.metastone.game.cards.interfaced.CardSetImplementation;

public class CardSetStringConverter extends StringConverter<CardSetImplementation> {

	@Override
	public CardSetImplementation fromString(String string) {
		return null;
	}

	@Override
	public String toString(CardSetImplementation object) {
		return object.toString();
	}
}
