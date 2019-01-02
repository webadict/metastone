package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.Map;

public class SetAttributeSpell extends Spell {

	public static SpellDesc create(Attribute attribute, int value) {
		Map<SpellArg, Object> arguments = SpellDesc.build(SetAttributeSpell.class);
		arguments.put(SpellArg.ATTRIBUTE, attribute);
		arguments.put(SpellArg.VALUE, value);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Attribute attribute = (Attribute) desc.get(SpellArg.ATTRIBUTE);
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		target.setAttribute(attribute, value);
	}

}