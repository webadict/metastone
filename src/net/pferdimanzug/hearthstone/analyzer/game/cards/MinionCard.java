package net.pferdimanzug.hearthstone.analyzer.game.cards;

import net.pferdimanzug.hearthstone.analyzer.game.GameContext;
import net.pferdimanzug.hearthstone.analyzer.game.GameTag;
import net.pferdimanzug.hearthstone.analyzer.game.Player;
import net.pferdimanzug.hearthstone.analyzer.game.actions.ActionType;
import net.pferdimanzug.hearthstone.analyzer.game.actions.PlayCardAction;
import net.pferdimanzug.hearthstone.analyzer.game.actions.TargetRequirement;
import net.pferdimanzug.hearthstone.analyzer.game.entities.heroes.HeroClass;
import net.pferdimanzug.hearthstone.analyzer.game.entities.minions.Minion;
import net.pferdimanzug.hearthstone.analyzer.game.entities.minions.Race;

public abstract class MinionCard extends Card {

	public MinionCard(String name, Rarity rarity, HeroClass classRestriction, int manaCost) {
		super(name, CardType.MINION, rarity, classRestriction, manaCost);
	}

	public abstract Minion summon();

	protected Minion createMinion(int baseAttack, int baseHp, Race race, GameTag... tags) {
		Minion minion = new Minion(this);
		minion.setBaseAttack(baseAttack);
		minion.setBaseHp(baseHp);
		minion.setRace(race);
		for (GameTag gameTag : tags) {
			minion.setTag(gameTag);
		}
		return minion;
	}
	
	protected Minion createMinion(int baseAttack, int baseHp, GameTag... tags) {
		return createMinion(baseAttack, baseHp, Race.NONE, tags);
	}

	@Override
	public PlayCardAction play() {
		return new PlayCardAction(this) {
			{
				setTargetRequirement(TargetRequirement.NONE);
				setActionType(ActionType.SUMMON);
				setEffectHint(EffectHint.POSITIVE);
			}

			@Override
			protected void cast(GameContext context, Player player) {
				context.getLogic().summon(player, summon(), null);

			}
		};

	}

}