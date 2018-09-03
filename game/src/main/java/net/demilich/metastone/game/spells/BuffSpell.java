package net.demilich.metastone.game.spells;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.demilich.metastone.game.cards.enchantment.Enchantment;
import net.demilich.metastone.game.spells.desc.enchantment.EnchantmentArg;
import net.demilich.metastone.game.spells.desc.enchantment.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

public class BuffSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(BuffSpell.class);

	public static SpellDesc create(EntityReference target, int value) {
		Map<SpellArg, Object> arguments = SpellDesc.build(BuffSpell.class);
		arguments.put(SpellArg.VALUE, value);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(EntityReference target, int attackBonus, int hpBonus) {
		Map<SpellArg, Object> arguments = SpellDesc.build(BuffSpell.class);
		arguments.put(SpellArg.ATTACK_BONUS, attackBonus);
		arguments.put(SpellArg.HP_BONUS, hpBonus);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int attackBonus = desc.getValue(SpellArg.ATTACK_BONUS, context, player, target, source, 0);
		int hpBonus = desc.getValue(SpellArg.HP_BONUS, context, player, target, source, 0);
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		AlgebraicOperation operation = (AlgebraicOperation) desc.get(SpellArg.OPERATION);
		if (operation == null) {
			operation = AlgebraicOperation.ADD;
		}

		if (value != 0) {
			attackBonus = hpBonus = value;
		}
		logger.debug("{} gains ({})", target, attackBonus + "/" + hpBonus);

		List<Integer> target_ids = new ArrayList<>();
		target_ids.add(target.getId());

		Condition condition = (Condition) desc.get(SpellArg.CONDITION);
		if (attackBonus != 0) {
			Map<EnchantmentArg, Object> enchantmentMap = EnchantmentDesc.build(Enchantment.class);
			enchantmentMap.put(EnchantmentArg.CARD_IDS, target_ids);
			enchantmentMap.put(EnchantmentArg.VALUE, attackBonus);
			enchantmentMap.put(EnchantmentArg.ATTRIBUTE, Attribute.ATTACK);
			if (condition != null) {
				enchantmentMap.put(EnchantmentArg.CONDITION, condition);
			}
			enchantmentMap.put(EnchantmentArg.OPERATION, operation);
			Enchantment enchantment = new Enchantment(new EnchantmentDesc(enchantmentMap));
			context.getEnchantments().add(enchantment);
		}
		if (hpBonus != 0) {
			Map<EnchantmentArg, Object> enchantmentMap = EnchantmentDesc.build(Enchantment.class);
			enchantmentMap.put(EnchantmentArg.CARD_IDS, target_ids);
			enchantmentMap.put(EnchantmentArg.VALUE, hpBonus);
			enchantmentMap.put(EnchantmentArg.ATTRIBUTE, Attribute.MAX_HP);
			if (condition != null) {
				enchantmentMap.put(EnchantmentArg.CONDITION, condition);
			}
			enchantmentMap.put(EnchantmentArg.OPERATION, operation);
			Enchantment enchantment = new Enchantment(new EnchantmentDesc(enchantmentMap));
			context.getEnchantments().add(enchantment);
			target.modifyHpBonus(hpBonus, context.getLogic().getEntityMaxHp(target));
		}
	}

}
