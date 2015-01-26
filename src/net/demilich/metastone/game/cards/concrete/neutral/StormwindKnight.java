package net.demilich.metastone.game.cards.concrete.neutral;

import net.demilich.metastone.game.GameTag;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;

public class StormwindKnight extends MinionCard {

	public StormwindKnight() {
		super("Stormwind Knight", 2, 5, Rarity.FREE, HeroClass.ANY, 4);
		setDescription("Charge");
	}

	@Override
	public int getTypeId() {
		return 209;
	}



	@Override
	public Minion summon() {
		return createMinion(GameTag.CHARGE);
	}
}
