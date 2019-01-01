package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.entities.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.group.Group;
import net.demilich.metastone.game.cards.interfaced.HeroClassImplementation;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Tribe;
import net.demilich.metastone.game.entities.minions.Summon;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.Operation;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class SpellUtils {

	public static void castChildSpell(GameContext context, Player player, SpellDesc spell, Entity source, Entity target) {
		EntityReference sourceReference = source != null ? source.getReference() : null;
		EntityReference targetReference = spell.getTarget();
		if (targetReference == null && target != null) {
			targetReference = target.getReference();
		}
		context.getLogic().castSpell(player.getId(), spell, sourceReference, targetReference, true);
	}

    public static void castChildSpell(GameContext context, Player player, SpellDesc spell, Entity source, Entity target, Entity output) {
	    context.getSpellOutputStack().push(output.getReference());
        EntityReference sourceReference = source != null ? source.getReference() : null;
        EntityReference targetReference = spell.getTarget();
        if (targetReference == null && target != null) {
            targetReference = target.getReference();
        }
        context.getLogic().castSpell(player.getId(), spell, sourceReference, targetReference, true);
        context.getSpellOutputStack().pop();
    }

	public static boolean evaluateOperation(Operation operation, int actualValue, int targetValue) {
		switch (operation) {
		case EQUAL:
			return actualValue == targetValue;
		case GREATER:
			return actualValue > targetValue;
		case GREATER_OR_EQUAL:
			return actualValue >= targetValue;
		case HAS:
			return actualValue > 0;
		case LESS:
			return actualValue < targetValue;
		case LESS_OR_EQUAL:
			return actualValue <= targetValue;
		}
		return false;
	}

	public static CardCollection getCardCollection(GameContext context, Player player, SpellDesc spell, Entity target, Entity source) {
		CardCollection cards;
		if (spell.contains(SpellArg.CARD_SOURCE)) {
			CardSource cardSource = (CardSource) spell.get(SpellArg.CARD_SOURCE);
			cards = cardSource.getCards(context, player);
		} else {
			cards = CardCatalogue.query(context.getDeckFormat());
		}
		return cards;
	}

	public static CardCollection getCards(CardCollection source, Predicate<Card> filter) {
		CardCollection result = new CardCollection();
		for (Card card : source) {
			if (filter == null || filter.test(card)) {
				result.add(card);
			}
		}
		return result;
	}
	
	public static Card getCard(GameContext context, SpellDesc spell) {
		Card card = null;
		String cardName = (String) spell.get(SpellArg.CARD);
		card = CardCatalogue.getCardById(cardName);
		if (spell.get(SpellArg.CARD).toString().toUpperCase().equals("PENDING_CARD")) {
			card = (Card) context.getPendingCard();
		} else if (spell.get(SpellArg.CARD).toString().toUpperCase().equals("EVENT_CARD")) {
			card = (Card) context.getEventCard();
		}
		return card;
	}

	public static Card[] getCards(GameContext context, Player player, SpellDesc spell, Entity source, Entity target) {
		String[] cardNames = new String[0];
		if (spell.contains(SpellArg.CARDS)) {
			cardNames = (String[]) spell.get(SpellArg.CARDS);
		} else if (spell.contains(SpellArg.GROUP)) {
			Group group = getGroup(context, spell);
			Entity[] entities = group.getGroup(context);
			if (entities instanceof Card[]) {
				return (Card[]) entities;
			}
		} else if (spell.contains(SpellArg.CARD_FILTER)) {
			EntityFilter cardFilter = (EntityFilter) spell.get(SpellArg.CARD_FILTER);
			CardCollection cardCollection = getCardCollection(context, player, spell, target, source);
			List<Card> cards = new ArrayList<>();
			for (Card card : cardCollection) {
				if (cardFilter.matches(context, player, card)) {
					cards.add(card);
				}
			}
			return cards.toArray(new Card[0]);
		} else if (spell.contains(SpellArg.CARD)) {
			cardNames = new String[1];
			cardNames[0] = (String) spell.get(SpellArg.CARD);
		}
		Card[] cards = new Card[cardNames.length];
		for (int i = 0; i < cards.length; i++) {
			cards[i] = context.getCardById(cardNames[i]);
		}
		return cards;
	}
	
	public static DiscoverAction getDiscover(GameContext context, Player player, SpellDesc spell, CardCollection cards) {
		List<GameAction> discoverActions = new ArrayList<>();
		for (Card card : cards) {
			SpellDesc spellClone = spell.addArg(SpellArg.CARD, card.getCardId());
			DiscoverAction discover = DiscoverAction.createDiscover(spellClone);
			discover.setCard(card);
			discover.setActionSuffix(card.getName());
			discoverActions.add(discover);
		}
		if (discoverActions.size() == 0) {
			return null;
		}
		
		if (context.getLogic().attributeExists(Attribute.ALL_RANDOM_YOGG_ONLY_FINAL_DESTINATION)) {
			return (DiscoverAction) discoverActions.get(context.getLogic().random(discoverActions.size()));
		} else {
			return (DiscoverAction) player.getBehaviour().requestAction(context, player, discoverActions);
		}
	}
	
	public static Group getGroup(GameContext context, SpellDesc spell) {
		String groupCardId = (String) spell.get(SpellArg.GROUP);
		Card card = context.getCardById(groupCardId);
		if (card instanceof GroupCard) {
			GroupCard groupCard = (GroupCard) card;
			return groupCard.getGroup().create();
		}
		return null;
	}

	public static DiscoverAction getSpellDiscover(GameContext context, Player player, SpellDesc desc, List<SpellDesc> spells) {
		List<GameAction> discoverActions = new ArrayList<>();
		for (SpellDesc spell : spells) {
			DiscoverAction discover = DiscoverAction.createDiscover(spell);
			discover.setName(spell.getString(SpellArg.NAME));
			discover.setDescription(spell.getString(SpellArg.DESCRIPTION));
			discover.setActionSuffix((String) spell.get(SpellArg.NAME));
			discoverActions.add(discover);
		}
		
		if (context.getLogic().attributeExists(Attribute.ALL_RANDOM_YOGG_ONLY_FINAL_DESTINATION)) {
			return (DiscoverAction) discoverActions.get(context.getLogic().random(discoverActions.size()));
		} else {
			return (DiscoverAction) player.getBehaviour().requestAction(context, player, discoverActions);
		}
	}

	public static Card getRandomCard(CardCollection source, Predicate<Card> filter) {
		CardCollection result = getCards(source, filter);
		if (result.isEmpty()) {
			return null;
		}
		return result.getRandom();
	}
	
	public static HeroClassImplementation getRandomBaseHeroClass() {
		HeroClassImplementation[] values = HeroClassImplementation.values();
		List<HeroClassImplementation> heroClasses = new ArrayList<>();
		for (HeroClassImplementation heroClass : values) {
			if (heroClass.isBaseClass()) {
				heroClasses.add(heroClass);
			}
		}
		return heroClasses.get(ThreadLocalRandom.current().nextInt(heroClasses.size()));
	}
	
	public static HeroClassImplementation getRandomHeroClassExcept(HeroClassImplementation... heroClassesExcluded) {
		HeroClassImplementation[] values = HeroClassImplementation.values();
		List<HeroClassImplementation> heroClasses = new ArrayList<>();
		for (HeroClassImplementation heroClass : values) {
			if (heroClass.isBaseClass()) {
				heroClasses.add(heroClass);
				for (HeroClassImplementation heroClassExcluded : heroClassesExcluded) {
					if (heroClassExcluded == heroClass) {
						heroClasses.remove(heroClass);
					}
				}
			}
		}
		return heroClasses.get(ThreadLocalRandom.current().nextInt(heroClasses.size()));
	}

	public static <T> T getRandomTarget(List<T> targets) {
		int randomIndex = ThreadLocalRandom.current().nextInt(targets.size());
		return targets.get(randomIndex);
	}

	public static List<Actor> getValidRandomTargets(List<Entity> targets) {
		List<Actor> validTargets = new ArrayList<>();
		for (Entity entity : targets) {
			Actor actor = (Actor) entity;
			if (!actor.isDestroyed() || actor.getEntityType() == EntityType.HERO) {
				validTargets.add(actor);
			}

		}
		return validTargets;
	}

	public static List<Entity> getValidTargets(GameContext context, Player player, List<Entity> allTargets, EntityFilter filter) {
		if (filter == null) {
			return allTargets;
		}
		List<Entity> validTargets = new ArrayList<>();
		for (Entity entity : allTargets) {
			if (filter.matches(context, player, entity)) {
				validTargets.add(entity);
			}
		}
		return validTargets;
	}

	public static int hasHowManyOfTribe(Player player, Tribe tribe) {
		int count = 0;
		for (Summon summon : player.getSummons()) {
			if (summon.getTribe() == tribe) {
				count++;
			}
		}
		return count;
	}
	
	public static boolean highlanderDeck(Player player) {
		List<String> cards = new ArrayList<String>();
		for (Card card : player.getDeck()) {
			if (cards.contains(card.getCardId())) {
				return false;
			}
			cards.add(card.getCardId());
		}
		return true;
	}

	public static boolean holdsCardOfType(Player player, CardType cardType) {
		for (Card card : player.getHand()) {
			if (card.getCardType().isCardType(cardType)) {
				return true;
			}
		}
		return false;
	}

	public static boolean holdsMinionOfTribe(Player player, Tribe tribe) {
		for (Card card : player.getHand()) {
			if (card.getTribe() == tribe) {
				return true;
			}
		}
		return false;
	}

	public static int howManyMinionsDiedThisTurn(GameContext context) {
		int currentTurn = context.getTurn();
		int count = 0;
		for (Player player : context.getPlayers()) {
			for (Entity deadEntity : player.getGraveyard()) {
				if (deadEntity.getEntityType() != EntityType.MINION) {
					continue;
				}

				if (deadEntity.getAttributeValue(Attribute.DIED_ON_TURN) == currentTurn) {
					count++;
				}

			}
		}
		return count;
	}
	
	public static int getBoardPosition(GameContext context, Player player, SpellDesc desc, Entity source, int count) {
		final int UNDEFINED = -1;
		int boardPosition = desc.getInt(SpellArg.BOARD_POSITION_ABSOLUTE, UNDEFINED);
		if (boardPosition != UNDEFINED) {
			return boardPosition;
		}
		boolean relativeBoardPosition = desc.getBool(SpellArg.BOARD_POSITION_RELATIVE);
		if (!relativeBoardPosition) {
			return UNDEFINED;
		}

		int sourcePosition = context.getBoardPosition((Summon) source);
		if (sourcePosition == UNDEFINED) {
			return UNDEFINED;
		}
		if (relativeBoardPosition) {
			return sourcePosition + ((count + 1) % 2);
		}
		return UNDEFINED;
	}

	private SpellUtils() {
	}

}
