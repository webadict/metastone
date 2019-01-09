package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Tribe;
import net.demilich.metastone.game.events.BeforeSummonEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class BeforeMinionSummonedTrigger extends GameEventTrigger {

	public BeforeMinionSummonedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		BeforeSummonEvent summonEvent = (BeforeSummonEvent) event;
		Tribe tribe = (Tribe) desc.get(EventTriggerArg.TRIBE);
		if (tribe != null && !summonEvent.getMinion().isTribe(tribe)) {
			return false;
		}

		Attribute requiredAttribute = (Attribute) desc.get(EventTriggerArg.REQUIRED_ATTRIBUTE);
		if (requiredAttribute != null && !summonEvent.getMinion().hasAttribute(requiredAttribute)) {
			return false;
		}
		
		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.BEFORE_SUMMON;
	}

}
