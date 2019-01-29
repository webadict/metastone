package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.IHasValueEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public abstract class ValuedGameEventTrigger extends GameEventTrigger {

	private boolean countByValue;

	public ValuedGameEventTrigger(EventTriggerDesc desc) {
		super(desc);
		this.countByValue = desc.hasCountByValue();
	}

	public void countDown(GameEvent event) {
		if (countByValue && event instanceof IHasValueEvent) {
			countDownByValue((IHasValueEvent) event);
		} else {
			super.countDown(event);
		}
    }

    public void countDownByValue(IHasValueEvent event) {
		setTriggerCount(getTriggerCount() - event.getValue());
		if (getTriggerCount() < 0) {
			setTriggerCount(0);
		}
	}

}
