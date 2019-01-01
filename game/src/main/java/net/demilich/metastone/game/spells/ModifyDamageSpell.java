package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.entities.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;

public class ModifyDamageSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (context.getDamageStack().isEmpty()) {
			return;
		}
		
		int damage = context.getDamageStack().pop();
		AlgebraicOperation operation = (AlgebraicOperation) desc.get(SpellArg.OPERATION);
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		int minDamage = desc.getInt(SpellArg.MIN_DAMAGE, 0);
		Entity eventTarget = context.resolveSingleTarget(context.getEventTargetStack().peek());
		switch(operation) {
		case ADD:
			if (context.getLogic().hasEntityAttribute(eventTarget, Attribute.TAKE_DOUBLE_DAMAGE)) {
				value *= 2;
			}
			damage += value;
			break;
		case SUBTRACT:
			if (context.getLogic().hasEntityAttribute(eventTarget, Attribute.TAKE_DOUBLE_DAMAGE)) {
				value *= 2;
			}
			damage -= value;
			damage = Math.max(minDamage, damage);
			break;
		case MODULO:
			if (context.getLogic().hasEntityAttribute(eventTarget, Attribute.TAKE_DOUBLE_DAMAGE)) {
				damage /= 2;
			}
			damage %= value;
			if (context.getLogic().hasEntityAttribute(eventTarget, Attribute.TAKE_DOUBLE_DAMAGE)) {
				damage *= 2;
			}
			break;
		case MULTIPLY:
			damage *= value;
			break;
		case DIVIDE:
			damage /= value;
			damage = Math.max(minDamage, damage);
			break;
		case NEGATE:
			damage = -damage;
			break;
		case SET:
			if (context.getLogic().hasEntityAttribute(eventTarget, Attribute.TAKE_DOUBLE_DAMAGE)) {
				value *= 2;
			}
			damage = value;
			break;
		case MINIMUM:
			if (context.getLogic().hasEntityAttribute(eventTarget, Attribute.TAKE_DOUBLE_DAMAGE)) {
				value *= 2;
			}
			if (damage < value) {
				damage = value;
			}
			break;
		case MAXIMUM:
			if (context.getLogic().hasEntityAttribute(eventTarget, Attribute.TAKE_DOUBLE_DAMAGE)) {
				value *= 2;
			}
			if (damage > value) {
				damage = value;
			}
			break;
		default:
			break;
		}
		
		context.getDamageStack().push(damage);
	}

}
