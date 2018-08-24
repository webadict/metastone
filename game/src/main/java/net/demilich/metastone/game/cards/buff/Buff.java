package net.demilich.metastone.game.cards.buff;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.buff.BuffArg;
import net.demilich.metastone.game.spells.desc.buff.BuffDesc;
import net.demilich.metastone.game.spells.desc.buff.BuffType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.spells.trigger.GameEventTrigger;
import net.demilich.metastone.game.spells.trigger.IGameEventListener;
import net.demilich.metastone.game.targeting.EntityReference;

public class Buff extends CustomCloneable implements IGameEventListener {

	private boolean expired;
	private int owner;
	private EntityReference hostReference;
	private GameEventTrigger expirationTrigger;

	private Attribute attribute;
	private BuffDesc desc;

	private Card cardSource;
	private BuffType buffType = BuffType.STANDARD;

	public Buff(BuffDesc desc) {
		this(desc, null);
	}

	public Buff(BuffDesc desc, Card card) {
		this.desc = desc;
		this.cardSource = card;
		this.attribute = (Attribute) desc.get(BuffArg.ATTRIBUTE);
		EventTriggerDesc triggerDesc = (EventTriggerDesc) desc.get(BuffArg.EXPIRATION_TRIGGER);
		if (triggerDesc != null) {
			this.expirationTrigger = triggerDesc.create();
		}
	}

	public Buff(BuffDesc desc, Card card, BuffType buffType) {
		this(desc, card);
		this.buffType = buffType;
	}
	
	@Override
	public boolean canFire(GameEvent event) {
		return true;
	}

	@Override
	public Buff clone() {
		Buff clone = (Buff) super.clone();
		clone.expirationTrigger = expirationTrigger != null ? (GameEventTrigger) expirationTrigger.clone() : null;
		return clone;
	}

	public void expire() {
		expired = true;
	}

	protected Object get(BuffArg arg) {
		return desc.get(arg);
	}

	public BuffType getBuffType() {
		return buffType;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	@Override
	public EntityReference getHostReference() {
		return hostReference;
	}

	@Override
	public int getOwner() {
		return owner;
	}

	protected TargetPlayer getTargetPlayer() {
		if (!desc.contains(BuffArg.TARGET_PLAYER)) {
			return TargetPlayer.SELF;
		}
		return (TargetPlayer) desc.get(BuffArg.TARGET_PLAYER);
	}

	@Override
	public boolean interestedIn(GameEventType eventType) {
		if (expirationTrigger == null) {
			return false;
		}
		return eventType == expirationTrigger.interestedIn() || expirationTrigger.interestedIn() == GameEventType.ALL;
	}

	@Override
	public boolean isExpired() {
		return expired;
	}

	@Override
	public void onAdd(GameContext context) {
	}

	@Override
	public void onGameEvent(GameEvent event) {
		Entity host = event.getGameContext().resolveSingleTarget(getHostReference());
		if (expirationTrigger != null && event.getEventType() == expirationTrigger.interestedIn() && expirationTrigger.fires(event, host)) {
			expire();
		}
	}

	@Override
	public void onRemove(GameContext context) {
		expired = true;
	}

	public boolean isAttribute(Attribute attribute) {
		return attribute == this.attribute;
	}

	public boolean getBool() {
		return desc.getBool(BuffArg.VALUE);
	}

	public int getValue(GameContext context, Entity target, Entity host, int currentValue) {
		Player player = context.getActivePlayer();
		AlgebraicOperation operation = (AlgebraicOperation) desc.get(BuffArg.OPERATION);
		if (operation == null) {
			operation = AlgebraicOperation.ADD;
		}
		int value = desc.getValue(BuffArg.VALUE, context, player, target, host, 0);
		switch (operation) {
			case ADD:
				return currentValue + value;
			case SUBTRACT:
				return currentValue - value;
			case SET:
				return value;
			case MAXIMUM:
				return Math.max(currentValue, value);
			case MINIMUM:
				return Math.min(currentValue, value);
			case MODULO:
				return currentValue % value;
			case MULTIPLY:
				return currentValue * value;
			case DIVIDE:
				if (value == 0) {
					value = 1;
				}
				return currentValue / value;
			case NEGATE:
				return -currentValue;
		}
		return currentValue;
	}

	@Override
	public void setHost(Entity host) {
		hostReference = host.getReference();
	}

	@Override
	public void setOwner(int playerIndex) {
		this.owner = playerIndex;
		if (expirationTrigger != null) {
			expirationTrigger.setOwner(playerIndex);
		}
	}

	@Override
	public boolean hasPersistentOwner() {
		return false;
	}

	@Override
	public boolean oneTurnOnly() {
		return false;
	}

	@Override
	public boolean isDelayed() {
		return false;
	}

	@Override
	public void delayTimeDown() {
		
	}

	@Override
	public boolean canFireCondition(GameEvent event) {
		if (expirationTrigger != null) {
			return expirationTrigger.canFireCondition(event);
		}
		return true;
	}

	@Override
	public boolean hasCounter() {
		return false;
	}

	@Override
	public void countDown() {
		
	}

}
