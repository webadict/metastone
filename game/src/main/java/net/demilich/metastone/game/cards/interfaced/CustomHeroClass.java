package net.demilich.metastone.game.cards.interfaced;

public class CustomHeroClass implements HeroClassImplementation {
    private String name;
    // TODO: Make custom classes able to be base.
    private boolean isBaseClass = false;

    CustomHeroClass(String name) {
        this.name = name;
        HeroClassImplementation.heroClasses.put(name, this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isBaseClass() {
        return isBaseClass;
    }
}
