package net.demilich.metastone.game.cards.interfaced;

import java.util.HashMap;
import java.util.Map;

public interface CardSetImplementation {

    Map<String, CardSetImplementation> cardSets = new HashMap<>();

    static CardSetImplementation[] values() {
        return cardSets.values().toArray(new CardSetImplementation[0]);
    }

    static CardSetImplementation valueOf(String name) {
        if (cardSets.containsKey(name)) {
            return cardSets.get(name);
        }
        return null;
    }

    static void initializeImplementations() {
        for (BaseCardSet cardSet : BaseCardSet.values()) {
            cardSets.put(cardSet.getName(), cardSet);
        }
    }

    String getName();
}
