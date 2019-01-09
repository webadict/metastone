package net.demilich.metastone.game.entities.minions;

public enum Tribe {
	NONE,

    ALL,

	BEAST,
	DEMON,
	DRAGON,
	ELEMENTAL,
	MECH,
	MURLOC,
	PIRATE,
	TOTEM,
    ;

	public boolean isTribe(Tribe tribe) {
	    if (this == ALL && tribe != NONE) {
	        return true;
        }
        return this == tribe;
    }
}
