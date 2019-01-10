package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;

abstract public class SummonEvent extends GameEvent {

	protected final Actor minion;
	protected final Card source;

	public SummonEvent(GameContext context, Actor minion, Card source) {
		super(context, minion.getOwner(), -1);
		this.minion = minion;
		this.source = source;
	}
	
	@Override
	public Entity getEventTarget() {
		return getMinion();
	}

	public Actor getMinion() {
		return minion;
	}

	public Card getSource() {
		return source;
	}

}
