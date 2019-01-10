package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;

public class DuringSummonEvent extends SummonEvent {

	public DuringSummonEvent(GameContext context, Actor minion, Card source) {
		super(context, minion, source);
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.SUMMON;
	}

	@Override
	public String toString() {
		return "[Summon Event MINION " + minion + " from SOURCE " + source + "]";
	}

}
