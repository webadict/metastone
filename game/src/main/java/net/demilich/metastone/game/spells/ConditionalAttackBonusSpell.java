package net.demilich.metastone.game.spells;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.demilich.metastone.game.entities.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.enchantment.Enchantment;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.enchantment.EnchantmentArg;
import net.demilich.metastone.game.spells.desc.enchantment.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * @deprecated As of v2.0, use BuffSpell instead.
 */
@Deprecated
public class ConditionalAttackBonusSpell extends Spell {

	public static SpellDesc create(EntityReference target, ValueProvider valueProvider) {
		Map<SpellArg, Object> arguments = SpellDesc.build(ConditionalAttackBonusSpell.class);
		arguments.put(SpellArg.VALUE, valueProvider);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(ValueProvider valueProvider) {
		return create(null, valueProvider);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int attackBonus = desc.getValue(SpellArg.ATTACK_BONUS, context, player, target, source, 0);
		int hpBonus = desc.getValue(SpellArg.HP_BONUS, context, player, target, source, 0);
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);

		if (value != 0) {
			attackBonus = hpBonus = value;
		}

		List<Integer> target_ids = new ArrayList<>();
		target_ids.add(target.getId());
		if (attackBonus != 0) {
			Map<EnchantmentArg, Object> enchantmentMap = EnchantmentDesc.build(Enchantment.class);
			enchantmentMap.put(EnchantmentArg.CARD_IDS, target_ids);
			enchantmentMap.put(EnchantmentArg.VALUE, attackBonus);
			enchantmentMap.put(EnchantmentArg.ATTRIBUTE, Attribute.ATTACK);
			Enchantment enchantment = new Enchantment(new EnchantmentDesc(enchantmentMap));
			context.getEnchantments().add(enchantment);
		}
		if (hpBonus != 0) {
			Map<EnchantmentArg, Object> enchantmentMap = EnchantmentDesc.build(Enchantment.class);
			enchantmentMap.put(EnchantmentArg.CARD_IDS, target_ids);
			enchantmentMap.put(EnchantmentArg.VALUE, hpBonus);
			enchantmentMap.put(EnchantmentArg.ATTRIBUTE, Attribute.MAX_HP);
			Enchantment enchantment = new Enchantment(new EnchantmentDesc(enchantmentMap));
			context.getEnchantments().add(enchantment);
			target.modifyHpBonus(hpBonus, context.getLogic().getEntityMaxHp(target));
		}
	}
}
