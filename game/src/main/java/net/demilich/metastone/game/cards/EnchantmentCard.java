package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.cards.desc.EnchantmentCardDesc;
import net.demilich.metastone.game.cards.enchantment.Enchantment;
import net.demilich.metastone.game.cards.group.GroupDesc;
import net.demilich.metastone.game.spells.desc.enchantment.EnchantmentDesc;

public class EnchantmentCard extends Card {

	private EnchantmentDesc enchantmentDesc;

	public EnchantmentCard(EnchantmentCardDesc desc) {
		super(desc);
		this.enchantmentDesc = desc.enchantment;
	}

	public EnchantmentDesc getEnchantmentDesc() {
		return enchantmentDesc;
	}

	@Override
	public PlayCardAction play() {
		throw new UnsupportedOperationException("The method .play() should not be called for " + getClass().getName());
	}

}
