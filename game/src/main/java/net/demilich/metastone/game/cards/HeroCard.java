package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.entities.Attribute;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.actions.PlayHeroCardAction;
import net.demilich.metastone.game.cards.desc.HeroCardDesc;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.heroes.powers.HeroPower;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class HeroCard extends Card {

	private static final Set<Attribute> inheritedAttributes = new HashSet<Attribute>(
			Arrays.asList(new Attribute[] { Attribute.HP, Attribute.MAX_HP, Attribute.ARMOR }));

	private final HeroCardDesc desc;

	public HeroCard(HeroCardDesc desc) {
		super(desc);
		this.desc = desc;
	}

	public Hero createHero() {
		HeroPower heroPower = (HeroPower) CardCatalogue.getCardById(desc.heroPower);
		Hero hero = new Hero(this, heroPower);
		for (Attribute gameTag : getAttributes().keySet()) {
			if (inheritedAttributes.contains(gameTag)) {
				hero.setAttribute(gameTag, getAttribute(gameTag));
			}
		}
		hero.setTribe(desc.tribe);
		return hero;
	}

	@Override
	public PlayCardAction play() {
		return new PlayHeroCardAction(getCardReference());
	}

}
