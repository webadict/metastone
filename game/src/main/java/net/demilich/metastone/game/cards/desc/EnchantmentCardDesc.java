package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.EnchantmentCard;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.desc.enchantment.EnchantmentDesc;

public class EnchantmentCardDesc extends CardDesc {

	public EnchantmentDesc enchantment;

	@Override
	public Card createInstance() {
		return new EnchantmentCard(this);
	}

}
