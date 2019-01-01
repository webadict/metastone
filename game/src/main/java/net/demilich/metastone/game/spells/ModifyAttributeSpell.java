package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.entities.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * @deprecated As of v2.0, use AddAttributeSpell
 */
@Deprecated
public class ModifyAttributeSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Attribute attribute = (Attribute) desc.get(SpellArg.ATTRIBUTE);
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		target.modifyAttribute(attribute, value);
	}

}
