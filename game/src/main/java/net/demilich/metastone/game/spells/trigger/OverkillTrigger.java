package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.DamageEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class OverkillTrigger extends DamageCausedTrigger {

	public OverkillTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		DamageEvent damageEvent = (DamageEvent) event;

		// Only activate on owner's turn.
		if (host.getOwner() != damageEvent.getGameContext().getActivePlayer().getId()) {
		    return false;
        }

        // Have to overkill the target.
        if (!(damageEvent.getEventTarget() instanceof Actor && ((Actor) damageEvent.getEventTarget()).getHp() < 0)) {
            return false;
        }

        // Return regular damage event.
		return super.fire(event, host);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.DAMAGE;
	}

}
