package net.demilich.metastone.game.cards.enchantment;

import net.demilich.metastone.game.entities.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.enchantment.EnchantmentArg;
import net.demilich.metastone.game.spells.desc.enchantment.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.spells.trigger.GameEventTrigger;
import net.demilich.metastone.game.spells.trigger.IGameEventListener;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Enchantment extends CustomCloneable implements IGameEventListener {

	private boolean expired;
	private int owner;
	private EntityReference hostReference;
	private GameEventTrigger expirationTrigger;

	private EnchantmentDesc desc;

	protected Map<Attribute, Object> buffs = new HashMap<>();

	public Enchantment(EnchantmentDesc desc) {
		this.desc = desc;
		EventTriggerDesc triggerDesc = (EventTriggerDesc) desc.get(EnchantmentArg.EXPIRATION_TRIGGER);
		if (triggerDesc != null) {
			this.expirationTrigger = triggerDesc.create();
		}
	}

	public boolean appliesTo(Entity entity) {
		if (expired) {
			return false;
		}

		if (!getRequiredCardIds().isEmpty() && !getRequiredCardIds().contains(entity.getId())) {
			return false;
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	protected List<Integer> getRequiredCardIds() {
		if (!desc.contains(EnchantmentArg.CARD_IDS)) {
			return new ArrayList<>();
		}
		return (List<Integer>) desc.get(EnchantmentArg.CARD_IDS);
	}
	
	@Override
	public boolean canFire(GameEvent event) {
		return true;
	}

	private void checkCondition(GameContext context, Player player, Entity source, Entity target) {
		Condition condition = (Condition) desc.get(EnchantmentArg.CONDITION);
		if (condition == null) {
			return;
		}
		if (!condition.isFulfilled(context, player, source, target)) {
			expire();
		}
	}

	@Override
	public Enchantment clone() {
		Enchantment clone = (Enchantment) super.clone();
		clone.expirationTrigger = expirationTrigger != null ? expirationTrigger.clone() : null;
		return clone;
	}

	public void expire() {
		expired = true;
	}

	protected Object get(EnchantmentArg arg) {
		return desc.get(arg);
	}

	public Attribute getAttribute() {
		return (Attribute) desc.get(EnchantmentArg.ATTRIBUTE);
	}

	@Override
	public EntityReference getHostReference() {
		return hostReference;
	}

	public int getMinValue() {
		return desc.getInt(EnchantmentArg.MIN_VALUE);
	}

	@Override
	public int getOwner() {
		return owner;
	}

	protected TargetPlayer getTargetPlayer() {
		if (!desc.contains(EnchantmentArg.TARGET_PLAYER)) {
			return TargetPlayer.SELF;
		}
		return (TargetPlayer) desc.get(EnchantmentArg.TARGET_PLAYER);
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

	public int process(int currentAttributeValue) {
		AlgebraicOperation operation = (AlgebraicOperation) desc.get(EnchantmentArg.OPERATION);
		int value = desc.getInt(EnchantmentArg.VALUE);
		if (operation != null) {
			return operation.performOperation(currentAttributeValue, value);
		}
		return currentAttributeValue + desc.getInt(EnchantmentArg.VALUE);
	}

	public boolean process(boolean currentAttributeBool) {
		boolean value = desc.getBool(EnchantmentArg.VALUE);
		return currentAttributeBool || value;
	}

//	public Object process(Card card, Object currentAttributeValue) {
//		AlgebraicOperation operation = (AlgebraicOperation) desc.get(EnchantmentArg.OPERATION);
//		int value = desc.getInt(EnchantmentArg.VALUE);
//		if (operation != null) {
//			return operation.performOperation(currentAttributeValue, value);
//		}
//		return currentAttributeValue + desc.getInt(EnchantmentArg.VALUE);
//	}

	public boolean getAttributeBool(Attribute attribute) {
		if (buffs.containsKey(attribute)) {
			Object attributeObject = buffs.get(attribute);
			if (attributeObject instanceof Boolean) {
				return (boolean) attributeObject;
			}
		}
		return false;
	}

//	public int getAttributeValue(GameContext context, Attribute attribute) {
//		if (buffs.containsKey(attribute)) {
//			Object attributeObject = buffs.get(attribute);
//			if (attributeObject instanceof Integer) {
//				return (int) attributeObject;
//			} else if (attributeObject instanceof ValueProvider) {
//				Entity host = context.resolveSingleTarget(hostReference);
//				// ((ValueProvider) attributeObject).getValue(context, player, target, host);
//				// TODO: Get Buffs to use Values instead.
//			}
//		}
//		return 0;
//	}

//	public Object getAttribute(Attribute attribute) {
//		switch (GameTagUtils.getTagValueType(attribute)) {
//			case INTEGER:
//				return getAttributeValue(attribute);
//			case BOOLEAN:
//				return getAttributeBool(attribute);
//		}
//		// TODO: Finish Buffs to work with Deathrattles
//		if (attribute == Attribute.DEATHRATTLES) {
//
//		}
//		return null;
//	}

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
