package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;

public class BeforeSummonEvent extends SummonEvent {

	public BeforeSummonEvent(GameContext context, Actor minion, Card source) {
		super(context, minion, source);
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.BEFORE_SUMMON;
	}

	@Override
	public String toString() {
		return "[Pre-Summon Event MINION " + minion + " from SOURCE " + source + "]";
	}

}
