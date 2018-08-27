package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.enchantment.Enchantment;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.enchantment.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;

import java.util.Map;

public class SwapAttributeSpell extends Spell {

	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = SpellDesc.build(SwapAttributeSpell.class);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (context.getSummonReferenceStack().isEmpty()) {
			return;
		}
		Minion sourceMinion = (Minion) context.resolveSingleTarget(context.getSummonReferenceStack().peek());
		Actor targetActor = (Actor) target;
		Attribute attribute = (Attribute) desc.get(SpellArg.ATTRIBUTE);
		Object sourceObject = context.getLogic().getEntityAttribute(sourceMinion, attribute);
		Object targetObject = context.getLogic().getEntityAttribute(targetActor, attribute);
		if (attribute == Attribute.HP) {
			context.getLogic().modifyMaxHp(sourceMinion, (int) targetObject);
			context.getLogic().modifyMaxHp(targetActor, (int) sourceObject);
			attribute = Attribute.MAX_HP;
		}
		Enchantment enchantment = new Enchantment(new EnchantmentDesc(EnchantmentDesc.build(Enchantment.class, attribute, sourceObject, AlgebraicOperation.SET, targetActor)));
		context.getLogic().addEnchantment(player, enchantment, player);
		enchantment = new Enchantment(new EnchantmentDesc(EnchantmentDesc.build(Enchantment.class, attribute, targetObject, AlgebraicOperation.SET, sourceMinion)));
		context.getLogic().addEnchantment(player, enchantment, player);
	}

}
