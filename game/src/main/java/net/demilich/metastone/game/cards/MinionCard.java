package net.demilich.metastone.game.cards;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.actions.PlayMinionCardAction;
import net.demilich.metastone.game.cards.desc.MinionCardDesc;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;

public class MinionCard extends SummonCard {

	private static final Set<Attribute> ignoredAttributes = new HashSet<Attribute>(
			Arrays.asList(new Attribute[] { Attribute.PASSIVE_TRIGGER, Attribute.DECK_TRIGGER, Attribute.MANA_COST_MODIFIER, Attribute.ATTACK,
					Attribute.MAX_HP, Attribute.SECRET, Attribute.QUEST, Attribute.CHOOSE_ONE, Attribute.BATTLECRY, Attribute.COMBO }));

	private final MinionCardDesc desc;

	public MinionCard(MinionCardDesc desc) {
		super(desc);
		setAttribute(Attribute.ATTACK, desc.baseAttack);
		setAttribute(Attribute.HP, desc.baseHp);
		setAttribute(Attribute.MAX_HP, desc.baseHp);
		if (desc.race != null) {
			setRace(desc.race);
		}
		this.desc = desc;
	}

	protected Minion createMinion(Attribute... tags) {
		Minion minion = new Minion(this);
		for (Attribute gameTag : getAttributes().keySet()) {
			if (!ignoredAttributes.contains(gameTag)) {
				minion.setAttribute(gameTag, getAttribute(gameTag));
			}
		}
		minion.setBaseAttack(getBaseAttack());
		minion.setHp(getHp());
		minion.setBaseHp(getBaseHp());
		BattlecryDesc battlecry = desc.battlecry;
		if (battlecry != null) {
			BattlecryAction battlecryAction = BattlecryAction.createBattlecry(battlecry.spell, battlecry.getTargetSelection());
			if (battlecry.condition != null) {
				battlecryAction.setCondition(battlecry.condition.create());
			}

			minion.setBattlecry(battlecryAction);
		}

		if (desc.deathrattle != null) {
			minion.removeAttribute(Attribute.DEATHRATTLES);
			minion.addDeathrattle(desc.deathrattle);
		}
		if (desc.trigger != null) {
			minion.addSpellTrigger(desc.trigger.create());
		}
		if (desc.triggers != null) {
			for (TriggerDesc trigger : desc.triggers) {
				minion.addSpellTrigger(trigger.create());
			}
		}
		if (desc.aura != null) {
			minion.addSpellTrigger(desc.aura.create());
		}
		if (desc.cardCostModifier != null) {
			minion.setCardCostModifier(desc.cardCostModifier.create());
		}
		// TODO: Check if this is needed???
		//minion.setHp(minion.getMaxHp());
		return minion;
	}

	public int getAttack(GameContext context, Player player) {
		return getAttributeValue(context, Attribute.ATTACK);
	}

	public int getHp() {
		return getBaseAttributeValue(Attribute.HP);
	}

	public int getBaseAttack() {
		return getBaseAttributeValue(Attribute.ATTACK);
	}

	public int getBaseHp() {
		return getBaseAttributeValue(Attribute.MAX_HP);
	}

	@Override
	public PlayCardAction play() {
		return new PlayMinionCardAction(getCardReference());
	}

	public void setRace(Race race) {
		setAttribute(Attribute.RACE, race);
	}

	public Minion summon() {
		return createMinion();
	}

}
