package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.Environment;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Map;

public class TargetingSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		context.getEnvironment().put(Environment.SPELL_VALUE, desc.getValue(SpellArg.VALUE, context, player, target, source, 0));
		for (SpellDesc spell : (SpellDesc[]) desc.get(SpellArg.SPELLS)) {
			SpellUtils.castChildSpell(context, player, spell, source, target);
		}
		context.getEnvironment().remove(Environment.SPELL_VALUE);
	}

}
