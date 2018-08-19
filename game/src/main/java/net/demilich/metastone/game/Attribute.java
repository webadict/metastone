package net.demilich.metastone.game;

public enum Attribute {
	// Base card attributes
	DEBUG,
	BASE_MANA_COST,
	HP,
	ATTACK,
	ATTACK_BONUS,
	MAX_HP,
	ARMOR,
	TEMPORARY_ATTACK_BONUS,
	HP_BONUS,
	AURA_ATTACK_BONUS,
	AURA_HP_BONUS,
	BASE_HP,
	BASE_ATTACK,
	CONDITIONAL_ATTACK_BONUS,
	RACE,

	// Death related
	DESTROYED,
	PENDING_DESTROY,

	// Card attributes
	COUNTERED,
	ENRAGABLE,
	ENRAGED,
	EXTRA_ATTACKS,
	FATIGUE,
	GHOSTLY,
	MANA_COST_MODIFIER,
	NUMBER_OF_ATTACKS,
	SILENCED,
	SUMMONING_SICKNESS,

	// Kinda Keywords
	BATTLECRY,
	CHOOSE_ONE,
	COMBO,
	DEATHRATTLES,
	DECK_TRIGGER,
	DISCOVER,
	PASSIVE_TRIGGER,
	QUEST,
	SECRET,

	// Keywords
	CANNOT_ATTACK,
	CANNOT_ATTACK_HERO_ON_SUMMON,
	CANNOT_ATTACK_HEROES,
	CHARGE,
	DIVINE_SHIELD,
	ECHO,
	FROZEN,
	HERO_POWER_DAMAGE,
	IMMUNE,
	IMMUNE_HERO,
	IMMUNE_WHILE_ATTACKING,
	LIFESTEAL,
	MAGNETIC,
	MEGA_WINDFURY,
	OPPONENT_SPELL_DAMAGE,
	OVERLOAD,
	POISONOUS,
	RUSH,
	SPELL_DAMAGE,
	STEALTH,
	TAUNT,
	UNLIMITED_ATTACKS,
	UNTARGETABLE_BY_SPELLS,
	WINDFURY,

	// Specialized attributes
	ATTACK_EQUALS_HP,
	BOTH_CHOOSE_ONE_OPTIONS,
	CANNOT_REDUCE_HP_BELOW_1,
	DOUBLE_BATTLECRIES,
	DOUBLE_DEATHRATTLES,
	HEAL_AMPLIFY_MULTIPLIER,
	HERO_POWER_CAN_TARGET_MINIONS,
	INVERT_HEALING,
	MURLOCS_COST_HEALTH,
	SPELL_AMPLIFY_MULTIPLIER,
	SPELL_DAMAGE_MULTIPLIER,
	SPELLS_COST_HEALTH,
	TAKE_DOUBLE_DAMAGE,

	// Hidden attributes
	AURA_UNTARGETABLE_BY_SPELLS,
	DIED_ON_TURN,
	LAST_HIT,
	SHADOWFORM,
	CTHUN_ATTACK_BUFF,
	CTHUN_HEALTH_BUFF,
	CTHUN_TAUNT,
	JADE_BUFF,
	ALL_RANDOM_FINAL_DESTINATION,
	ALL_RANDOM_YOGG_ONLY_FINAL_DESTINATION,

	// Hero attributes
	HERO_POWER_USAGES,

	// AI flags
	MARKED_FOR_DEATH, 
}
