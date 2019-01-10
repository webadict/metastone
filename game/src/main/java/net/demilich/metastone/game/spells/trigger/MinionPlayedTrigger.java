package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.DuringSummonEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class MinionPlayedTrigger extends MinionSummonedTrigger {
	public MinionPlayedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		DuringSummonEvent duringSummonEvent = (DuringSummonEvent) event;

		// when source card is null, then this minion not played as a minion
		// card
		if (duringSummonEvent.getSource() == null) {
			return false;
		}
		return super.fire(duringSummonEvent, host);
	}

}
