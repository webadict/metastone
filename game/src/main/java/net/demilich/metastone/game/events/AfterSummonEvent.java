package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;

public class AfterSummonEvent extends SummonEvent {

	public AfterSummonEvent(GameContext context, Actor minion, Card source) {
		super(context, minion, source);
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.AFTER_SUMMON;
	}

	@Override
	public String toString() {
		return "[After Summon Event MINION " + minion + " from SOURCE " + source + "]";
	}

}
