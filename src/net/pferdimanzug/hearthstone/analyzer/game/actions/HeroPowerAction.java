package net.pferdimanzug.hearthstone.analyzer.game.actions;

import net.pferdimanzug.hearthstone.analyzer.game.GameContext;
import net.pferdimanzug.hearthstone.analyzer.game.Player;
import net.pferdimanzug.hearthstone.analyzer.game.heroes.powers.HeroPower;

public abstract class HeroPowerAction extends PlayCardAction {

	public HeroPowerAction(HeroPower heroPower) {
		super(heroPower);
		setActionType(ActionType.HERO_POWER);
	}
	
	private HeroPower getHeroPower() {
		return (HeroPower) getCard();
	}
	
	@Override
	public TargetRequirement getTargetRequirement() {
		return getHeroPower().getTargetRequirement();
	}

	@Override
	public void execute(GameContext context, Player player) {
		context.getLogic().useHeroPower(player, getHeroPower());
		cast(context, player);
	}

}