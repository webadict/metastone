package net.demilich.metastone.gui.cards;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.minions.Tribe;

public class CardTooltip extends CardToken {

	@FXML
	private Label tribeLabel;

	public CardTooltip() {
		super("CardTooltip.fxml");
	}

	@Override
	public void setCard(GameContext context, Card card, Player player) {
		super.setCard(context, card, player);
		descriptionLabel.setText(card.getDescription());
		if (card.getTribe() == Tribe.NONE) {
			tribeLabel.setVisible(false);
		} else {
			tribeLabel.setText(card.getTribe().toString());
			tribeLabel.setVisible(true);
		}
	}

}
