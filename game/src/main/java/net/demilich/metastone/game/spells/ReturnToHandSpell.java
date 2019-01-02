package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Summon;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ReturnToHandSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(ReturnToHandSpell.class);

	public static SpellDesc create() {
		return create(null, null, false);
	}

	public static SpellDesc create(EntityReference target, SpellDesc spell, boolean randomTarget) {
		Map<SpellArg, Object> arguments = SpellDesc.build(ReturnToHandSpell.class);
		arguments.put(SpellArg.SPELL, spell);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.RANDOM_TARGET, randomTarget);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		SpellDesc cardSpell = (SpellDesc) desc.get(SpellArg.SPELL);
		//Summon summon = (Summon) target;
		Player owner = context.getPlayer(target.getOwner());
		if (owner.getHand().getCount() >= GameLogic.MAX_HAND_CARDS) {
			logger.debug("{} is destroyed because {}'s hand is full", target, owner.getName());
			context.getLogic().markAsDestroyed((Actor) target);
		} else {
			logger.debug("{} is returned to {}'s hand", target, owner.getName());
			Card sourceCard;
			if (target instanceof Summon) {
				Summon summon = (Summon) target;
				context.getLogic().removeSummon(summon, true);
				sourceCard = summon.getSourceCard().getCopy();
				context.getLogic().receiveCard(summon.getOwner(), sourceCard);
			} else if (target instanceof Card) {
				Card card = (Card) target;
				sourceCard = card;
				context.getLogic().receiveCard(card.getOwner(), sourceCard);
			} else {
				return;
			}
			if (cardSpell != null) {
				SpellUtils.castChildSpell(context, player, cardSpell, source, target, sourceCard);
			}
		}
	}

}
