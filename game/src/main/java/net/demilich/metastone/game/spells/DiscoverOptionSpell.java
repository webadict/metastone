package net.demilich.metastone.game.spells;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

public class DiscoverOptionSpell extends Spell {
	
	public static SpellDesc create(EntityReference target, SpellDesc spell) {
		Map<SpellArg, Object> arguments = SpellDesc.build(DiscoverOptionSpell.class);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.SPELL, spell);
		return new SpellDesc(arguments);
	}
	
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		List<SpellDesc> spells = new ArrayList<SpellDesc>();
		for (SpellDesc spell : (SpellDesc[]) desc.get(SpellArg.SPELLS)) {
			spells.add(spell);
		}
		
		List<SpellDesc> spellChoices = new ArrayList<SpellDesc>();
		int count = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 3);
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		boolean exclusive = desc.getBool(SpellArg.EXCLUSIVE);
		for (int i = 0; i < count; i++) {
			if (!spells.isEmpty()) {
				SpellDesc spell = spells.get(context.getLogic().random(spells.size()));
				spellChoices.add(spell);
				spells.remove(spell);
			}
		}
		
		Map<SpellDesc, Integer> spellOrder = new HashMap<SpellDesc, Integer>();
		for (int i = 0; i < spells.size(); i++)
		{
		    SpellDesc spell = spells.get(i);
		    spellOrder.put(spell, i);
		}
		List<Integer> chosenSpellInts = new ArrayList<Integer>();
		for (int i = 0; i < value; i++) {
			if (!spells.isEmpty()) {
				SpellDesc chosenSpell = SpellUtils.getSpellDiscover(context, player, desc, spellChoices).getSpell();
				chosenSpellInts.add(spellOrder.get(chosenSpell));
				if (exclusive) {
					spells.remove(chosenSpell);
				}
			}
		}
		Collections.sort(chosenSpellInts);
		SpellDesc[] chosenSpells = new SpellDesc[chosenSpellInts.size()];
		for (int i = 0; i < chosenSpellInts.size(); i++) {
			chosenSpells[i] = spells.get(chosenSpellInts.get(i));
		}
		if (chosenSpellInts.size() > 0) {
			SpellUtils.castChildSpell(context, player, MetaSpell.create(target.getReference(), false, chosenSpells), source, target);
		}
	}

}
