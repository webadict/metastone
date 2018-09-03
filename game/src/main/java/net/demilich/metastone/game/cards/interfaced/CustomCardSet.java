package net.demilich.metastone.game.cards.interfaced;

public class CustomCardSet implements CardSetImplementation {
    private String name;

    CustomCardSet(String name) {
        this.name = name;
        CardSetImplementation.cardSets.put(name, this);
    }

    @Override
    public String getName() {
        return name;
    }
}
