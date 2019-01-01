package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.CardLocation;

public class DrawCardSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        CardCollection relevantCards = new CardCollection();
        int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
        EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
        if (target != null && target.getEntityType() == EntityType.CARD) {
            Card card = (Card) target;
            relevantCards.add(card);
        } else if (cardFilter != null) {
            relevantCards.addAll(SpellUtils.getCards(player.getDeck(), card -> cardFilter.matches(context, player, card)));
        } else {
            relevantCards.addAll(SpellUtils.getCards(player.getDeck(), null));
        }

        for (int i = 0; i < value; i++) {
            Card card = null;
            if (!relevantCards.isEmpty()) {
                card = relevantCards.getRandom();
                relevantCards.remove(card);
                context.getLogic().drawCard(player.getId(), card, source);
            }
            if (card == null || card.getLocation() == CardLocation.GRAVEYARD) {
                return;
            }
            SpellDesc subSpell = desc.getSubSpell();
            if (subSpell != null) {
                SpellUtils.castChildSpell(context, player, subSpell, source, target, card);
            }
        }
	}
}
