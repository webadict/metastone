package net.demilich.metastone.gui.playmode;

import net.demilich.metastone.game.GameContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import net.demilich.metastone.game.entities.Attribute;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Permanent;
import net.demilich.metastone.game.entities.minions.Summon;
import net.demilich.metastone.gui.cards.CardTooltip;

public class SummonToken extends GameToken {
	@FXML
	private Label name;
	@FXML
	private Group attackAnchor;
	@FXML
	private Group hpAnchor;

	@FXML
	private Node defaultToken;
	@FXML
	private Node divineShield;
	@FXML
	private Node taunt;
	@FXML
	private Text windfury;
	@FXML
	private Node deathrattle;

	@FXML
	private Shape frozen;

	private CardTooltip cardTooltip;
	
	Logger logger = LoggerFactory.getLogger(SummonToken.class);

	public SummonToken() {
		super("SummonToken.fxml");
		Tooltip tooltip = new Tooltip();
		cardTooltip = new CardTooltip();
		tooltip.setGraphic(cardTooltip);
		Tooltip.install(this, tooltip);
		frozen.getStrokeDashArray().add(16.0);
	}

	public void setSummon(GameContext context, Summon summon) {
		name.setText(summon.getName());
		if (summon instanceof Minion) {
			attackAnchor.setVisible(true);
			hpAnchor.setVisible(true);
			setScoreValue(attackAnchor, context.getLogic().getEntityAttack(summon), summon.getBaseAttributeValue(Attribute.ATTACK));
			setScoreValue(hpAnchor, summon.getHp(), summon.getBaseHp(), context.getLogic().getEntityMaxHp(summon));
		} else if (summon instanceof Permanent) {
			attackAnchor.setVisible(false);
			hpAnchor.setVisible(false);
		}
		visualizeStatus(context, summon);
		cardTooltip.setCard(summon.getSourceCard());
	}

	private void visualizeStatus(GameContext context, Summon summon) {
		taunt.setVisible(context.getLogic().hasEntityAttribute(summon, Attribute.TAUNT));
		defaultToken.setVisible(!context.getLogic().hasEntityAttribute(summon, Attribute.TAUNT));
		divineShield.setVisible(context.getLogic().hasEntityAttribute(summon, Attribute.DIVINE_SHIELD));
		windfury.setVisible(context.getLogic().hasEntityAttribute(summon, Attribute.WINDFURY) || context.getLogic().hasEntityAttribute(summon, Attribute.MEGA_WINDFURY));
		if (context.getLogic().hasEntityAttribute(summon, Attribute.MEGA_WINDFURY)) {
			windfury.setText("x4");
		} else {
			windfury.setText("x2");
		}
		deathrattle.setVisible(context.getLogic().hasEntityAttribute(summon, Attribute.DEATHRATTLES));
		frozen.setVisible(context.getLogic().hasEntityAttribute(summon, Attribute.FROZEN));
		visualizeStealth(context, summon);
	}

	private void visualizeStealth(GameContext context, Summon summon) {
		Node token = context.getLogic().hasEntityAttribute(summon, Attribute.TAUNT) ? taunt : defaultToken;
		token.setOpacity(context.getLogic().hasEntityAttribute(summon, Attribute.STEALTH) ||
                context.getLogic().hasEntityAttribute(summon, Attribute.STEALTH_FOR_ONE_TURN) ? 0.5 : 1);
	}

}
