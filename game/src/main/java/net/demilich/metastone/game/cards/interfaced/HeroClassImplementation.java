package net.demilich.metastone.game.cards.interfaced;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public interface HeroClassImplementation {

    Map<String, HeroClassImplementation> heroClasses = new HashMap<>();

    static HeroClassImplementation[] values() {
        return heroClasses.values().toArray(new HeroClassImplementation[0]);
    }

    static HeroClassImplementation valueOf(String name) {
        if (heroClasses.containsKey(name)) {
            return heroClasses.get(name);
        }
        return null;
    }

    static void initializeImplementations() {
        for (HeroClass heroClass : HeroClass.values()) {
            heroClasses.put(heroClass.getName(), heroClass);
        }
        for (NonHeroClass heroClass : NonHeroClass.values()) {
            heroClasses.put(heroClass.getName(), heroClass);
        }
        for (NonBaseHeroClass heroClass : NonBaseHeroClass.values()) {
            heroClasses.put(heroClass.getName(), heroClass);
        }
    }

    String getName();
    boolean isBaseClass();
}
