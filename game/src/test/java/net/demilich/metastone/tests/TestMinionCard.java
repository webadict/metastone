package net.demilich.metastone.tests;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.cards.desc.MinionCardDesc;
import net.demilich.metastone.game.cards.interfaced.NonHeroClass;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.minions.Minion;

import java.util.EnumMap;

public class TestMinionCard extends MinionCard {

	private static int id = 1;

	private static MinionCardDesc getDesc(int attack, int hp, Attribute... attributes) {
		MinionCardDesc desc = new MinionCardDesc();
		desc.name = "Test monster " + ++id;
		desc.rarity = Rarity.FREE;
		desc.baseAttack = attack;
		desc.baseHp = hp;
		desc.type = CardType.MINION;
		desc.heroClass = NonHeroClass.NEUTRAL;
		desc.attributes = new EnumMap<>(Attribute.class);
		for (Attribute gameTag : attributes) {
			desc.attributes.put(gameTag, true);
		}
		return desc;
	}

	private final Minion minion;

	public TestMinionCard(int baseAttack, int baseHp, Attribute... tags) {
		super(getDesc(baseAttack, baseHp, tags));
		setCollectible(false);

		this.minion = createMinion();
		for (Attribute attribute : tags) {
			minion.setAttribute(attribute);
		}
	}

	public TestMinionCard(int baseAttack, int baseHp, int manaCost) {
		super(getDesc(baseAttack, baseHp));
		setCollectible(false);
		this.minion = createMinion();
	}

	@Override
	public String getCardId() {
		return "minion_test";
	}

	public Actor getMinion() {
		return minion;
	}

	@Override
	public Minion summon() {
		return minion.clone();
	}

}
