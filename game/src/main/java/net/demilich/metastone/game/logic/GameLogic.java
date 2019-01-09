package net.demilich.metastone.game.logic;

import net.demilich.metastone.BuildConfig;
import net.demilich.metastone.game.Environment;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlaySpellCardAction;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.cards.enchantment.Enchantment;
import net.demilich.metastone.game.cards.interfaced.HeroClass;
import net.demilich.metastone.game.cards.interfaced.NonHeroClass;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Permanent;
import net.demilich.metastone.game.entities.minions.Summon;
import net.demilich.metastone.game.entities.minions.Tribe;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.events.*;
import net.demilich.metastone.game.heroes.powers.HeroPower;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.SpellFactory;
import net.demilich.metastone.game.spells.desc.enchantment.EnchantmentArg;
import net.demilich.metastone.game.spells.desc.enchantment.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import net.demilich.metastone.game.spells.trigger.IGameEventListener;
import net.demilich.metastone.game.spells.trigger.SpellTrigger;
import net.demilich.metastone.game.spells.trigger.types.Quest;
import net.demilich.metastone.game.spells.trigger.types.Secret;
import net.demilich.metastone.game.targeting.*;
import net.demilich.metastone.game.utils.GameTagUtils;
import net.demilich.metastone.utils.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GameLogic implements Cloneable {

	public static Logger logger = LoggerFactory.getLogger(GameLogic.class);

	public static final int MAX_PLAYERS = 2;
	public static final int MAX_MINIONS = 7;
	public static final int MAX_HAND_CARDS = 10;
	public static final int MAX_HERO_HP = 30;
	public static final int STARTER_CARDS = 3;
	public static final int MAX_MANA = 10;
	public static final int MAX_QUESTS = 1;
	public static final int MAX_SECRETS = 5;
	public static final int DECK_SIZE = 30;
	public static final int MAX_DECK_SIZE = 60;
	public static final int TURN_LIMIT = 100;

	public static final int WINDFURY_ATTACKS = 2;
	public static final int MEGA_WINDFURY_ATTACKS = 4;

	public static final String TEMP_CARD_LABEL = "temp_card_id_";

	private static final int INFINITE = -1;

	private static boolean hasPlayerLost(Player player) {
		return player.getHero().isDestroyed() || player.isDestroyed();
	}

	private final TargetLogic targetLogic = new TargetLogic();
	private final ActionLogic actionLogic = new ActionLogic();
	private final SpellFactory spellFactory = new SpellFactory();
	private final IdFactory idFactory;
	private GameContext context;

	private boolean loggingEnabled = true;

	// DEBUG
	private final int MAX_HISTORY_ENTRIES = 100;
	private Queue<String> debugHistory = new LinkedList<>();

	public GameLogic() {
		idFactory = new IdFactory();
	}

	private GameLogic(IdFactory idFactory) {
		this.idFactory = idFactory;
	}

	public void addEnchantment(Player player, Enchantment enchantment, Entity target) {
		context.getEnchantments().add(enchantment);
		addGameEventListener(player, enchantment, target);
	}

	public void addGameEventListener(Player player, IGameEventListener gameEventListener, Entity target) {
		debugHistory.add("Player " + player.getId() + " has set event listener " + gameEventListener.getClass().getName() + " from entity " + target.getName() + "[Reference ID: " + target.getId() + "]");
		gameEventListener.setHost(target);
		if (!gameEventListener.hasPersistentOwner() || gameEventListener.getOwner() == -1) {
			gameEventListener.setOwner(player.getId());
		}

		gameEventListener.onAdd(context);
		context.addTrigger(gameEventListener);
		log("New spelltrigger was added for {} on {}: {}", player.getName(), target, gameEventListener);
	}

	public void addManaModifier(Player player, CardCostModifier cardCostModifier, Entity target) {
		context.getCardCostModifiers().add(cardCostModifier);
		addGameEventListener(player, cardCostModifier, target);
	}

	public void afterCardPlayed(int playerId, CardReference cardReference) {
		Player player = context.getPlayer(playerId);

		player.modifyAttribute(Attribute.COMBO, +1);
		Card card = context.resolveCardReference(cardReference);

		context.fireGameEvent(new AfterCardPlayedEvent(context, playerId, card));
		
		card.removeAttribute(Attribute.MANA_COST_MODIFIER);

		if (hasEntityAttribute(card, Attribute.ECHO)) {
			Card echoCard = card.getCopy();
			echoCard.setAttribute(Attribute.GHOSTLY);
			receiveCard(playerId, echoCard);
		}
	}

	public int applyAmplify(Player player, int baseValue, Attribute attribute) {
		int amplify = getTotalAttributeMultiplier(player, attribute);
		return baseValue * amplify;
	}

	public void applyAttribute(Entity entity, Attribute attr) {
		if (attr == Attribute.MEGA_WINDFURY
				&& hasEntityAttribute(entity, Attribute.WINDFURY)
				&& !hasEntityAttribute(entity, Attribute.MEGA_WINDFURY)) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, MEGA_WINDFURY_ATTACKS - WINDFURY_ATTACKS);
		} else if (attr == Attribute.WINDFURY
				&& !hasEntityAttribute(entity, Attribute.WINDFURY)
				&& !hasEntityAttribute(entity, Attribute.MEGA_WINDFURY)) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, WINDFURY_ATTACKS - 1);
		} else if (attr == Attribute.MEGA_WINDFURY
				&& !hasEntityAttribute(entity, Attribute.WINDFURY)
				&& !hasEntityAttribute(entity, Attribute.MEGA_WINDFURY)) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, MEGA_WINDFURY_ATTACKS - 1);
		}
		Map<EnchantmentArg, Object> enchantmentMap = EnchantmentDesc.build(Enchantment.class);
		enchantmentMap.put(EnchantmentArg.ATTRIBUTE, attr);
		List<Integer> target_ids = new ArrayList<>();
		target_ids.add(entity.getId());
		enchantmentMap.put(EnchantmentArg.CARD_IDS, target_ids);
		enchantmentMap.put(EnchantmentArg.VALUE, true);
		Enchantment enchantment = new Enchantment(new EnchantmentDesc(enchantmentMap));
		context.getEnchantments().add(enchantment);

		log("Applying attr {} to {}", attr, entity);
	}

	/**
	 * Applies hero power damage increases
	 * @param player
	 * 			The Player to grab additional hero power damage from
	 * @param baseValue
	 * 			The base damage the hero power does
	 * @return
	 * 			Increased hero power damage
	 */
	public int applyHeroPowerDamage(Player player, int baseValue) {
		int spellpower = getTotalAttributeValue(player, Attribute.HERO_POWER_DAMAGE);
		return baseValue + spellpower;
	}

	/**
	 * Applies spell damage increases
	 * @param player
	 * 			The Player to grab the additional spell damage from
	 * @param source
	 * 			The source Card
	 * @param baseValue
	 * 			The base damage the spell does
	 * @return
	 * 			Increased spell damage
	 */
	public int applySpellpower(Player player, Entity source, int baseValue) {
		int spellpower = getTotalAttributeValue(player, Attribute.SPELL_DAMAGE)
				+ getTotalAttributeValue(context.getOpponent(player), Attribute.OPPONENT_SPELL_DAMAGE);
		if (hasEntityAttribute(source, Attribute.SPELL_DAMAGE_MULTIPLIER)) {
			spellpower *= source.getAttributeValue(Attribute.SPELL_DAMAGE_MULTIPLIER);
		}
		return baseValue + spellpower;
	}

	/**
	 * Assigns an ID to each Card in a given deck
	 * @param cardCollection
	 * 		The Deck to assign IDs to
	 */
	private void assignCardIds(CardCollection cardCollection) {
		for (Card card : cardCollection) {
			card.setId(idFactory.generateId());
			card.setLocation(CardLocation.DECK);
		}
	}

	public boolean attributeExists(Attribute attr) {
		for (Player player : context.getPlayers()) {
			if (hasEntityAttribute(player.getHero(), attr)) {
				return true;
			}
			for (Entity summon : player.getSummons()) {
				if (hasEntityAttribute(summon, attr) && !hasEntityAttribute(summon, Attribute.PENDING_DESTROY)) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean canAttackThisTurn(Actor actor) {
		if (hasEntityAttribute(actor, Attribute.CANNOT_ATTACK)) {
			return false;
		}
		if (hasEntityAttribute(actor, Attribute.FROZEN)) {
			return false;
		}
		if (hasEntityAttribute(actor, Attribute.SUMMONING_SICKNESS)
				&& !(hasEntityAttribute(actor, Attribute.CHARGE)
				|| hasEntityAttribute(actor, Attribute.RUSH))) {
			return false;
		}
		return getEntityAttack(actor) > 0
				&& ((actor.getAttributeValue(Attribute.NUMBER_OF_ATTACKS) + actor.getAttributeValue(Attribute.EXTRA_ATTACKS)) > 0
				|| hasEntityAttribute(actor, Attribute.UNLIMITED_ATTACKS));
	}

	public boolean canPlayCard(int playerId, CardReference cardReference) {
		Player player = context.getPlayer(playerId);
		Card card = context.resolveCardReference(cardReference);
		int manaCost = getModifiedManaCost(player, card);
		if (card.getCardType().isCardType(CardType.SPELL)
				&& hasEntityAttribute(player, Attribute.SPELLS_COST_HEALTH)
				&& player.getHero().getEffectiveHp() < manaCost) {
			return false;
		} else if (card.getCardType().isCardType(CardType.MINION)
				&& card.isTribe(Tribe.MURLOC)
				&& hasEntityAttribute(player, Attribute.MURLOCS_COST_HEALTH)
				&& player.getHero().getEffectiveHp() < manaCost) {
			return false;
		} else if (player.getMana() < manaCost && manaCost != 0
				&& !((card.getCardType().isCardType(CardType.SPELL)
				&& hasEntityAttribute(player, Attribute.SPELLS_COST_HEALTH))
				|| (card.isTribe(Tribe.MURLOC)
				&& hasEntityAttribute(player, Attribute.MURLOCS_COST_HEALTH)))) {
			return false;
		}
		if (card.getCardType().isCardType(CardType.HERO_POWER)) {
			HeroPower power = (HeroPower) card;
			int heroPowerUsages = getGreatestAttributeValue(player, Attribute.HERO_POWER_USAGES);
			if (heroPowerUsages == 0) {
				heroPowerUsages = 1;
			}
			if (heroPowerUsages != INFINITE && power.hasBeenUsed() >= heroPowerUsages) {
				return false;
			}
		} else if (card.getCardType().isCardType(CardType.MINION)) {
			return canSummonMoreMinions(player);
		}

		if (card instanceof SpellCard) {
			SpellCard spellCard = (SpellCard) card;
			return spellCard.canBeCast(context, player);
		}
		return true;
	}

	public boolean canPlayQuest(Player player, QuestCard card) {
		return player.getSecrets().size() < MAX_SECRETS && player.getQuests().size() < MAX_QUESTS && !player.getQuests().contains(card.getCardId());
	}

	public boolean canPlaySecret(Player player, SecretCard card) {
		return player.getSecrets().size() < MAX_SECRETS && !player.getSecrets().contains(card.getCardId());
	}

	public boolean canSummonMoreMinions(Player player) {
		return player.getSummons().size() < MAX_MINIONS;
	}

	public void castChooseOneSpell(int playerId, SpellDesc spellDesc, EntityReference sourceReference, EntityReference targetReference, String cardId) {
		Player player = context.getPlayer(playerId);
		Entity source = null;
		if (sourceReference != null) {
			try {
				source = context.resolveSingleTarget(sourceReference);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Error resolving source entity while casting spell: " + spellDesc);
			}
		}
		EntityReference spellTarget = spellDesc.hasPredefinedTarget() ? spellDesc.getTarget() : targetReference;
		List<Entity> targets = targetLogic.resolveTargetKey(context, player, source, spellTarget);
		Card sourceCard = null;
		SpellCard chosenCard = (SpellCard) context.getCardById(cardId);
		sourceCard = source.getEntityType() == EntityType.CARD ? (Card) source : null;
		if (!spellDesc.hasPredefinedTarget() && targets != null && targets.size() == 1) {
			if (chosenCard.getTargetRequirement() != TargetSelection.NONE) {
				context.getEnvironment().remove(Environment.TARGET_OVERRIDE);
				context.getEnvironment().put(Environment.CHOOSE_ONE_CARD, chosenCard.getCardId());
				GameEvent spellTargetEvent = new TargetAcquisitionEvent(context, playerId, ActionType.SPELL, chosenCard, targets.get(0));
				context.fireGameEvent(spellTargetEvent);
				Entity targetOverride = context
						.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.TARGET_OVERRIDE));
				if (targetOverride != null && targetOverride.getId() != IdFactory.UNASSIGNED) {
					targets.remove(0);
					targets.add(targetOverride);
					spellDesc = spellDesc.addArg(SpellArg.FILTER, null);
					log("Target for spell {} has been changed! New target {}", chosenCard, targets.get(0));
				}
			}
		}
		try {
			Spell spell = spellFactory.getSpell(spellDesc);
			spell.cast(context, player, spellDesc, source, targets);
		} catch (Exception e) {
			if (source != null) {
				logger.error("Error while playing card: " + source.getName());
			}
			logger.error("Error while casting spell: " + spellDesc);
			panicDump();
			e.printStackTrace();
		}

		context.getEnvironment().remove(Environment.TARGET_OVERRIDE);
		context.getEnvironment().remove(Environment.CHOOSE_ONE_CARD);

		checkForDeadEntities();
		if (targets == null || targets.size() != 1) {
			context.fireGameEvent(new AfterSpellCastedEvent(context, playerId, sourceCard, null));
		} else {
			context.fireGameEvent(new AfterSpellCastedEvent(context, playerId, sourceCard, targets.get(0)));
		}
	}

	public void castSpell(int playerId, SpellDesc spellDesc, EntityReference sourceReference, EntityReference targetReference,
			boolean childSpell) {
		castSpell(playerId, spellDesc, sourceReference, targetReference, TargetSelection.NONE, childSpell);
	}

	public void castSpell(int playerId, SpellDesc spellDesc, EntityReference sourceReference, EntityReference targetReference,
			TargetSelection targetSelection, boolean childSpell) {
		Player player = context.getPlayer(playerId);
		Entity source = null;
		if (sourceReference != null) {
			try {
				source = context.resolveSingleTarget(sourceReference);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Error resolving source entity while casting spell: " + spellDesc);
			}

		}
		//SpellCard spellCard = null;
		EntityReference spellTarget = spellDesc.hasPredefinedTarget() ? spellDesc.getTarget() : targetReference;
		List<Entity> targets = targetLogic.resolveTargetKey(context, player, source, spellTarget);
		// target can only be changed when there is one target
		// note: this code block is basically exclusively for the SpellBender
		// Secret, but it can easily be expanded if targets of area of effect
		// spell should be changeable as well
		Card sourceCard = null;
		if (source != null) {
			sourceCard = source.getEntityType() == EntityType.CARD ? (Card) source : null;
		}
		if (sourceCard != null && sourceCard.getCardType().isCardType(CardType.SPELL) && !spellDesc.hasPredefinedTarget() && targets != null
				&& targets.size() == 1) {
			if (sourceCard.getCardType().isCardType(CardType.SPELL) && targetSelection != TargetSelection.NONE && !childSpell) {
				GameEvent spellTargetEvent = new TargetAcquisitionEvent(context, playerId, ActionType.SPELL, sourceCard, targets.get(0));
				context.fireGameEvent(spellTargetEvent);
				Entity targetOverride = context
						.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.TARGET_OVERRIDE));
				if (targetOverride != null && targetOverride.getId() != IdFactory.UNASSIGNED) {
					targets.remove(0);
					targets.add(targetOverride);
					spellDesc = spellDesc.addArg(SpellArg.FILTER, null);
					log("Target for spell {} has been changed! New target {}", sourceCard, targets.get(0));
				}
			}

		}
		try {
			Spell spell = spellFactory.getSpell(spellDesc);
			spell.cast(context, player, spellDesc, source, targets);
		} catch (Exception e) {
			if (source != null) {
				logger.error("Error while playing card: " + source.getName());
			}
			logger.error("Error while casting spell: " + spellDesc);
			panicDump();
			e.printStackTrace();
		}

		if (sourceCard != null && sourceCard.getCardType().isCardType(CardType.SPELL) && !childSpell) {
			context.getEnvironment().remove(Environment.TARGET_OVERRIDE);

			checkForDeadEntities();
			if (targets == null || targets.size() != 1) {
				context.fireGameEvent(new AfterSpellCastedEvent(context, playerId, sourceCard, null));
			} else {
				context.fireGameEvent(new AfterSpellCastedEvent(context, playerId, sourceCard, targets.get(0)));
			}
		}
	}

	public void changeHero(Player player, Hero hero) {
		hero.setId(player.getHero().getId());
		if (hero.getHeroClass() == null || hero.getHeroClass() == NonHeroClass.NEUTRAL) {
			hero.setHeroClass(player.getHero().getHeroClass());
		}

		log("{}'s hero has been changed to {}", player.getName(), hero);
		hero.setOwner(player.getId());
		hero.setWeapon(player.getHero().getWeapon());
		player.setHero(hero);
		refreshAttacksPerRound(hero);
	}

	public void changeHero(Player player, Hero hero, HeroCard heroCard, boolean resolveBattlecry) {
		hero.setId(player.getHero().getId());
		if (hero.getHeroClass() == null || hero.getHeroClass() == NonHeroClass.NEUTRAL) {
			hero.setHeroClass(player.getHero().getHeroClass());
		}

		log("{}'s hero has been changed to {}", player.getName(), hero);
		hero.setOwner(player.getId());
		hero.setWeapon(player.getHero().getWeapon());
		player.setHero(hero);
		if (resolveBattlecry && hero.getBattlecry() != null) {
			resolveBattlecry(player.getId(), hero);
		}
	}

	public void checkForDeadEntities() {
		checkForDeadEntities(0);
	}

	/**
	 * Checks all player minions and weapons for destroyed actors and proceeds
	 * with the removal in correct order
	 */
	public void checkForDeadEntities(int i) {
		// sanity check, this method should never call itself that often
		if (i > 20) {
			panicDump();
			throw new RuntimeException("Infinite death checking loop");
		}

		List<Actor> destroyList = new ArrayList<>();
		for (Player player : context.getPlayers()) {

			if (player.getHero().isDestroyed()) {
				destroyList.add(player.getHero());
			}

			for (Summon summon : player.getSummons()) {
				if (summon.isDestroyed()) {
					destroyList.add(summon);
				}
			}
			if (player.getHero().getWeapon() != null && player.getHero().getWeapon().isDestroyed()) {
				destroyList.add(player.getHero().getWeapon());
			}
		}

		if (destroyList.isEmpty()) {
			return;
		}

		// sort the destroyed actors by their id. This implies that actors with a lower id entered the game ealier than those with higher ids!
		Collections.sort(destroyList, (a1, a2) -> Integer.compare(a1.getId(), a2.getId()));
		// this method performs the actual removal
		destroy(destroyList.toArray(new Actor[0]));
		if (context.gameDecided()) {
			return;
		}
		// deathrattles have been resolved, which may lead to other actors being destroyed now, so we need to check again
		checkForDeadEntities(i + 1);
	}

	@Override
	public GameLogic clone() {
		GameLogic clone = new GameLogic(idFactory.clone());
		clone.debugHistory = new LinkedList<>(debugHistory);
		return clone;
	}

	public int damage(Player player, Actor target, int baseDamage, Entity source) {
		return damage(player, target, baseDamage, source, false);
	}

	public int damage(Player player, Actor target, int baseDamage, Entity source, boolean ignoreSpellDamage) {
		// sanity check to prevent StackOverFlowError with Mistress of Pain +
		// Auchenai Soulpriest
		if (target.getHp() < -100) {
			return 0;
		}
		int damage = baseDamage;
		Card sourceCard = source != null && source.getEntityType() == EntityType.CARD ? (Card) source : null;
		if (!ignoreSpellDamage && sourceCard != null) {
			if (sourceCard.getCardType().isCardType(CardType.SPELL)) {
				damage = applySpellpower(player, source, baseDamage);
			} else if (sourceCard.getCardType().isCardType(CardType.HERO_POWER)) {
				damage = applyHeroPowerDamage(player, damage);
			}
			if (sourceCard.getCardType().isCardType(CardType.SPELL) || sourceCard.getCardType().isCardType(CardType.HERO_POWER)) {
				damage = applyAmplify(player, damage, Attribute.SPELL_AMPLIFY_MULTIPLIER);
			}
		}
		int damageDealt = 0;
		if (hasEntityAttribute(target, Attribute.TAKE_DOUBLE_DAMAGE)) {
			damage *= 2;
		}
		context.getDamageStack().push(damage);
		context.fireGameEvent(new PreDamageEvent(context, target, source));
		damage = context.getDamageStack().pop();
		if (damage > 0) {
			source.removeAttribute(Attribute.STEALTH);
            source.removeAttribute(Attribute.STEALTH_FOR_ONE_TURN);
		}
		switch (target.getEntityType()) {
		case MINION:
			damageDealt = damageMinion((Actor) target, damage);
			if (damageDealt > 0 && source != null && hasEntityAttribute(source, Attribute.POISONOUS)) {
				markAsDestroyed(target);
			}
			break;
		case HERO:
			damageDealt = damageHero((Hero) target, damage);
			break;
		default:
			break;
		}

		target.setAttribute(Attribute.LAST_HIT, damageDealt);
		if (damageDealt > 0) {
			DamageEvent damageEvent = new DamageEvent(context, target, source, damageDealt);
			context.fireGameEvent(damageEvent);
			player.getStatistics().damageDealt(damageDealt);

			if (source != null && hasEntityAttribute(source, Attribute.LIFESTEAL)) {
				heal(player, context.getPlayer(source.getOwner()).getHero(), damageDealt, source);
            }

            target.modifyAttribute(Attribute.DAMAGE_THIS_TURN, damageDealt);
		}

		return damageDealt;
	}

	private int damageHero(Hero hero, int damage) {
		if (hasEntityAttribute(hero, Attribute.IMMUNE) || hasAttribute(context.getPlayer(hero.getOwner()), Attribute.IMMUNE_HERO)) {
			log("{} is IMMUNE and does not take damage", hero);
			return 0;
		}
		int effectiveHp = hero.getEffectiveHp();
		hero.modifyArmor(-damage);
		int newHp = Math.min(hero.getHp(), effectiveHp - damage);
		hero.setHp(newHp);
		log(hero.getName() + " receives " + damage + " damage, hp now: " + hero.getHp() + "(" + hero.getArmor() + ")");
		return damage;
	}

	private int damageMinion(Actor minion, int damage) {
		if (hasEntityAttribute(minion, Attribute.DIVINE_SHIELD)) {
			removeAttribute(minion, Attribute.DIVINE_SHIELD);
			log("{}'s DIVINE SHIELD absorbs the damage", minion);
			return 0;
		}
		if (hasEntityAttribute(minion, Attribute.IMMUNE)) {
			log("{} is IMMUNE and does not take damage", minion);
			return 0;
		}
		if (damage >= minion.getHp() && hasEntityAttribute(minion, Attribute.CANNOT_REDUCE_HP_BELOW_1)) {
			damage = minion.getHp() - 1;
		}

		log("{} is damaged for {}", minion, damage);
		minion.setHp(minion.getHp() - damage);
		handleEnrage(minion);
		return damage;
	}

	public void destroy(Actor... targets) {
		int[] boardPositions = new int[targets.length];

		for (int i = 0; i < targets.length; i++) {
			Actor target = targets[i];
			removeSpellTriggers(target, false);
			Player owner = context.getPlayer(target.getOwner());
			context.getPlayer(target.getOwner()).getGraveyard().add(target);

			int boardPosition = owner.getSummons().indexOf(target);
			boardPositions[i] = boardPosition;
		}

		for (int i = 0; i < targets.length; i++) {
			Actor target = targets[i];
			log("{} is destroyed", target);
			Player owner = context.getPlayer(target.getOwner());
			owner.getSummons().remove(target);
		}

		for (int i = 0; i < targets.length; i++) {
			Actor target = targets[i];
			Player owner = context.getPlayer(target.getOwner());
			switch (target.getEntityType()) {
			case HERO:
				log("Hero {} has been destroyed.", target.getName());
				applyAttribute(target, Attribute.DESTROYED);
				applyAttribute(context.getPlayer(target.getOwner()), Attribute.DESTROYED);
				break;
			case MINION:
				destroyMinion((Minion) target);
				break;
			case WEAPON:
				destroyWeapon((Weapon) target);
				break;
			case ANY:
			default:
				logger.error("Trying to destroy unknown entity type {}", target.getEntityType());
				break;
			}

			resolveDeathrattles(owner, target, boardPositions[i]);
		}
		
		for (Actor target : targets) {
			removeSpellTriggers(target, true);
		}

		context.fireGameEvent(new BoardChangedEvent(context));
	}

	private void destroyMinion(Minion minion) {
		context.getEnvironment().put(Environment.KILLED_MINION, minion.getReference());
		KillEvent killEvent = new KillEvent(context, minion);
		context.fireGameEvent(killEvent);
		context.getEnvironment().remove(Environment.KILLED_MINION);

		applyAttribute(minion, Attribute.DESTROYED);
		minion.setBaseAttribute(Attribute.DIED_ON_TURN, context.getTurn());
	}

	private void destroyWeapon(Weapon weapon) {
		Player owner = context.getPlayer(weapon.getOwner());
		// resolveDeathrattles(owner, weapon);
		if (owner.getHero().getWeapon() != null && owner.getHero().getWeapon().getId() == weapon.getId()) {
			owner.getHero().setWeapon(null);
		}
		context.fireGameEvent(new WeaponDestroyedEvent(context, weapon));
	}

	public int determineBeginner(int... playerIds) {
		return ThreadLocalRandom.current().nextBoolean() ? playerIds[0] : playerIds[1];
	}

	public void discardCard(Player player, Card card) {
		logger.debug("{} discards {}", player.getName(), card);
		// only a 'real' discard should fire a DiscardEvent
		if (card.getLocation() == CardLocation.HAND) {
			context.fireGameEvent(new DiscardEvent(context, player.getId(), card));
		}

		removeCard(player.getId(), card);
	}

	public Card drawCard(int playerId, Entity source) {
		Player player = context.getPlayer(playerId);
		CardCollection deck = player.getDeck();
		if (deck.isEmpty()) {
			Hero hero = player.getHero();
			int fatigue = hasEntityAttribute(player, Attribute.FATIGUE) ? player.getAttributeValue(Attribute.FATIGUE) : 0;
			fatigue++;
			player.setAttribute(Attribute.FATIGUE, fatigue);
			damage(player, hero, fatigue, hero);
			log("{}'s deck is empty, taking {} fatigue damage!", player.getName(), fatigue);
			player.getStatistics().fatigueDamage(fatigue);
			return null;
		}

		Card card = deck.getRandom();
		return drawCard(playerId, card, source);
	}

	public Card drawCard(int playerId, Card card, Entity source) {
		Player player = context.getPlayer(playerId);
		player.getStatistics().cardDrawn();
		player.getDeck().remove(card);
		receiveCard(playerId, card, source, true);
		return card;
	}

	public void drawSetAsideCard(int playerId, Card card) {
		if (card.getId() == IdFactory.UNASSIGNED) {
			card.setId(idFactory.generateId());
		}
		card.setOwner(playerId);
		Player player = context.getPlayer(playerId);
		player.getSetAsideZone().add(card);
	}

	public void echoCard(Card card) {
	    card.clone();
    }

	public void endTurnForEntity(Entity entity) {
        entity.removeAttribute(Attribute.DAMAGE_THIS_TURN);
        entity.removeAttribute(Attribute.EXTRA_ATTACKS);
        entity.removeAttribute(Attribute.HERO_POWER_USAGES);
        entity.removeAttribute(Attribute.SUMMONING_SICKNESS);
        entity.removeAttribute(Attribute.TEMPORARY_ATTACK_BONUS);
    }

    public int endTurn(int playerId) {
        for (Player player : context.getPlayers()) {
            player.removeAttribute(Attribute.COMBO);

            Hero hero = player.getHero();
            endTurnForEntity(hero);
            if (context.getActivePlayer() == player) {
                handleFrozen(hero);
                hero.activateWeapon(false);

                log("{} ends their turn.", player.getName());
            }

            for (Summon summon : player.getSummons()) {
                endTurnForEntity(summon);
                if (context.getActivePlayer() == player) {
                    handleFrozen(summon);
                }
            }
        }

        context.fireGameEvent(new TurnEndEvent(context, playerId));
        for (Iterator<CardCostModifier> iterator = context.getCardCostModifiers().iterator(); iterator.hasNext();) {
            CardCostModifier cardCostModifier = iterator.next();
            if (cardCostModifier.isExpired()) {
                iterator.remove();
            }
        }
        for (Iterator<Enchantment> iterator = context.getEnchantments().iterator(); iterator.hasNext();) {
            Enchantment enchantment = iterator.next();
            if (enchantment.isExpired()) {
                iterator.remove();
            }
        }
        checkForDeadEntities();
        removeGhosts(playerId);

        Player currentPlayer = context.getPlayer(playerId);

        if (currentPlayer.hasAttribute(Attribute.EXTRA_TURNS)
                && !currentPlayer.hasAttribute(Attribute.FORCE_END_TURN)) {
            currentPlayer.modifyAttribute(Attribute.EXTRA_TURNS, -1);
            return playerId;
        }

        currentPlayer.removeAttribute(Attribute.FORCE_END_TURN);
        return context.getOpponent(context.getPlayer(playerId)).getId();
    }

	public void equipWeapon(int playerId, Weapon weapon) {
		equipWeapon(playerId, weapon, false);
	}

	public void equipWeapon(int playerId, Weapon weapon, boolean battlecry) {
		Player player = context.getPlayer(playerId);

		weapon.setId(idFactory.generateId());
		Weapon currentWeapon = player.getHero().getWeapon();
		
		if (currentWeapon != null) {
			player.getSetAsideZone().add(currentWeapon);
		}

		log("{} equips weapon {}", player.getHero(), weapon);
		player.getHero().setWeapon(weapon);

		if (battlecry && weapon.getBattlecry() != null) {
			resolveBattlecry(playerId, weapon);
		}
		
		if (currentWeapon != null) {
			log("{} discards currently equipped weapon {}", player.getHero(), currentWeapon);
			destroy(currentWeapon);
			player.getSetAsideZone().remove(currentWeapon);
		}
		
		player.getStatistics().equipWeapon(weapon);
		weapon.setActive(context.getActivePlayerId() == playerId);
		if (weapon.hasSpellTrigger()) {
			for (SpellTrigger spellTrigger : weapon.getSpellTriggers()) {
				addGameEventListener(player, spellTrigger, weapon);
			}
		}
		if (weapon.getCardCostModifier() != null) {
			addManaModifier(player, weapon.getCardCostModifier(), weapon);
		}
		checkForDeadEntities();
		context.fireGameEvent(new WeaponEquippedEvent(context, weapon));
		context.fireGameEvent(new BoardChangedEvent(context));
	}

	public void fight(Player player, Actor attacker, Actor defender) {
		log("{} attacks {}", attacker, defender);

		context.getEnvironment().put(Environment.ATTACKER_REFERENCE, attacker.getReference());

		TargetAcquisitionEvent targetAcquisitionEvent = new TargetAcquisitionEvent(context, player.getId(), ActionType.PHYSICAL_ATTACK,
				attacker, defender);
		context.fireGameEvent(targetAcquisitionEvent);
		Actor target = defender;
		if (context.getEnvironment().containsKey(Environment.TARGET_OVERRIDE)) {
			target = (Actor) context.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.TARGET_OVERRIDE));
		}
		context.getEnvironment().remove(Environment.TARGET_OVERRIDE);

		// if (attacker.hasTag(GameTag.FUMBLE) && randomBool()) {
		// log("{} fumbled and hits another target", attacker);
		// target = getAnotherRandomTarget(player, attacker, defender,
		// EntityReference.ENEMY_CHARACTERS);
		// }

		if (target != defender) {
			log("Target of attack was changed! New Target: {}", target);
		}

		if (hasEntityAttribute(attacker, Attribute.IMMUNE_WHILE_ATTACKING)) {
			applyAttribute(attacker, Attribute.IMMUNE);
		}

		removeAttribute(attacker, Attribute.STEALTH);
        removeAttribute(attacker, Attribute.STEALTH_FOR_ONE_TURN);

		int attackerDamage = getEntityAttack(attacker);
		int defenderDamage = getEntityAttack(target);
		context.fireGameEvent(new PhysicalAttackEvent(context, attacker, target, attackerDamage));
		// secret may have killed attacker ADDENDUM: or defender
		if (attacker.isDestroyed() || target.isDestroyed()) {
			context.getEnvironment().remove(Environment.ATTACKER_REFERENCE);
			return;
		}

		if (target.getOwner() == -1) {
			logger.error("Target has no owner!! {}", target);
		}

		Player owningPlayer = context.getPlayer(target.getOwner());
		boolean damaged = damage(owningPlayer, target, attackerDamage, attacker) > 0;
		if (defenderDamage > 0) {
			damage(player, attacker, defenderDamage, target);
		}
		if (hasEntityAttribute(attacker, Attribute.IMMUNE_WHILE_ATTACKING)) {
			attacker.removeAttribute(Attribute.IMMUNE);
		}

		if (attacker.getEntityType() == EntityType.HERO) {
			Hero hero = (Hero) attacker;
			Weapon weapon = hero.getWeapon();
			if (weapon != null && weapon.isActive()) {
				modifyDurability(hero.getWeapon(), -1);
			}
		}
		attacker.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, -1);

		context.fireGameEvent(new AfterPhysicalAttackEvent(context, attacker, target, damaged ? attackerDamage : 0));

		context.getEnvironment().remove(Environment.ATTACKER_REFERENCE);
	}

	public void gainArmor(Player player, int armor) {
		logger.debug("{} gains {} armor", player.getHero(), armor);
		player.getHero().modifyArmor(armor);
		player.getStatistics().armorGained(armor);
		if (armor > 0) {
			context.fireGameEvent(new ArmorGainedEvent(context, player.getHero()));
		}
	}

	public String generateCardID() {
		return TEMP_CARD_LABEL + idFactory.generateId();
	}

	public Actor getAnotherRandomTarget(Player player, Actor attacker, Actor originalTarget, EntityReference potentialTargets) {
		List<Entity> validTargets = context.resolveTarget(player, null, potentialTargets);
		// cannot redirect to attacker
		validTargets.remove(attacker);
		// cannot redirect to original target
		validTargets.remove(originalTarget);
		if (validTargets.isEmpty()) {
			return originalTarget;
		}

		return (Actor) SpellUtils.getRandomTarget(validTargets);
	}

	/**
	 * Gets the {@link Attribute} object of the {@link Entity} after {@link Enchantment}s.
	 *
	 * @param entity
	 * @param attribute
	 * @return
	 */
	public Object getEntityAttribute(Entity entity, Attribute attribute) {
		switch (attribute) {
			case ATTACK:
				return getEntityAttack(entity);
			case HP:
				return entity.getBaseAttributeValue(Attribute.HP);
			case MAX_HP:
				return getEntityMaxHp(entity);
		}
		switch (GameTagUtils.getTagValueType(attribute)){
			case INTEGER:
				return getModifiedAttributeValue(entity, attribute);
			case BOOLEAN:
				return getModifiedAttributeBool(entity, attribute);
		}
		return null;
	}

	/**
	 * Gets the {@link Entity}'s Attack {@link Attribute} after {@link Enchantment}s.
	 *
	 * @param entity
	 * @return
	 */
	public int getEntityAttack(Entity entity) {
		if (entity instanceof Actor && hasEntityAttribute(entity, Attribute.ATTACK_EQUALS_HP)) {
			return ((Actor) entity).getHp();
		}
		int attack = getModifiedAttributeValue(entity, Attribute.ATTACK)
				+ getModifiedAttributeValue(entity, Attribute.AURA_ATTACK_BONUS)
				+ getModifiedAttributeValue(entity, Attribute.TEMPORARY_ATTACK_BONUS);
		if (entity instanceof Hero) {
			Hero hero = (Hero) entity;
			attack += getEntityAttack(hero.getWeapon());
		}
		return Math.max(0, attack);
	}

	/**
	 * Gets the {@link Entity}'s Max HP {@link Attribute} after {@link Enchantment}s.
	 *
	 * @param entity
	 * @return
	 */
	public int getEntityMaxHp(Entity entity) {
		int maxHp = getModifiedAttributeValue(entity, Attribute.MAX_HP)
				+ getModifiedAttributeValue(entity, Attribute.AURA_HP_BONUS);
		return Math.max(0, maxHp);
	}

	/**
	 * Returns the first value of the attribute encountered. This method should
	 * be used with caution, as the result is random if there are different
	 * values of the same attribute in play.
	 * 
	 * @param player
	 * @param attr
	 *            Which attribute to find
	 * @param defaultValue
	 *            The value returned if no occurrence of the attribute is found
	 * @return the first occurrence of the value of attribute or defaultValue
	 */
	public int getAttributeValue(Player player, Attribute attr, int defaultValue) {
		for (Summon summon : player.getSummons()) {
			if (hasEntityAttribute(summon, attr)) {
				return summon.getAttributeValue(attr);
			}
		}

		return defaultValue;
	}

	public GameAction getAutoHeroPowerAction(int playerId) {
		return actionLogic.getAutoHeroPower(context, context.getPlayer(playerId));
	}

	/**
	 * Return the greatest value of the attribute from all Actors of a Player.
	 * This method will return infinite if an Attribute value is negative, so
	 * use this method with caution.
	 * 
	 * @param player
	 *            Which Player to check
	 * @param attr
	 *            Which attribute to find
	 * @return The highest value from all sources. -1 is considered infinite.
	 */
	public int getGreatestAttributeValue(Player player, Attribute attr) {
		int greatest = Math.max(INFINITE, player.getHero().getAttributeValue(attr));
		if (greatest == INFINITE) {
			return greatest;
		}
		for (Summon summon : player.getSummons()) {
			if (hasEntityAttribute(summon, attr)) {
				if (summon.getAttributeValue(attr) > greatest) {
					greatest = summon.getAttributeValue(attr);
				}
				if (summon.getAttributeValue(attr) == INFINITE) {
					return INFINITE;
				}
			}
		}
		return greatest;
	}

	public MatchResult getMatchResult(Player player, Player opponent) {
		boolean playerLost = hasPlayerLost(player);
		boolean opponentLost = hasPlayerLost(opponent);
		if (playerLost && opponentLost) {
			return MatchResult.DOUBLE_LOSS;
		} else if (playerLost || opponentLost) {
			return MatchResult.WON;
		}
		return MatchResult.RUNNING;
	}

	public int getMaxNumberOfAttacks(Actor actor) {
		if (hasEntityAttribute(actor, Attribute.MEGA_WINDFURY)) {
			return MEGA_WINDFURY_ATTACKS;
		} else if (hasEntityAttribute(actor, Attribute.WINDFURY)) {
			return WINDFURY_ATTACKS;
		}
		return 1;
	}

	public int getModifiedManaCost(Player player, Card card) {
		int manaCost = card.getManaCost(context, player);
		int minValue = 0;
		for (CardCostModifier costModifier : context.getCardCostModifiers()) {
			if (!costModifier.appliesTo(card)) {
				continue;
			}
			manaCost = costModifier.process(context, player, card, manaCost);
			if (costModifier.getMinValue() > minValue) {
				minValue = costModifier.getMinValue();
			}
		}
		if (hasEntityAttribute(card, Attribute.MANA_COST_MODIFIER)) {
			manaCost += card.getAttributeValue(Attribute.MANA_COST_MODIFIER);
		}
		manaCost = MathUtils.clamp(manaCost, minValue, Integer.MAX_VALUE);
		return manaCost;
	}

	public int getModifiedAttributeValue(Entity entity, Attribute attribute) {
		if (entity == null) {
			return 0;
		}
		int attributeValue = entity.getAttributeValue(attribute);
		for (Enchantment enchantment : context.getEnchantments()) {
			if (enchantment.getAttribute() != attribute || !enchantment.appliesTo(entity)) {
				continue;
			}
			attributeValue = enchantment.process(attributeValue);
			// TODO: Set min value for Enchantment stuffs
			if (enchantment.getMinValue() > 0) {
				int minValue = enchantment.getMinValue();
				attributeValue = MathUtils.clamp(attributeValue, minValue, Integer.MAX_VALUE);
			}
		}
		return attributeValue;
	}

	public boolean getModifiedAttributeBool(Entity entity, Attribute attribute) {
		boolean attributeBool = entity.hasBaseAttribute(attribute);
		for (Enchantment enchantment : context.getEnchantments()) {
			if (enchantment.getAttribute() != attribute || !enchantment.appliesTo(entity)) {
				continue;
			}
			attributeBool = enchantment.process(attributeBool);
		}
		return attributeBool;
	}

	public List<IGameEventListener> getQuests(Player player) {
		List<IGameEventListener> quests = context.getTriggersAssociatedWith(player.getHero().getReference());
		for (Iterator<IGameEventListener> iterator = quests.iterator(); iterator.hasNext();) {
			IGameEventListener trigger = iterator.next();
			if (!(trigger instanceof Quest)) {
				iterator.remove();
			}
		}
		return quests;
	}

	public List<IGameEventListener> getSecrets(Player player) {
		List<IGameEventListener> secrets = context.getTriggersAssociatedWith(player.getHero().getReference());
		for (Iterator<IGameEventListener> iterator = secrets.iterator(); iterator.hasNext();) {
			IGameEventListener trigger = iterator.next();
			if (!(trigger instanceof Secret)) {
				iterator.remove();
			}
		}
		return secrets;
	}

	public int getTotalAttributeValue(Attribute attr) {
		int total = 0;
		for (Player player : context.getPlayers()) {
			total += getTotalAttributeValue(player, attr);
		}
		return total;
	}

	public int getTotalAttributeValue(Player player, Attribute attr) {
		int total = player.getHero().getAttributeValue(attr);
		for (Summon summon : player.getSummons()) {
			if (!hasEntityAttribute(summon, attr)) {
				continue;
			}

			total += summon.getAttributeValue(attr);
		}
		return total;
	}

	public int getTotalAttributeMultiplier(Player player, Attribute attribute) {
		int total = 1;
		if (hasEntityAttribute(player.getHero(), attribute)) {
			player.getHero().getAttributeValue(attribute);
		}
		for (Summon summon : player.getSummons()) {
			if (hasEntityAttribute(summon, attribute)) {
				total *= summon.getAttributeValue(attribute);
			}
		}
		return total;
	}

	public List<GameAction> getValidActions(int playerId) {
		Player player = context.getPlayer(playerId);
		return actionLogic.getValidActions(context, player);
	}

	public List<Entity> getValidTargets(int playerId, GameAction action) {
		Player player = context.getPlayer(playerId);
		return targetLogic.getValidTargets(context, player, action);
	}

	public Player getWinner(Player player, Player opponent) {
		boolean playerLost = hasPlayerLost(player);
		boolean opponentLost = hasPlayerLost(opponent);
		if (playerLost && opponentLost) {
			return null;
		} else if (opponentLost) {
			return player;
		} else if (playerLost) {
			return opponent;
		}
		return null;
	}

	private void handleEnrage(Actor entity) {
		if (!hasEntityAttribute(entity, Attribute.ENRAGABLE)) {
			return;
		}
		boolean enraged = entity.getHp() < getEntityMaxHp(entity);
		// enrage state has not changed; do nothing
		if (hasEntityAttribute(entity, Attribute.ENRAGED) == enraged) {
			return;
		}

		if (enraged) {
			log("{} is now enraged", entity);
			entity.setAttribute(Attribute.ENRAGED);
		} else {
			log("{} is no longer enraged", entity);
			entity.removeAttribute(Attribute.ENRAGED);
		}

		context.fireGameEvent(new EnrageChangedEvent(context, entity));
	}

	private void handleFrozen(Actor actor) {
		if (!hasEntityAttribute(actor, Attribute.FROZEN)) {
			return;
		}
		if (actor.getAttributeValue(Attribute.NUMBER_OF_ATTACKS) >= getMaxNumberOfAttacks(actor)) {
			removeAttribute(actor, Attribute.FROZEN);
		}
	}

	public boolean hasAttribute(Player player, Attribute attr) {
		if (hasEntityAttribute(player.getHero(), attr)) {
			return true;
		}
		for (Summon summon : player.getSummons()) {
			if (hasEntityAttribute(summon, attr) && !hasEntityAttribute(summon, Attribute.PENDING_DESTROY)) {
				return true;
			}
		}

		return false;
	}

	public boolean hasEntityAttribute(Entity entity, Attribute attribute) {
		switch (GameTagUtils.getTagValueType(attribute)){
			case INTEGER:
				return getModifiedAttributeValue(entity, attribute) != 0;
			case BOOLEAN:
				return getModifiedAttributeBool(entity, attribute);
		}

		return false;
	}

	public boolean hasAutoHeroPower(int player) {
		return actionLogic.hasAutoHeroPower(context, context.getPlayer(player));
	}

	public boolean hasCard(Player player, Card card) {
		for (Card heldCard : player.getHand()) {
			if (card.getCardId().equals(heldCard.getCardId())) {
				return true;
			}
		}
		if (player.getHero().getHeroPower().getCardId().equals(card.getCardId())) {
			return true;
		}
		return false;
	}

	public void heal(Player player, Actor target, int healing, Entity source) {
		if (hasAttribute(player, Attribute.INVERT_HEALING)) {
			log("All healing inverted, deal damage instead!");
			damage(player, target, healing, source);
			return;
		}
		if (source != null && source instanceof Card
				&& (((Card) source).getCardType().isCardType(CardType.SPELL)
				|| ((Card) source).getCardType().isCardType(CardType.HERO_POWER))) {
			healing = applyAmplify(player, healing, Attribute.HEAL_AMPLIFY_MULTIPLIER);
		}
		boolean success = false;
		switch (target.getEntityType()) {
		case MINION:
			success = healMinion((Actor) target, healing);
			break;
		case HERO:
			success = healHero((Hero) target, healing);
			break;
		default:
			break;
		}

		if (success) {
			HealEvent healEvent = new HealEvent(context, player.getId(), target, healing);
			context.fireGameEvent(healEvent);
			player.getStatistics().heal(healing);
		}
	}

	private boolean healHero(Hero hero, int healing) {
		int newHp = Math.min(getEntityMaxHp(hero), hero.getHp() + healing);
		int oldHp = hero.getHp();
		if (logger.isDebugEnabled()) {
			log(hero + " is healed for " + healing + ", hp now: " + newHp / getEntityMaxHp(hero));
		}

		hero.setHp(newHp);
		return newHp != oldHp;
	}

	private boolean healMinion(Actor minion, int healing) {
		int newHp = Math.min(getEntityMaxHp(minion), minion.getHp() + healing);
		int oldHp = minion.getHp();
		if (logger.isDebugEnabled()) {
			log(minion + " is healed for " + healing + ", hp now: " + newHp + "/" + getEntityMaxHp(minion));
		}

		minion.setHp(newHp);
		handleEnrage(minion);
		return newHp != oldHp;
	}

	public void init(int playerId, boolean begins) {
		Player player = context.getPlayer(playerId);
		player.getHero().setId(idFactory.generateId());
		player.getHero().setOwner(player.getId());
		player.getHero().setMaxHp(player.getHero().getBaseAttributeValue(Attribute.MAX_HP));
		player.getHero().setHp(player.getHero().getBaseAttributeValue(Attribute.MAX_HP));

		player.getHero().getHeroPower().setId(idFactory.generateId());
		assignCardIds(player.getDeck());
		assignCardIds(player.getHand());

		for (Card card : player.getCards()) {
			card.setBaseAttribute(Attribute.STARTED_IN_DECK);
		}

		log("Setting hero hp to {} for {}", player.getHero().getHp(), player.getName());

		player.getDeck().shuffle();

		mulligan(player, begins);

		for (Card card : player.getCards()) {
			if (card.getAttribute(Attribute.DECK_TRIGGER) != null) {
				TriggerDesc triggerDesc = (TriggerDesc) card.getAttribute(Attribute.DECK_TRIGGER);
				addGameEventListener(player, triggerDesc.create(), card);
			}
		}

		GameStartEvent gameStartEvent = new GameStartEvent(context, player.getId());
		context.fireGameEvent(gameStartEvent);
	}

	public boolean isLoggingEnabled() {
		return loggingEnabled;
	}

	public boolean isWounded(Actor actor) {
		return getEntityMaxHp(actor) > actor.getHp();
	}

    public JoustEvent joust(Player player) {
        return joust(player, CardType.MINION, true);
    }

    public JoustEvent joust(Player player, CardType cardType, boolean bothPlayers) {
        Card ownCard = player.getDeck().getRandomOfType(cardType);
        Card opponentCard = null;
        boolean won = false;
        // no minions left in deck - automatically loose joust
        if (ownCard == null) {
            won = false;
            log("Jousting LOST - no card of type {} left", cardType.toString());
        } else if (bothPlayers) {
            Player opponent = context.getOpponent(player);
            opponentCard = opponent.getDeck().getRandomOfType(cardType);
            // opponent has no minions left in deck - automatically win joust
            if (opponentCard == null) {
                won = true;
                log("Jousting WON - opponent has no card of type {} left", cardType.toString());
            } else {
                // both players have minion cards left, the initiator needs to
                // have the one with
                // higher mana cost to win the joust
                won = ownCard.getBaseManaCost() > opponentCard.getBaseManaCost();

                log("Jousting {} - {} vs. {}", won ? "WON" : "LOST", ownCard, opponentCard);
            }
        }
        JoustEvent joustEvent = new JoustEvent(context, player.getId(), won, ownCard, opponentCard);
        context.fireGameEvent(joustEvent);
        return joustEvent;
    }

	private void log(String message) {
		logToDebugHistory(message);
		if (isLoggingEnabled() && logger.isDebugEnabled()) {
			logger.debug(message);
		}
	}

	private void log(String message, Object param1) {
		logToDebugHistory(message, param1);
		if (isLoggingEnabled() && logger.isDebugEnabled()) {
			logger.debug(message, param1);
		}
	}

	private void log(String message, Object param1, Object param2) {
		logToDebugHistory(message, param1, param2);
		if (isLoggingEnabled() && logger.isDebugEnabled()) {
			logger.debug(message, param1, param2);
		}
	}

	private void log(String message, Object param1, Object param2, Object param3) {
		logToDebugHistory(message, param1, param2, param3);
		if (isLoggingEnabled() && logger.isDebugEnabled()) {
			logger.debug(message, param1, param2, param3);
		}
	}

	private void logToDebugHistory(String message, Object... params) {
		if (!BuildConfig.DEV_BUILD) {
			return;
		}
		if (debugHistory.size() == MAX_HISTORY_ENTRIES) {
			debugHistory.poll();
		}
		if (params != null && params.length > 0) {
			message = message.replaceAll("\\{\\}", "%s");
			message = String.format(message, params);
		}

		debugHistory.add(message);
	}

	public void markAsDestroyed(Actor target) {
		if (target != null) {
			applyAttribute(target, Attribute.DESTROYED);
		}
	}

	public boolean mergeSummons(Summon existingSummon, Summon summonToMerge) {
		Player player = context.getPlayer(existingSummon.getOwner());

		log("{} merges {} with {}", player.getName(), existingSummon, summonToMerge);

		context.fireGameEvent(new BoardChangedEvent(context));

		if (summonToMerge.hasSpellTrigger()) {
			for (SpellTrigger trigger : summonToMerge.getSpellTriggers()) {
				addGameEventListener(player, trigger, existingSummon);
			}
		}

		if (summonToMerge.getCardCostModifier() != null) {
			addManaModifier(player, summonToMerge.getCardCostModifier(), existingSummon);
		}

		if (summonToMerge instanceof Minion
				&& existingSummon instanceof Minion) {
			Minion existingMinion = (Minion) existingSummon;
			Minion minionToMerge = (Minion) summonToMerge;
			if (existingMinion != null) {
				existingMinion.modifyAttribute(Attribute.ATTACK, getEntityAttack(minionToMerge));
				existingMinion.modifyHpBonus(minionToMerge.getHp(), getEntityMaxHp(existingMinion));
			}
			handleEnrage(existingSummon);
		}
		context.fireGameEvent(new BoardChangedEvent(context));
		return true;
	}

	public void mindControl(Player player, Summon summon) {
		log("{} mind controls {}", player.getName(), summon);
		Player opponent = context.getOpponent(player);
		if (!opponent.getSummons().contains(summon)) {
			// logger.warn("Minion {} cannot be mind-controlled, because
			// opponent does not own it.", minion);
			return;
		}
		if (canSummonMoreMinions(player)) {
			context.getOpponent(player).getSummons().remove(summon);
			player.getSummons().add(summon);
			summon.setOwner(player.getId());
			applyAttribute(summon, Attribute.SUMMONING_SICKNESS);
			refreshAttacksPerRound(summon);
			List<IGameEventListener> triggers = context.getTriggersAssociatedWith(summon.getReference());
			removeSpellTriggers(summon);
			for (IGameEventListener trigger : triggers) {
				addGameEventListener(player, trigger, summon);
			}
			context.fireGameEvent(new BoardChangedEvent(context));
		} else {
			markAsDestroyed(summon);
		}
	}

	public void modifyCurrentMana(int playerId, int mana) {
		Player player = context.getPlayer(playerId);
		int newMana = Math.min(player.getMana() + mana, MAX_MANA);
		player.setMana(newMana);
	}

	public void modifyDurability(Weapon weapon, int durability) {
		log("Durability of weapon {} is changed by {}", weapon, durability);

		weapon.modifyAttribute(Attribute.HP, durability);
		if (durability > 0) {
			weapon.modifyAttribute(Attribute.MAX_HP, durability);
		}
	}

	@Deprecated
	public void modifyMaxHp(Actor actor, int value) {
		actor.setMaxHp(value);
		actor.setHp(value);
		handleEnrage(actor);
	}

	public void modifyMaxMana(Player player, int delta) {
		log("Maximum mana was changed by {} for {}", delta, player.getName());
		int maxMana = MathUtils.clamp(player.getMaxMana() + delta, 0, GameLogic.MAX_MANA);
		player.setMaxMana(maxMana);
		if (delta < 0 && player.getMana() > player.getMaxMana()) {
			player.setMana(player.getMaxMana());
		}
	}

	private void mulligan(Player player, boolean begins) {
		int numberOfStarterCards = begins ? STARTER_CARDS : STARTER_CARDS + 1;
		List<Card> starterCards = new ArrayList<>();
		for (int j = 0; j < numberOfStarterCards; j++) {
			Card randomCard = player.getDeck().getRandom();
			if (randomCard != null) {
				player.getDeck().remove(randomCard);
				log("Player {} been offered card {} for mulligan", player.getName(), randomCard);
				starterCards.add(randomCard);
			}
		}

		List<Card> discardedCards = player.getBehaviour().mulligan(context, player, starterCards);

		// remove player selected cards from starter cards
		for (Card discardedCard : discardedCards) {
			log("Player {} mulligans {} ", player.getName(), discardedCard);
			starterCards.remove(discardedCard);
		}

		// draw random cards from deck until required starter card count is
		// reached
		while (starterCards.size() < numberOfStarterCards) {
			Card randomCard = player.getDeck().getRandom();
			player.getDeck().remove(randomCard);
			starterCards.add(randomCard);
		}

		// put the mulligan cards back in the deck
		for (Card discardedCard : discardedCards) {
			player.getDeck().add(discardedCard);
		}

		for (Card starterCard : starterCards) {
			if (starterCard != null) {
				receiveCard(player.getId(), starterCard);
			}
		}

		// second player gets the coin additionally
		if (!begins) {
			Card theCoin = CardCatalogue.getCardById("spell_the_coin");
			receiveCard(player.getId(), theCoin);
		}
	}

	public void panicDump() {
		logger.error("=========PANIC DUMP=========");
		for (String entry : debugHistory) {
			logger.error(entry);
		}
	}

	public void performGameAction(int playerId, GameAction action) {
		debugHistory.add(action.toString());
		if (playerId != context.getActivePlayerId()) {
			logger.warn("Player {} tries to perform an action, but it is not his turn!", context.getPlayer(playerId).getName());
		}
		if (action.getTargetRequirement() != TargetSelection.NONE) {
			Entity target = context.resolveSingleTarget(action.getTargetKey());
			if (target != null) {
				context.getEnvironment().put(Environment.TARGET, target.getReference());
			} else {
				context.getEnvironment().put(Environment.TARGET, null);
			}
		}

		action.execute(context, playerId);

		context.getEnvironment().remove(Environment.TARGET);
		if (action.getActionType() != ActionType.BATTLECRY) {
			checkForDeadEntities();
		}
	}

	public void playCard(int playerId, CardReference cardReference) {
		Player player = context.getPlayer(playerId);
		Card card = context.resolveCardReference(cardReference);

		int modifiedManaCost = getModifiedManaCost(player, card);
		if (card.getCardType().isCardType(CardType.SPELL)
				&& hasEntityAttribute(player, Attribute.SPELLS_COST_HEALTH)) {
			context.getEnvironment().put(Environment.LAST_MANA_COST, 0);
			damage(player, player.getHero(), modifiedManaCost, card, true);
		} else if (card.isTribe(Tribe.MURLOC)
				&& hasEntityAttribute(player.getHero(), Attribute.MURLOCS_COST_HEALTH)) {
			context.getEnvironment().put(Environment.LAST_MANA_COST, 0);
			damage(player, player.getHero(), modifiedManaCost, card, true);
		} else {
			context.getEnvironment().put(Environment.LAST_MANA_COST, modifiedManaCost);
			modifyCurrentMana(playerId, -modifiedManaCost);
			player.getStatistics().manaSpent(modifiedManaCost);
		}
		log("{} plays {}", player.getName(), card);

		player.getStatistics().cardPlayed(card, context.getTurn());
		CardPlayedEvent cardPlayedEvent = new CardPlayedEvent(context, playerId, card);
		context.fireGameEvent(cardPlayedEvent);

		if (hasEntityAttribute(card, Attribute.OVERLOAD)) {
			context.fireGameEvent(new OverloadEvent(context, playerId, card));
		}

		removeCard(playerId, card);

		if ((card.getCardType().isCardType(CardType.SPELL))) {
			GameEvent spellCastedEvent = new SpellCastedEvent(context, playerId, card);
			context.fireGameEvent(spellCastedEvent);
			if (hasEntityAttribute(card, Attribute.COUNTERED)) {
				log("{} was countered!", card.getName());
				return;
			}
		}

		if (hasEntityAttribute(card, Attribute.OVERLOAD)) {
			player.modifyAttribute(Attribute.OVERLOAD, card.getAttributeValue(Attribute.OVERLOAD));
		}
	}

	public void playQuest(Player player, Quest quest) {
		playQuest(player, quest, true);
	}

	public void playQuest(Player player, Quest quest, boolean fromHand) {
		log("{} has a new quest activated: {}", player.getName(), quest.getSource());
		addGameEventListener(player, quest, player.getHero());
		player.getSecrets().add(quest.getSource().getCardId());
		player.getQuests().add(quest.getSource().getCardId());
		if (fromHand) {
			context.fireGameEvent(new QuestPlayedEvent(context, player.getId(), (QuestCard) quest.getSource()));
		}
	}

	public void playSecret(Player player, Secret secret) {
		playSecret(player, secret, true);
	}

	public void playSecret(Player player, Secret secret, boolean fromHand) {
		log("{} has a new secret activated: {}", player.getName(), secret.getSource());
		addGameEventListener(player, secret, player.getHero());
		player.getSecrets().add(secret.getSource().getCardId());
		if (fromHand) {
			context.fireGameEvent(new SecretPlayedEvent(context, player.getId(), (SecretCard) secret.getSource()));
		}
	}

	public void processTargetModifiers(Player player, GameAction action) {
		HeroPower heroPower = player.getHero().getHeroPower();
		// TODO: WTF IS THIS?!?!? This needs to be changed.
		if (heroPower.getHeroClass() != HeroClass.HUNTER) {
			return;
		}
		if (action.getActionType() == ActionType.HERO_POWER && hasAttribute(player, Attribute.HERO_POWER_CAN_TARGET_MINIONS)) {
			PlaySpellCardAction spellCardAction = (PlaySpellCardAction) action;
			SpellDesc targetChangedSpell = spellCardAction.getSpell().removeArg(SpellArg.TARGET);
			spellCardAction.setSpell(targetChangedSpell);
			spellCardAction.setTargetRequirement(TargetSelection.ANY);
		}
	}

	/**
	 * 
	 * @param max
	 *            Upper bound of random number (exclusive)
	 * @return Random number between 0 and max (exclusive)
	 */
	public int random(int max) {
		return ThreadLocalRandom.current().nextInt(max);
	}

	public boolean randomBool() {
		return ThreadLocalRandom.current().nextBoolean();
	}

	public void receiveCard(int playerId, Card card) {
		receiveCard(playerId, card, null);
	}

	public void receiveCard(int playerId, Card card, Entity source) {
		receiveCard(playerId, card, source, false);
	}

	public void receiveCard(int playerId, Card card, Entity source, boolean drawn) {
		Player player = context.getPlayer(playerId);
		if (card.getId() == IdFactory.UNASSIGNED) {
			card.setId(idFactory.generateId());
		}

		card.setOwner(playerId);
		CardCollection hand = player.getHand();

		if (hand.getCount() < MAX_HAND_CARDS) {
			if (card.getAttribute(Attribute.PASSIVE_TRIGGER) != null) {
				TriggerDesc triggerDesc = (TriggerDesc) card.getAttribute(Attribute.PASSIVE_TRIGGER);
				addGameEventListener(player, triggerDesc.create(), card);
			}
			
			log("{} receives card {}", player.getName(), card);
			hand.add(card);
			card.setLocation(CardLocation.HAND);
			CardType sourceType = null;
			if (source instanceof Card) {
				Card sourceCard = (Card) source;
				sourceType = sourceCard.getCardType();
			}
			context.fireGameEvent(new DrawCardEvent(context, playerId, card, sourceType, drawn));
		} else {
			log("{} has too many cards on his hand, card destroyed: {}", player.getName(), card);
			discardCard(player, card);
		}
	}

	public void refreshAttacksPerRound(Entity entity) {
		int attacks = 1;
		if (hasEntityAttribute(entity, Attribute.MEGA_WINDFURY)) {
			attacks = MEGA_WINDFURY_ATTACKS;
		} else if (hasEntityAttribute(entity, Attribute.WINDFURY)) {
			attacks = WINDFURY_ATTACKS;
		}
		entity.setAttribute(Attribute.NUMBER_OF_ATTACKS, attacks);
	}

	public void removeAttribute(Entity entity, Attribute attr) {
		if (!hasEntityAttribute(entity, attr)) {
			return;
		}
		if (attr == Attribute.MEGA_WINDFURY && hasEntityAttribute(entity, Attribute.WINDFURY)) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, WINDFURY_ATTACKS - MEGA_WINDFURY_ATTACKS);
		}
		if (attr == Attribute.WINDFURY && !hasEntityAttribute(entity, Attribute.MEGA_WINDFURY)) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, 1 - WINDFURY_ATTACKS);
		} else if (attr == Attribute.MEGA_WINDFURY) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, 1 - MEGA_WINDFURY_ATTACKS);
		}
		entity.removeAttribute(attr);
		log("Removing attribute {} from {}", attr, entity);
	}

	public void removeCard(int playerId, Card card) {
		Player player = context.getPlayer(playerId);
		log("Card {} has been moved from the HAND to the GRAVEYARD", card);
		card.setLocation(CardLocation.GRAVEYARD);
		removeSpellTriggers(card);
		player.getHand().remove(card);
		player.getGraveyard().add(card);
	}

	public void removeAllCards(int playerId) {
		for (Card card : context.getPlayer(playerId).getHand().toList()) {
			removeCard(playerId, card);
		}
	}

	public void removeCardFromDeck(int playerID, Card card) {
		Player player = context.getPlayer(playerID);
		log("Card {} has been moved from the DECK to the GRAVEYARD", card);
		card.setLocation(CardLocation.GRAVEYARD);
		removeSpellTriggers(card);
		player.getDeck().remove(card);
		player.getGraveyard().add(card);
	}

	public void removeGhosts(int playerId) {
		Player player = context.getPlayer(playerId);
		List<Card> cardList = new ArrayList<Card>(player.getHand().toList());
		for (Card card : cardList) {
			if (hasEntityAttribute(card, Attribute.GHOSTLY)) {
				discardCard(player, card);
			}
		}
	}

	public void removeQuests(Player player) {
		log("All quests for {} have been destroyed", player.getName());
		// This actually works amazingly
		for (IGameEventListener quest : getQuests(player)) {
			quest.onRemove(context);
			context.removeTrigger(quest);
		}
		player.getSecrets().removeAll(player.getQuests());
		player.getQuests().clear();
	}

	public void removeSummon(Summon summon, boolean peacefully) {
		removeSpellTriggers(summon);

		log("{} was removed", summon);

		applyAttribute(summon, Attribute.DESTROYED);

		Player owner = context.getPlayer(summon.getOwner());
		owner.getSummons().remove(summon);
		if (peacefully) {
			owner.getSetAsideZone().add(summon);
		} else {
			owner.getGraveyard().add(summon);
		}
		context.fireGameEvent(new BoardChangedEvent(context));
	}

	public void removeSecrets(Player player) {
		log("All secrets for {} have been destroyed", player.getName());
		// this only works while Secrets are the only SpellTrigger on the heroes
		// Web - Lol, it works now.
		for (IGameEventListener secret : getSecrets(player)) {
			secret.onRemove(context);
			context.removeTrigger(secret);
		}
		player.getSecrets().clear();
		player.getSecrets().addAll(player.getQuests());
	}

	private void removeSpellTriggers(Entity entity) {
		removeSpellTriggers(entity, true);
	}

	private void removeSpellTriggers(Entity entity, boolean removeAuras) {
		EntityReference entityReference = entity.getReference();
		for (IGameEventListener trigger : context.getTriggersAssociatedWith(entityReference)) {
			if (!removeAuras && trigger instanceof Aura) {
				continue;
			}
			log("SpellTrigger {} was removed for {}", trigger, entity);
			trigger.onRemove(context);
		}
		context.removeTriggersAssociatedWith(entityReference, removeAuras);
		for (Iterator<CardCostModifier> iterator = context.getCardCostModifiers().iterator(); iterator.hasNext();) {
			CardCostModifier cardCostModifier = iterator.next();
			if (cardCostModifier.getHostReference().equals(entityReference)) {
				iterator.remove();
			}
		}
		for (Iterator<Enchantment> iterator = context.getEnchantments().iterator(); iterator.hasNext();) {
			Enchantment enchantment = iterator.next();
			if (enchantment.getHostReference() != null && enchantment.getHostReference().equals(entityReference)) {
				iterator.remove();
			}
		}
	}

	public void replaceCard(int playerId, Card oldCard, Card newCard) {
		Player player = context.getPlayer(playerId);
		if (newCard.getId() == IdFactory.UNASSIGNED) {
			newCard.setId(idFactory.generateId());
		}
		
		if (!player.getHand().contains(oldCard)) {
			return;
		}

		newCard.setOwner(playerId);
		CardCollection hand = player.getHand();

		if (newCard.getAttribute(Attribute.PASSIVE_TRIGGER) != null) {
			TriggerDesc triggerDesc = (TriggerDesc) newCard.getAttribute(Attribute.PASSIVE_TRIGGER);
			addGameEventListener(player, triggerDesc.create(), newCard);
		}

		log("{} replaces card {} with card {}", player.getName(), oldCard, newCard);
		hand.replace(oldCard, newCard);
		removeCard(playerId, oldCard);
		newCard.setLocation(CardLocation.HAND);
		context.fireGameEvent(new DrawCardEvent(context, playerId, newCard, null, false));
	}
	
	public void replaceCardInDeck(int playerId, Card oldCard, Card newCard) {
		Player player = context.getPlayer(playerId);
		if (newCard.getId() == IdFactory.UNASSIGNED) {
			newCard.setId(idFactory.generateId());
		}
		
		if (!player.getDeck().contains(oldCard)) {
			return;
		}

		newCard.setOwner(playerId);
		CardCollection deck = player.getDeck();

		if (newCard.getAttribute(Attribute.DECK_TRIGGER) != null) {
			TriggerDesc triggerDesc = (TriggerDesc) newCard.getAttribute(Attribute.DECK_TRIGGER);
			addGameEventListener(player, triggerDesc.create(), newCard);
		}

		log("{} replaces card {} with card {}", player.getName(), oldCard, newCard);
		deck.replace(oldCard, newCard);
		removeCardFromDeck(playerId, oldCard);
		newCard.setLocation(CardLocation.DECK);
	}

	private void resolveBattlecry(int playerId, Actor actor) {
		BattlecryAction battlecry = actor.getBattlecry();
		Player player = context.getPlayer(playerId);
		if (!battlecry.canBeExecuted(context, player)) {
			return;
		}

		GameAction battlecryAction = null;
		battlecry.setSource(actor.getReference());
		if (battlecry.getTargetRequirement() != TargetSelection.NONE) {
			List<Entity> validTargets = targetLogic.getValidTargets(context, player, battlecry);
			if (validTargets.isEmpty()) {
				return;
			}

			List<GameAction> battlecryActions = new ArrayList<>();
			for (Entity validTarget : validTargets) {
				GameAction targetedBattlecry = battlecry.clone();
				targetedBattlecry.setTarget(validTarget);
				battlecryActions.add(targetedBattlecry);
			}
			
			if (attributeExists(Attribute.ALL_RANDOM_FINAL_DESTINATION)) {
				battlecryAction = battlecryActions.get(random(battlecryActions.size()));
			} else {
				battlecryAction = player.getBehaviour().requestAction(context, player, battlecryActions);
			}
		} else {
			battlecryAction = battlecry;
		}
		if (hasAttribute(player, Attribute.DOUBLE_BATTLECRIES) && actor.getSourceCard().hasAttribute(Attribute.BATTLECRY)) {
			// You need DOUBLE_BATTLECRIES before your battlecry action, not after.
			performGameAction(playerId, battlecryAction);
			if (!battlecry.canBeExecuted(context, player)) {
				return;
			}
			performGameAction(playerId, battlecryAction);
		} else {
			performGameAction(playerId, battlecryAction);
		}
	}

	public void resolveDeathrattles(Player player, Actor actor) {
		resolveDeathrattles(player, actor, -1);
	}

	public void resolveDeathrattles(Player player, Actor actor, int boardPosition) {
		if (!hasEntityAttribute(actor, Attribute.DEATHRATTLES)) {
			return;
		}
		if (boardPosition == -1) {
			player.getSummons().indexOf(actor);
		}
		boolean doubleDeathrattles = hasAttribute(player, Attribute.DOUBLE_DEATHRATTLES);
		EntityReference sourceReference = actor.getReference();
		for (SpellDesc deathrattleTemplate : actor.getDeathrattles()) {
			SpellDesc deathrattle = deathrattleTemplate.addArg(SpellArg.BOARD_POSITION_ABSOLUTE, boardPosition);
			castSpell(player.getId(), deathrattle, sourceReference, EntityReference.NONE, false);
			if (doubleDeathrattles) {
				castSpell(player.getId(), deathrattle, sourceReference, EntityReference.NONE, false);
			}
		}
	}

	public void questTriggered(Player player, Quest quest) {
		log("Quest was trigged: {}", quest.getSource());
		player.getSecrets().remove(quest.getSource().getCardId());
		player.getQuests().remove(quest.getSource().getCardId());
		context.fireGameEvent(new QuestSuccessfulEvent(context, (QuestCard) quest.getSource(), player.getId()));
	}

	public void secretTriggered(Player player, Secret secret) {
		log("Secret was trigged: {}", secret.getSource());
		player.getSecrets().remove(secret.getSource().getCardId());
		context.fireGameEvent(new SecretRevealedEvent(context, (SecretCard) secret.getSource(), player.getId()));
	}

	// TODO: circular dependency. Very ugly, refactor!
	public void setContext(GameContext context) {
		this.context = context;
	}

	public void setLoggingEnabled(boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}

	public void shuffleToDeck(Player player, Card card) {
		if (card.getId() == IdFactory.UNASSIGNED) {
			card.setId(idFactory.generateId());
		}
		card.setLocation(CardLocation.DECK);

		if (player.getDeck().getCount() < MAX_DECK_SIZE) {
			player.getDeck().addRandomly(card);
			
			if (card.getAttribute(Attribute.DECK_TRIGGER) != null) {
				TriggerDesc triggerDesc = (TriggerDesc) card.getAttribute(Attribute.DECK_TRIGGER);
				addGameEventListener(player, triggerDesc.create(), card);
			}
			log("Card {} has been shuffled to {}'s deck", card, player.getName());
		}
	}

	public void silence(int playerId, Minion target) {
		context.fireGameEvent(new SilenceEvent(context, playerId, target));
		final HashSet<Attribute> immuneToSilence = new HashSet<Attribute>();
		immuneToSilence.add(Attribute.HP);
		immuneToSilence.add(Attribute.MAX_HP);
		immuneToSilence.add(Attribute.ATTACK);
		immuneToSilence.add(Attribute.SUMMONING_SICKNESS);
		immuneToSilence.add(Attribute.AURA_ATTACK_BONUS);
		immuneToSilence.add(Attribute.AURA_HP_BONUS);
		immuneToSilence.add(Attribute.AURA_UNTARGETABLE_BY_SPELLS);
		immuneToSilence.add(Attribute.TRIBE);
		immuneToSilence.add(Attribute.NUMBER_OF_ATTACKS);

		List<Attribute> tags = new ArrayList<Attribute>();
		tags.addAll(target.getAttributes().keySet());
		for (Attribute attr : tags) {
			if (immuneToSilence.contains(attr)) {
				continue;
			}
			removeAttribute(target, attr);
		}
		removeSpellTriggers(target);

		int oldMaxHp = getEntityMaxHp(target);
		// TODO: Get Enchantments that are attached and remove them.
		if (target.getHp() > getEntityMaxHp(target)) {
			target.setHp(getEntityMaxHp(target));
		} else if (oldMaxHp < getEntityMaxHp(target)) {
			target.setHp(target.getHp() + getEntityMaxHp(target) - oldMaxHp);
		}

		log("{} was silenced", target);
	}

	public void startTurn(int playerId) {
		Player player = context.getPlayer(playerId);
		if (player.getMaxMana() < MAX_MANA) {
			player.setMaxMana(player.getMaxMana() + 1);
		}
		player.getStatistics().startTurn();

		player.setLockedMana(player.getAttributeValue(Attribute.OVERLOAD));
		int mana = Math.min(player.getMaxMana() - player.getLockedMana(), MAX_MANA);
		player.setMana(mana);
		String manaString = player.getMana() + "/" + player.getMaxMana();
		if (player.getLockedMana() > 0) {
			manaString += " (" + player.getLockedMana() + " locked by overload)";
		}
		log("{} starts his turn with {} mana", player.getName(), manaString);

		player.removeAttribute(Attribute.OVERLOAD);
		for (Summon summon : player.getSummons()) {
			summon.removeAttribute(Attribute.TEMPORARY_ATTACK_BONUS);
		}

		player.getHero().getHeroPower().setUsed(0);
		player.getHero().activateWeapon(true);
		refreshAttacksPerRound(player.getHero());
		for (Summon summon : player.getSummons()) {
			summon.removeAttribute(Attribute.SUMMONING_SICKNESS);
			refreshAttacksPerRound(summon);
		}
		context.fireGameEvent(new TurnStartEvent(context, player.getId()));
		drawCard(playerId, null);
		checkForDeadEntities();
	}

	public boolean summon(int playerId, Summon summon) {
		return summon(playerId, summon, null, -1, false);
	}

	public boolean summon(int playerId, Summon summon, Card source, int index, boolean resolveBattlecry) {
		Player player = context.getPlayer(playerId);
		if (!canSummonMoreMinions(player)) {
			log("{} cannot summon any more summons, {} is destroyed", player.getName(), summon);
			return false;
		}

		List<Summon> summonList = player.getSummons();

		if (resolveBattlecry
				&& hasEntityAttribute(summon, Attribute.MAGNETIC)
				&& (index >= 0 &&  index < summonList.size())) {
			Summon rightSummon = summonList.get(index);
			if (rightSummon instanceof Minion
					&& (summon.isTribe(rightSummon.getTribe())
                    || rightSummon.isTribe(summon.getTribe()))) {
				mergeSummons(rightSummon, summon);
				return false;
			}
		}

		summon.setId(idFactory.generateId());
		summon.setOwner(player.getId());

		context.getSummonReferenceStack().push(summon.getReference());

		log("{} summons {}", player.getName(), summon);

		if (index < 0 || index >= summonList.size()) {
			summonList.add(summon);
		} else {
			summonList.add(index, summon);
		}
		if (summon instanceof Minion) {
			Minion minion = (Minion) summon;

			context.fireGameEvent(new BeforeSummonEvent(context, minion, source));
		}
		context.fireGameEvent(new BoardChangedEvent(context));
		
		if (resolveBattlecry && summon.getBattlecry() != null) {
			resolveBattlecry(player.getId(), summon);
			checkForDeadEntities();
		}

		if (context.getEnvironment().get(Environment.TRANSFORM_REFERENCE) != null) {
			summon = (Summon) context.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.TRANSFORM_REFERENCE));
			summon.setBattlecry(null);
			context.getEnvironment().remove(Environment.TRANSFORM_REFERENCE);
		}

		context.fireGameEvent(new BoardChangedEvent(context));

		if (summon instanceof Minion) {
			Minion minion = (Minion) summon;
			player.getStatistics().minionSummoned(minion, context.getTurn());

			if (context.getEnvironment().get(Environment.TARGET_OVERRIDE) != null) {
				Actor actor = (Actor) context.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.TARGET_OVERRIDE));
				context.getEnvironment().remove(Environment.TARGET_OVERRIDE);
				SummonEvent summonEvent = new SummonEvent(context, actor, source);
				context.fireGameEvent(summonEvent);
			} else {
				SummonEvent summonEvent = new SummonEvent(context, minion, source);
				context.fireGameEvent(summonEvent);
			}

			applyAttribute(minion, Attribute.SUMMONING_SICKNESS);
			refreshAttacksPerRound(minion);
		} else if (summon instanceof Permanent) {
			Permanent permanent = (Permanent) summon;
			player.getStatistics().permanentSummoned(permanent, context.getTurn());
			
		}

		if (summon.hasSpellTrigger()) {
			for (SpellTrigger trigger : summon.getSpellTriggers()) {
				addGameEventListener(player, trigger, summon);
			}
		}

		if (summon.getCardCostModifier() != null) {
			addManaModifier(player, summon.getCardCostModifier(), summon);
		}

		if (summon instanceof Minion) {
			Minion minion = (Minion) summon;
			if (source != null) {
				source.setAttribute(Attribute.ATTACK, source.getBaseAttributeValue(Attribute.ATTACK));
				source.setAttribute(Attribute.MAX_HP, source.getBaseAttributeValue(Attribute.MAX_HP));
				source.setAttribute(Attribute.HP, source.getBaseAttributeValue(Attribute.MAX_HP));
			}
			handleEnrage(minion);

			context.getSummonReferenceStack().pop();
			if (player.getSummons().contains(minion)) {
				context.fireGameEvent(new AfterSummonEvent(context, minion, source));
			}
		}
		context.fireGameEvent(new BoardChangedEvent(context));
		return true;
	}

	public Summon newSummon(int playerId, Summon summon, Card source, int index, boolean resolveBattlecry) {
		Player player = context.getPlayer(playerId);
		if (!canSummonMoreMinions(player)) {
			log("{} cannot summon any more summons, {} is destroyed", player.getName(), summon);
			return null;
		}

		List<Summon> summonList = player.getSummons();

		if (resolveBattlecry
				&& hasEntityAttribute(summon, Attribute.MAGNETIC)
				&& (index >= 0 &&  index < summonList.size())) {
			Summon rightSummon = summonList.get(index);
			if (rightSummon instanceof Minion
					&& (summon.isTribe(rightSummon.getTribe())
                    || rightSummon.isTribe(summon.getTribe()))) {
				mergeSummons(rightSummon, summon);
				return null;
			}
		}

		summon.setId(idFactory.generateId());
		summon.setOwner(player.getId());

		context.getSummonReferenceStack().push(summon.getReference());

		log("{} summons {}", player.getName(), summon);

		if (index < 0 || index >= summonList.size()) {
			summonList.add(summon);
		} else {
			summonList.add(index, summon);
		}
		if (summon instanceof Minion) {
			Minion minion = (Minion) summon;

			context.fireGameEvent(new BeforeSummonEvent(context, minion, source));
		}
		context.fireGameEvent(new BoardChangedEvent(context));

		if (resolveBattlecry && summon.getBattlecry() != null) {
			resolveBattlecry(player.getId(), summon);
			checkForDeadEntities();
		}

		if (context.getEnvironment().get(Environment.TRANSFORM_REFERENCE) != null) {
			summon = (Summon) context.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.TRANSFORM_REFERENCE));
			summon.setBattlecry(null);
			context.getEnvironment().remove(Environment.TRANSFORM_REFERENCE);
		}

		context.fireGameEvent(new BoardChangedEvent(context));

		if (summon instanceof Minion) {
			Minion minion = (Minion) summon;
			player.getStatistics().minionSummoned(minion, context.getTurn());

			if (context.getEnvironment().get(Environment.TARGET_OVERRIDE) != null) {
				Actor actor = (Actor) context.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.TARGET_OVERRIDE));
				context.getEnvironment().remove(Environment.TARGET_OVERRIDE);
				SummonEvent summonEvent = new SummonEvent(context, actor, source);
				context.fireGameEvent(summonEvent);
			} else {
				SummonEvent summonEvent = new SummonEvent(context, minion, source);
				context.fireGameEvent(summonEvent);
			}

			applyAttribute(minion, Attribute.SUMMONING_SICKNESS);
			refreshAttacksPerRound(minion);
		} else if (summon instanceof Permanent) {
			Permanent permanent = (Permanent) summon;
			player.getStatistics().permanentSummoned(permanent, context.getTurn());

		}

		if (summon.hasSpellTrigger()) {
			for (SpellTrigger trigger : summon.getSpellTriggers()) {
				addGameEventListener(player, trigger, summon);
			}
		}

		if (summon.getCardCostModifier() != null) {
			addManaModifier(player, summon.getCardCostModifier(), summon);
		}

		if (summon instanceof Minion) {
			Minion minion = (Minion) summon;
			if (source != null) {
				source.setAttribute(Attribute.ATTACK, source.getBaseAttributeValue(Attribute.ATTACK));
				source.setAttribute(Attribute.MAX_HP, source.getBaseAttributeValue(Attribute.MAX_HP));
				source.setAttribute(Attribute.HP, source.getBaseAttributeValue(Attribute.MAX_HP));
			}
			handleEnrage(minion);

			context.getSummonReferenceStack().pop();
			if (player.getSummons().contains(minion)) {
				context.fireGameEvent(new AfterSummonEvent(context, minion, source));
			}
		}
		context.fireGameEvent(new BoardChangedEvent(context));
		return summon;
	}

	/**
	 * Transforms a Minion into a new Minion.
	 * 
	 * @param summon
	 *            The original summon in play
	 * @param newSummon
	 *            The new summon to transform into
	 */
	public void transformMinion(Summon summon, Summon newSummon) {
		// Remove any spell triggers associated with the old minion.
		removeSpellTriggers(summon);

		Player owner = context.getPlayer(summon.getOwner());
		int index = owner.getSummons().indexOf(summon);
		owner.getSummons().remove(summon);
		
		// If we want to straight up remove a minion from existence without
		// killing it, this would be the best way.
		if (newSummon != null) {
			log("{} was transformed to {}", summon, newSummon);

			// Give the new minion an ID.
			newSummon.setId(idFactory.generateId());
			newSummon.setOwner(owner.getId());
	
			// If the minion being transforms is being summoned, replace the old
			// minion on the stack.
			// Otherwise, summon the add the new minion.
			// However, do not give a summon event.
			if (!context.getSummonReferenceStack().isEmpty() && context.getSummonReferenceStack().peek().equals(summon.getReference())
					&& !context.getEnvironment().containsKey(Environment.TRANSFORM_REFERENCE)) {
				context.getEnvironment().put(Environment.TRANSFORM_REFERENCE, newSummon.getReference());
				owner.getSummons().add(index, newSummon);
	
				// It's quite possible that this is actually supposed to add the
				// minion to the zone it was originally in.
				// This means minions in the SetAsideZone or the Graveyard that are
				// targeted (through bizarre mechanics)
				// add the minion to there. This will be tested eventually with
				// Resurrect, Recombobulator, and Illidan.
				// Since this is unknown, this is the patch for it.
			} else if (!owner.getSetAsideZone().contains(summon)) {
				if (index < 0 || index >= owner.getSummons().size()) {
					owner.getSummons().add(newSummon);
				} else {
					owner.getSummons().add(index, newSummon);
				}
	
				applyAttribute(newSummon, Attribute.SUMMONING_SICKNESS);
				refreshAttacksPerRound(newSummon);
	
				if (newSummon.hasSpellTrigger()) {
					for (SpellTrigger spellTrigger : newSummon.getSpellTriggers()) {
						addGameEventListener(owner, spellTrigger, newSummon);
					}
				}
	
				if (newSummon.getCardCostModifier() != null) {
					addManaModifier(owner, newSummon.getCardCostModifier(), newSummon);
				}
	
				handleEnrage(newSummon);
			} else {
				owner.getSetAsideZone().add(newSummon);
				newSummon.setId(idFactory.generateId());
				newSummon.setOwner(owner.getId());
				removeSpellTriggers(newSummon);
				return;
			}
		
		}

		// Move the old minion to the Set Aside Zone
		owner.getSetAsideZone().add(summon);

		context.fireGameEvent(new BoardChangedEvent(context));
	}

	public void useHeroPower(int playerId) {
		Player player = context.getPlayer(playerId);
		HeroPower power = player.getHero().getHeroPower();
		int modifiedManaCost = getModifiedManaCost(player, power);
		modifyCurrentMana(playerId, -modifiedManaCost);
		log("{} uses {}", player.getName(), power);
		power.markUsed();
		player.getStatistics().cardPlayed(power, context.getTurn());
		context.fireGameEvent(new HeroPowerUsedEvent(context, playerId, power));
	}

}
