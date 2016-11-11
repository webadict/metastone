package net.demilich.metastone.game.spells;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardDescType;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.cards.desc.SpellCardDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.TargetSelection;

public class CreateCardSpell extends Spell {

	private SpellDesc[] discoverCardParts(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		List<SpellDesc> spells = new ArrayList<SpellDesc>();
		for (SpellDesc spell : (SpellDesc[]) desc.get(SpellArg.SPELLS)) {
			spells.add(spell);
		}
		
		List<SpellDesc> spellChoices = new ArrayList<SpellDesc>();
		int count = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 3);
		int value = desc.getValue(SpellArg.SECONDARY_VALUE, context, player, target, source, 2);
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
		return chosenSpells;
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		HeroClass heroClass = HeroClass.ANY;
		Rarity rarity = Rarity.FREE;
		CardSet cardSet = CardSet.BASIC;
		SpellDesc[] spells = discoverCardParts(context, player, desc, source, target);
		switch (source.getEntityType()) {
		case ANY:
			break;
		case CARD:
			break;
		case HERO:
			break;
		case MINION:
			Minion sourceMinion = (Minion) source;
			heroClass = sourceMinion.getSourceCard().getHeroClass();
			rarity = sourceMinion.getSourceCard().getRarity();
			cardSet = sourceMinion.getSourceCard().getCardSet();
			break;
		case PLAYER:
			break;
		case WEAPON:
			break;
		default:
			break;
		}
		Card newCard = null;
		switch ((CardType) desc.get(SpellArg.CARD_TYPE)) {
		case SPELL:
			List<SpellDesc> spellList = new ArrayList<SpellDesc>();
			String description = "";
			TargetSelection targetSelection = TargetSelection.NONE;
			for (SpellDesc spell : spells) {
				CardDescType cardDescType = (CardDescType) spell.get(SpellArg.CARD_DESC_TYPE);
				if (cardDescType == CardDescType.SPELL) {
					description += spell.getString(SpellArg.DESCRIPTION) + " ";
					spellList.add(spell);
					TargetSelection checkTS = (TargetSelection) spell.get(SpellArg.TARGET_SELECTION);
					if (checkTS != null && checkTS.compareTo(targetSelection) > 0) {
						targetSelection = checkTS;
					}
				}
			}
			SpellDesc spell = MetaSpell.create(target.getReference(), false, (SpellDesc[]) spellList.toArray());
			SpellCardDesc spellCardDesc = new SpellCardDesc();
			spellCardDesc.name = desc.getString(SpellArg.NAME);
			spellCardDesc.heroClass = heroClass;
			spellCardDesc.rarity = rarity;
			spellCardDesc.description = description;
			spellCardDesc.targetSelection = targetSelection;
			spellCardDesc.spell = spell;
			spellCardDesc.collectible = false;
			//spellCardDesc.attributes.put(key, value);
			spellCardDesc.set = cardSet;
			spellCardDesc.baseManaCost = desc.getValue(SpellArg.MANA, context, player, target, source, 0);
			newCard = spellCardDesc.createInstance();
			break;
		case CHOOSE_ONE:
		case HERO_POWER:
		case MINION:
		case WEAPON:
		default:
			return;
		}
		if (newCard != null) {
			context.getLogic().receiveCard(player.getId(), newCard);
		}
	}
}
