package net.demilich.metastone.game.behaviour.heuristic;

import net.demilich.metastone.game.entities.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.minions.Minion;

public class WeightedHeuristic implements IGameStateHeuristic {

	private float calculateMinionScore(GameContext context, Minion minion) {
		float minionScore = context.getLogic().getEntityAttack(minion) + minion.getHp();
		float baseScore = minionScore;
		if (context.getLogic().hasEntityAttribute(minion, Attribute.FROZEN)) {
			return minion.getHp();
		}
		if (context.getLogic().hasEntityAttribute(minion, Attribute.TAUNT)) {
			minionScore += 2;
		}
		if (context.getLogic().hasEntityAttribute(minion, Attribute.WINDFURY)) {
			minionScore += context.getLogic().getEntityAttack(minion) * 0.5f;
		}
		if (context.getLogic().hasEntityAttribute(minion, Attribute.DIVINE_SHIELD)) {
			minionScore += 1.5f * baseScore;
		}
		if (context.getLogic().hasEntityAttribute(minion, Attribute.SPELL_DAMAGE)) {
			minionScore += minion.getAttributeValue(Attribute.SPELL_DAMAGE);
		}
		if (context.getLogic().hasEntityAttribute(minion, Attribute.ENRAGED)) {
			minionScore += 1;
		}
		if (context.getLogic().hasEntityAttribute(minion, Attribute.STEALTH) || context.getLogic().hasEntityAttribute(minion, Attribute.STEALTH_FOR_ONE_TURN)) {
			minionScore += 1;
		}
		if (context.getLogic().hasEntityAttribute(minion, Attribute.UNTARGETABLE_BY_SPELLS)) {
			minionScore += 1.5f * baseScore;
		}

		return minionScore;
	}

	@Override
	public double getScore(GameContext context, int playerId) {
		float score = 0;
		Player player = context.getPlayer(playerId);
		Player opponent = context.getOpponent(player);
		if (player.getHero().isDestroyed()) {
			return Float.NEGATIVE_INFINITY;
		}
		if (opponent.getHero().isDestroyed()) {
			return Float.POSITIVE_INFINITY;
		}
		int ownHp = player.getHero().getEffectiveHp();
		int opponentHp = opponent.getHero().getEffectiveHp();
		score += ownHp - opponentHp;

		score += player.getHand().getCount() * 3;
		score -= opponent.getHand().getCount() * 3;
		score += player.getMinions().size() * 2;
		score -= opponent.getMinions().size() * 2;
		for (Minion minion : player.getMinions()) {
			score += calculateMinionScore(context, minion);
		}
		for (Minion minion : opponent.getMinions()) {
			score -= calculateMinionScore(context, minion);
		}

		return score;
	}

	@Override
	public void onActionSelected(GameContext context, int playerId) {
	}

}
