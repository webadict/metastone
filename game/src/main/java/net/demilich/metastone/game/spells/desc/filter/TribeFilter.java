package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Tribe;

public class TribeFilter extends EntityFilter {

	public TribeFilter(FilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity) {
		Tribe tribe = (Tribe) desc.get(FilterArg.TRIBE);
		return entity.getTribe() == tribe;
	}

}
