package net.demilich.metastone.game.actions;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.CardReference;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;

public class PlayHeroCardAction extends PlayCardAction {

	private final BattlecryAction battlecry;

	public PlayHeroCardAction(CardReference cardReference) {
		this(cardReference, null);
	}

	public PlayHeroCardAction(CardReference cardReference, BattlecryAction battlecry) {
		super(cardReference);
		this.battlecry = battlecry;
		setTargetRequirement(TargetSelection.FRIENDLY_MINIONS);
		setActionType(ActionType.CHANGE_HERO);
	}

	@Override
	public void play(GameContext context, int playerId) {
		HeroCard heroCard = (HeroCard) context.getPendingCard();
		Hero hero = heroCard.createHero();
		if (battlecry != null) {
			hero.setBattlecry(battlecry);
		}
		Player player = context.getPlayer(playerId);
		context.getLogic().changeHero(player, hero, heroCard, true);
	}


}
