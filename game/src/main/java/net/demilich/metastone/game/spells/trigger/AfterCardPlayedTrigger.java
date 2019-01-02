package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Tribe;
import net.demilich.metastone.game.events.AfterCardPlayedEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class AfterCardPlayedTrigger extends GameEventTrigger {

	public AfterCardPlayedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		AfterCardPlayedEvent cardPlayedEvent = (AfterCardPlayedEvent) event;
		CardType cardType = (CardType) desc.get(EventTriggerArg.CARD_TYPE);
		if (cardType != null && !cardPlayedEvent.getCard().getCardType().isCardType(cardType)) {
			return false;
		}
		
		Tribe tribe = (Tribe) desc.get(EventTriggerArg.TRIBE);
		if (tribe != null && cardPlayedEvent.getCard().getTribe() != tribe) {
			return false;
		}
		
		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.AFTER_CARD_PLAYED;
	}

}
