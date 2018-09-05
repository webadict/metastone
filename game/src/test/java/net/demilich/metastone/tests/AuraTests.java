package net.demilich.metastone.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.cards.interfaced.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.DestroySpell;
import net.demilich.metastone.game.spells.aura.BuffAura;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;

public class AuraTests extends BasicTests {

	@Test
	public void testAdjacentAura() {
		GameContext context = createContext(HeroClass.MAGE, HeroClass.WARRIOR);
		Player player = context.getPlayer1();

		TestMinionCard minionCard = new TestMinionCard(1, 1);
		Minion testMinion1 = playMinionCard(context, player, minionCard);

		MinionCard direWolfCard = (MinionCard) CardCatalogue.getCardById("minion_dire_wolf_alpha");
		Minion direWolf = playMinionCard(context, player, direWolfCard);

		minionCard = new TestMinionCard(5, 5);
		Minion testMinion2 = playMinionCard(context, player, minionCard);
		minionCard = new TestMinionCard(5, 5);
		Minion testMinion3 = playMinionCard(context, player, minionCard);

		Assert.assertEquals(context.getLogic().getEntityAttack(direWolf), 2);
		Assert.assertEquals(context.getLogic().getEntityAttack(testMinion1), 2);
		Assert.assertEquals(context.getLogic().getEntityAttack(testMinion2), 6);
		Assert.assertEquals(context.getLogic().getEntityAttack(testMinion3), 5);

		SpellCard destroyCard = new TestSpellCard(DestroySpell.create());
		destroyCard.setTargetRequirement(TargetSelection.ANY);
		context.getLogic().receiveCard(player.getId(), destroyCard);
		GameAction destroyAction = destroyCard.play();
		destroyAction.setTarget(testMinion2);
		context.getLogic().performGameAction(player.getId(), destroyAction);
		Assert.assertEquals(context.getLogic().getEntityAttack(testMinion1), 2);
		Assert.assertEquals(context.getLogic().getEntityAttack(direWolf), 2);
		Assert.assertEquals(context.getLogic().getEntityAttack(testMinion3), 6);

		playCard(context, player, CardCatalogue.getCardById("spell_hellfire"));
		Assert.assertEquals(context.getLogic().getEntityAttack(direWolf), 2);
		Assert.assertEquals(context.getLogic().getEntityAttack(testMinion3), 5);
	}

	@Test
	public void testAura() {
		GameContext context = createContext(HeroClass.MAGE, HeroClass.WARRIOR);
		Player player = context.getPlayer1();
		Player opponent = context.getPlayer2();

		TestMinionCard minionCard = new TestMinionCard(1, 1);
		minionCard.getMinion().addSpellTrigger(new BuffAura(1, 1, EntityReference.OTHER_FRIENDLY_MINIONS, null));
		playCard(context, player, minionCard);

		Actor minion1 = getSingleMinion(player.getMinions());
		Assert.assertEquals(context.getLogic().getEntityAttack(minion1), 1);

		minionCard = new TestMinionCard(1, 1);
		minionCard.getMinion().addSpellTrigger(new BuffAura(1, 1, EntityReference.OTHER_FRIENDLY_MINIONS, null));
		Actor minion2 = playMinionCard(context, player, minionCard);

		Assert.assertNotEquals(minion1, minion2);
		Assert.assertEquals(context.getLogic().getEntityAttack(minion1), 2);
		Assert.assertEquals(context.getLogic().getEntityAttack(minion2), 2);

		TestMinionCard minionCardOpponent = new TestMinionCard(3, 3);
		Actor enemyMinion = playMinionCard(context, opponent, minionCardOpponent);
		Assert.assertEquals(context.getLogic().getEntityAttack(enemyMinion), 3);

		Assert.assertEquals(context.getLogic().getEntityAttack(minion1), 2);
		Assert.assertEquals(context.getLogic().getEntityAttack(minion2), 2);
		PhysicalAttackAction attackAction = new PhysicalAttackAction(enemyMinion.getReference());
		attackAction.setTarget(minion2);
		context.getLogic().performGameAction(opponent.getId(), attackAction);
		Assert.assertEquals(context.getLogic().getEntityAttack(minion1), 1);

		minionCard = new TestMinionCard(1, 1);
		minion2 = playMinionCard(context, player, minionCard);
		Assert.assertEquals(context.getLogic().getEntityAttack(minion1), 1);
		Assert.assertEquals(context.getLogic().getEntityAttack(minion2), 2);
	}

