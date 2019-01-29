package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;

public class ArmorGainedEvent extends GameEvent implements IHasValueEvent {

	private final Hero hero;
	private int armorGained;

	public ArmorGainedEvent(GameContext context, Hero hero, int armor) {
		super(context, hero.getOwner(), -1);
		this.hero = hero;
		this.armorGained = armor;
	}
	
	@Override
	public Entity getEventTarget() {
		return hero;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.ARMOR_GAINED;
	}

	@Override
	public int getValue() {
		return armorGained;
	}

}
