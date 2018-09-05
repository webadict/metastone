package net.demilich.metastone.tests;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.interfaced.BaseCardSet;
import net.demilich.metastone.game.cards.interfaced.HeroClassImplementation;
import net.demilich.metastone.game.cards.interfaced.NonHeroClass;
import net.demilich.metastone.game.decks.DeckFactory;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.gameconfig.PlayerConfig;
import net.demilich.metastone.game.logic.GameLogic;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.concurrent.ThreadLocalRandom;

public class MassTest extends TestBase {

	private static HeroClassImplementation getRandomClass() {
		HeroClassImplementation randomClass = NonHeroClass.NEUTRAL;
		HeroClassImplementation[] values = HeroClassImplementation.values();
		while (!randomClass.isBaseClass()) {
			randomClass = values[ThreadLocalRandom.current().nextInt(values.length)];
		}
		return randomClass;
	}

	@BeforeTest
	private void loggerSetup() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
	}

	@Test(threadPoolSize = 16, invocationCount = 1000)
	public void testRandomMassPlay() {
		DeckFormat deckFormat = new DeckFormat();
		for (BaseCardSet set : BaseCardSet.values()) {
			deckFormat.addSet(set);
		}
		HeroClassImplementation heroClass1 = getRandomClass();
		PlayerConfig player1Config = new PlayerConfig(DeckFactory.getRandomDeck(heroClass1, deckFormat), new PlayRandomBehaviour());
		player1Config.setName("Player 1");
		player1Config.setHeroCard(getHeroCardForClass(heroClass1));
		Player player1 = new Player(player1Config);

		HeroClassImplementation heroClass2 = getRandomClass();
		PlayerConfig player2Config = new PlayerConfig(DeckFactory.getRandomDeck(heroClass2, deckFormat), new PlayRandomBehaviour());
		player2Config.setName("Player 2");
		player2Config.setHeroCard(getHeroCardForClass(heroClass2));
		Player player2 = new Player(player2Config);
		GameContext context = new GameContext(player1, player2, new GameLogic(), deckFormat);
		try {
			context.play();
			context.dispose();
		} catch (Exception e) {
			Assert.fail("Exception occured", e);
		}

	}

}