	@Test
	public void testAuraPlusFaceless() {
		GameContext context = createContext(HeroClass.PRIEST, HeroClass.WARRIOR);
		Player player = context.getPlayer1();

		Minion murloc = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_bluegill_warrior"));
		Assert.assertEquals(context.getLogic().getEntityAttack(murloc), 2);
		Assert.assertEquals(murloc.getHp(), 1);

		Minion warleader = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_murloc_warleader"));
		Assert.assertEquals(context.getLogic().getEntityAttack(murloc), 4);
		Assert.assertEquals(murloc.getHp(), 2);
		Assert.assertEquals(context.getLogic().getEntityAttack(warleader), 3);
		Assert.assertEquals(warleader.getHp(), 3);

		TestBehaviour behaviour = (TestBehaviour) player.getBehaviour();
		behaviour.setTargetPreference(warleader.getReference());

		Card facelessCard = CardCatalogue.getCardById("minion_faceless_manipulator");
		context.getLogic().receiveCard(player.getId(), facelessCard);
		GameAction action = facelessCard.play();
		action.setTarget(warleader);
		context.getLogic().performGameAction(player.getId(), action);
		Assert.assertEquals(context.getLogic().getEntityAttack(murloc), 6);
		Assert.assertEquals(murloc.getHp(), 3);
	}

	@Test
	public void testAuraPlusMindControl() {
		GameContext context = createContext(HeroClass.PRIEST, HeroClass.WARRIOR);
		Player player = context.getPlayer1();
		Player opponent = context.getPlayer2();

		context.getLogic().endTurn(player.getId());

		TestMinionCard minionCard = new TestMinionCard(1, 1);
		minionCard.getMinion().addSpellTrigger(new BuffAura(1, 1, EntityReference.FRIENDLY_MINIONS, null));
		Minion auraMinion = playMinionCard(context, opponent, minionCard);
		Minion opponentMinion = playMinionCard(context, opponent, new TestMinionCard(1, 1));
		Assert.assertEquals(context.getLogic().getEntityAttack(opponentMinion), 2);
		context.getLogic().endTurn(opponent.getId());

		minionCard = new TestMinionCard(1, 1);
		Actor minion1 = playMinionCard(context, player, minionCard);
		Assert.assertEquals(context.getLogic().getEntityAttack(minion1), 1);

		Card mindControlCard = CardCatalogue.getCardById("spell_mind_control");
		context.getLogic().receiveCard(player.getId(), mindControlCard);
		GameAction mindControl = mindControlCard.play();
		mindControl.setTarget(auraMinion);
		context.getLogic().performGameAction(player.getId(), mindControl);

		Assert.assertEquals(auraMinion.getOwner(), player.getId());
		Assert.assertEquals(context.getLogic().getEntityAttack(minion1), 2);
		Assert.assertEquals(context.getLogic().getEntityAttack(opponentMinion), 1);
	}
	
	@Test
	public void testOpponentAuraPlusFaceless() {
		GameContext context = createContext(HeroClass.PRIEST, HeroClass.WARRIOR);
		Player player = context.getPlayer1();
		Player opponent = context.getPlayer2();

		Minion wolf = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_dire_wolf_alpha"));
		Assert.assertEquals(context.getLogic().getEntityAttack(wolf), 2);
		Assert.assertEquals(wolf.getHp(), 2);

		Minion dummy = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_target_dummy"));
		Assert.assertEquals(context.getLogic().getEntityAttack(dummy), 1);
		Assert.assertEquals(dummy.getHp(), 2);
		Assert.assertEquals(context.getLogic().hasEntityAttribute(dummy, Attribute.AURA_UNTARGETABLE_BY_SPELLS), false);
		
		playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_wee_spellstopper"));
		Assert.assertEquals(context.getLogic().hasEntityAttribute(dummy, Attribute.AURA_UNTARGETABLE_BY_SPELLS), true);
		
		context.getLogic().endTurn(player.getId());

		TestBehaviour behaviour = (TestBehaviour) opponent.getBehaviour();
		behaviour.setTargetPreference(dummy.getReference());

		Card facelessCard = CardCatalogue.getCardById("minion_faceless_manipulator");
		context.getLogic().receiveCard(opponent.getId(), facelessCard);
		GameAction action = facelessCard.play();
		action.setTarget(dummy);
		context.getLogic().performGameAction(opponent.getId(), action);
		
		Minion facelessCopy = getSummonedMinion(opponent.getMinions());
		Assert.assertEquals(context.getLogic().hasEntityAttribute(facelessCopy, Attribute.AURA_UNTARGETABLE_BY_SPELLS), false);
		Assert.assertEquals(context.getLogic().getEntityAttack(facelessCopy), 0);
	}

}
