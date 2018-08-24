package net.demilich.metastone.game.spells;

import java.util.Map;
import java.util.function.Predicate;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.w3c.dom.Attr;

public class
HealSpell extends Spell {

	public static SpellDesc create(EntityReference target, int healing) {
		Map<SpellArg, Object> arguments = SpellDesc.build(HealSpell.class);
		arguments.put(SpellArg.VALUE, healing);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(int healing) {
		return create(null, healing);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int healing = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		if (target instanceof Actor) {
			context.getLogic().heal(player, (Actor) target, healing, source);
		} else if (target instanceof Weapon) {
			target.modifyAttribute(Attribute.HP, 1);
		}
	}

}
