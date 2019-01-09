package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.cards.interfaced.CardSetImplementation;
import net.demilich.metastone.game.cards.interfaced.HeroClassImplementation;
import net.demilich.metastone.game.entities.Attribute;
import net.demilich.metastone.game.entities.AttributeMap;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;

public abstract class CardDesc {

	public String id;
	public String name;
	public String description;
	public CardType type;
	public HeroClassImplementation heroClass;
	public HeroClassImplementation[] heroClasses;
	public Rarity rarity;
	public CardSetImplementation set;
	public int baseManaCost;
	public boolean collectible = true;
	public AttributeMap attributes;
	public int fileFormatVersion = 1;

	public CardCostModifierDesc manaCostModifier;

	//public ValueProviderDesc manaCostModifier;
	public TriggerDesc passiveTrigger;
	public TriggerDesc deckTrigger;

	public abstract Card createInstance();

    /**
     * Represents a key-value collection of {@link Attribute}. {@link AttributeMap} is a {@link java.util.Map} type, so in
     * JSON, it will be represented by a bracketed object.
     * <p>
     * For example, a spell with lifesteal will have a field {@code "attributes"} that looks like:
     * <pre>
     *     "attributes": {
     *         "LIFESTEAL": true
     *     }
     * </pre>
     * Not all attributes are {@link Boolean}. For example, an {@link Attribute#OVERLOAD} card that reads, "Overload: 3"
     * will have a value equal to amount of overload the card will give:
     * <pre>
     *     "attributes": {
     *         "OVERLOAD": 3
     *     }
     * </pre>
     *
     * @see Attribute for a full description of attributes. Some of them are not appropriate to put on a card, because
     * 		they are ephemeral (that is, they are only on a {@link net.demilich.metastone.game.entities.Entity} while it is
     * 		in play, not on a card definition like {@link CardDesc}).
     */
    public AttributeMap getAttributes() {
        return attributes;
    }

    public void setAttributes(AttributeMap attributes) {
        this.attributes = attributes;
    }

}
