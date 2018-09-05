package net.demilich.metastone.game.cards.group;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

public class CardGroup extends Group {

	public CardGroup(GroupDesc desc) {
		super(desc);
	}

	@Override
	public Entity[] getGroup(GameContext context) {
		String[] cards = (String[]) desc.get(GroupArg.CARDS);
		Card[] cardList = new Card[cards.length];
		for (int i = 0; i < cardList.length; i++) {
			Card card = context.getCardById(cards[i]);
			cardList[i] = card;
		}
		return cardList;
	}
}
