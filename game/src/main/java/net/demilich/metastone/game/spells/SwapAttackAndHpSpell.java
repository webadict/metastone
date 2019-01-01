package net.demilich.metastone.game.spells;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.demilich.metastone.game.entities.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.enchantment.Enchantment;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.enchantment.EnchantmentArg;
import net.demilich.metastone.game.spells.desc.enchantment.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.targeting.EntityReference;

public class SwapAttackAndHpSpell extends Spell {

	public static SpellDesc create() {
		return create(null);
	}

	public static SpellDesc create(EntityReference target) {
		Map<SpellArg, Object> arguments = SpellDesc.build(SwapAttackAndHpSpell.class);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Minion minion = (Minion) target;
		int attack = context.getLogic().getEntityAttack(minion);
		int hp = minion.getHp();
		context.getLogic().modifyMaxHp(minion, attack);
		List<Integer> target_ids = new ArrayList<>();
		target_ids.add(target.getId());

		Map<EnchantmentArg, Object> enchantmentMap = EnchantmentDesc.build(
				Enchantment.class,
				Attribute.ATTACK,
				hp,
				AlgebraicOperation.SET
		);
		enchantmentMap.put(EnchantmentArg.CARD_IDS, target_ids);
		Enchantment attackEnchantment = new Enchantment(new EnchantmentDesc(enchantmentMap));
		context.getLogic().addEnchantment(player, attackEnchantment, player);
		enchantmentMap = EnchantmentDesc.build(
				Enchantment.class,
				Attribute.MAX_HP,
				attack,
				AlgebraicOperation.SET
		);
		enchantmentMap.put(EnchantmentArg.CARD_IDS, target_ids);
		Enchantment maxHpEnchantment = new Enchantment(new EnchantmentDesc(enchantmentMap));
		context.getLogic().addEnchantment(player, maxHpEnchantment, player);
	}

}