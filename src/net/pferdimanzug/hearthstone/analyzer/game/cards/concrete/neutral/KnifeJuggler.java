package net.pferdimanzug.hearthstone.analyzer.game.cards.concrete.neutral;

import net.pferdimanzug.hearthstone.analyzer.game.cards.MinionCard;
import net.pferdimanzug.hearthstone.analyzer.game.cards.Rarity;
import net.pferdimanzug.hearthstone.analyzer.game.entities.heroes.HeroClass;
import net.pferdimanzug.hearthstone.analyzer.game.entities.minions.Minion;
import net.pferdimanzug.hearthstone.analyzer.game.spells.DamageRandomSpell;
import net.pferdimanzug.hearthstone.analyzer.game.spells.Spell;
import net.pferdimanzug.hearthstone.analyzer.game.spells.TargetPlayer;
import net.pferdimanzug.hearthstone.analyzer.game.spells.trigger.MinionSummonedTrigger;
import net.pferdimanzug.hearthstone.analyzer.game.spells.trigger.SpellTrigger;
import net.pferdimanzug.hearthstone.analyzer.game.targeting.EntityReference;

public class KnifeJuggler extends MinionCard {

	public KnifeJuggler() {
		super("Knife Juggler", 3, 2, Rarity.RARE, HeroClass.ANY, 2);
		setDescription("After you summon a minion, deal 1 damage to a random enemy.");
	}
	
	@Override
	public Minion summon() {
		Minion knifeJuggler = createMinion();
		Spell damageRandomSpell = new DamageRandomSpell(1, 1);
		damageRandomSpell.setTarget(EntityReference.ENEMY_CHARACTERS);
		SpellTrigger trigger = new SpellTrigger(new MinionSummonedTrigger(TargetPlayer.SELF), damageRandomSpell);
		knifeJuggler.setSpellTrigger(trigger);
		return knifeJuggler;
	}

}