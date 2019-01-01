package net.demilich.metastone.game.behaviour.threat;

import java.util.ArrayList;
import java.util.List;

import net.demilich.metastone.game.entities.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.behaviour.heuristic.IGameStateHeuristic;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.cards.interfaced.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;

public class ThreatBasedHeuristic implements IGameStateHeuristic {

	private static List<String> hardRemoval;

	static {
		hardRemoval = new ArrayList<>();
		hardRemoval.add("spell_polymorph");
		hardRemoval.add("spell_execute");
		hardRemoval.add("spell_crush");
		hardRemoval.add("spell_assassinate");
		hardRemoval.add("spell_siphon_soul");
		hardRemoval.add("spell_shadow_word_death");
		hardRemoval.add("spell_naturalize");
		hardRemoval.add("spell_hex");
		hardRemoval.add("spell_humility");
		hardRemoval.add("spell_equality");
		hardRemoval.add("spell_deadly_shot");
		hardRemoval.add("spell_sap");
		hardRemoval.add("minion_doomsayer");
		hardRemoval.add("minion_big_game_hunter");
	}

	private static ThreatLevel calcuateThreatLevel(GameContext context, int playerId) {
		int damageOnBoard = 0;
		Player player = context.getPlayer(playerId);
		Player opponent = context.getOpponent(player);
		for (Minion minion : opponent.getMinions()) {
			damageOnBoard += context.getLogic().getEntityAttack(minion) * minion.getAttributeValue(Attribute.NUMBER_OF_ATTACKS);
		}
		damageOnBoard += getHeroDamage(opponent.getHero());

		int remainingHp = player.getHero().getEffectiveHp() - damageOnBoard;
		if (remainingHp < 1) {
			return ThreatLevel.RED;
		} else if (remainingHp < 15) {
			return ThreatLevel.YELLOW;
		}

		return ThreatLevel.GREEN;
	}

	private static int getHeroDamage(Hero hero) {
		int heroDamage = 0;
		if (hero.getHeroClass() == HeroClass.MAGE) {
			heroDamage += 1;
		} else if (hero.getHeroClass() == HeroClass.HUNTER) {
			heroDamage += 2;
		} else if (hero.getHeroClass() == HeroClass.DRUID) {
			heroDamage += 1;
		} else if (hero.getHeroClass() == HeroClass.ROGUE) {
			heroDamage += 1;
		}
		if (hero.getWeapon() != null) {
			heroDamage += hero.getWeapon().getWeaponDamage();
		}
		return heroDamage;
	}

	private static boolean isHardRemoval(Card card) {
		return hardRemoval.contains(card.getCardId());
	}

	private final FeatureVector weights;

	public ThreatBasedHeuristic(FeatureVector vector) {
		this.weights = vector;
	}

	private double calculateMinionScore(GameContext context, Minion minion, ThreatLevel threatLevel) {
		if (context.getLogic().hasEntityAttribute(minion, Attribute.MARKED_FOR_DEATH)) {
			return 0;
		}
		double minionScore = weights.get(WeightedFeature.MINION_INTRINSIC_VALUE);
		minionScore += weights.get(WeightedFeature.MINION_ATTACK_FACTOR)
				* (context.getLogic().getEntityAttack(minion) - minion.getAttributeValue(Attribute.TEMPORARY_ATTACK_BONUS));
		minionScore += weights.get(WeightedFeature.MINION_HP_FACTOR) * minion.getHp();

		if (context.getLogic().hasEntityAttribute(minion, Attribute.TAUNT)) {
			switch (threatLevel) {
			case RED:
				minionScore += weights.get(WeightedFeature.MINION_RED_TAUNT_MODIFIER);
				break;
			case YELLOW:
				minionScore += weights.get(WeightedFeature.MINION_YELLOW_TAUNT_MODIFIER);
				break;
			default:
				minionScore += weights.get(WeightedFeature.MINION_DEFAULT_TAUNT_MODIFIER);
				break;
			}
		}

		if (context.getLogic().hasEntityAttribute(minion, Attribute.WINDFURY)) {
			minionScore += weights.get(WeightedFeature.MINION_WINDFURY_MODIFIER);
		} else if (context.getLogic().hasEntityAttribute(minion, Attribute.MEGA_WINDFURY)) {
			minionScore += 2 * weights.get(WeightedFeature.MINION_WINDFURY_MODIFIER);
		}

		if (context.getLogic().hasEntityAttribute(minion, Attribute.DIVINE_SHIELD)) {
			minionScore += weights.get(WeightedFeature.MINION_DIVINE_SHIELD_MODIFIER);
		}
		if (context.getLogic().hasEntityAttribute(minion, Attribute.SPELL_DAMAGE)) {
			minionScore += minion.getAttributeValue(Attribute.SPELL_DAMAGE) * weights.get(WeightedFeature.MINION_SPELL_POWER_MODIFIER);
		}

		if (context.getLogic().hasEntityAttribute(minion, Attribute.STEALTH) || context.getLogic().hasEntityAttribute(minion, Attribute.STEALTH_FOR_ONE_TURN)) {
			minionScore += weights.get(WeightedFeature.MINION_STEALTHED_MODIFIER);
		}
		if (context.getLogic().hasEntityAttribute(minion, Attribute.UNTARGETABLE_BY_SPELLS)) {
			minionScore += weights.get(WeightedFeature.MINION_UNTARGETABLE_BY_SPELLS_MODIFIER);
		}

		return minionScore;
	}

	@Override
	public double getScore(GameContext context, int playerId) {
		Player player = context.getPlayer(playerId);
		Player opponent = context.getOpponent(player);
		if (player.getHero().isDestroyed()) {
			return Float.NEGATIVE_INFINITY;
		}
		if (opponent.getHero().isDestroyed()) {
			return Float.POSITIVE_INFINITY;
		}
		double score = 0;

		ThreatLevel threatLevel = calcuateThreatLevel(context, playerId);
		switch (threatLevel) {
		case RED:
			score += weights.get(WeightedFeature.RED_MODIFIER);
			break;
		case YELLOW:
			score += weights.get(WeightedFeature.YELLOW_MODIFIER);
			break;
		default:
			break;
		}
		score += player.getHero().getEffectiveHp() * weights.get(WeightedFeature.OWN_HP_FACTOR);
		score += opponent.getHero().getEffectiveHp() * weights.get(WeightedFeature.OPPONENT_HP_FACTOR);
		for (Card card : player.getHand()) {
			if (isHardRemoval(card)) {
				score += weights.get(WeightedFeature.HARD_REMOVAL_VALUE);
			}
		}

		score += player.getHand().getCount() * weights.get(WeightedFeature.OWN_CARD_COUNT);
		score += opponent.getHand().getCount() * weights.get(WeightedFeature.OPPONENT_CARD_COUNT);

		for (Minion minion : player.getMinions()) {
			score += calculateMinionScore(context, minion, threatLevel);
		}

		for (Minion minion : opponent.getMinions()) {
			score -= calculateMinionScore(context, minion, threatLevel);
		}

		return score;
	}

	@Override
	public void onActionSelected(GameContext context, int playerId) {

	}

}
