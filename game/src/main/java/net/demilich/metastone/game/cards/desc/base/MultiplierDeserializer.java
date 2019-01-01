package net.demilich.metastone.game.cards.desc.base;

import com.google.gson.JsonObject;
import net.demilich.metastone.game.cards.desc.ParseValueType;
import net.demilich.metastone.game.cards.desc.multiplier.Multiplier;
import net.demilich.metastone.game.cards.desc.multiplier.MultiplierArg;
import net.demilich.metastone.game.cards.desc.multiplier.MultiplierDesc;

import java.util.Map;

public class MultiplierDeserializer extends Deserializer<Multiplier, MultiplierArg> {

    public MultiplierDeserializer() {
        super(Multiplier.class);
    }

    @Override
    protected Map<MultiplierArg, Object> createMap(Class<? extends Multiplier> tClass, JsonObject jsonData) {
        Map<MultiplierArg, Object> map = MultiplierDesc.build(tClass);
        parseArgument(MultiplierArg.CARD_TYPE, jsonData, map, ParseValueType.CARD_TYPE);
        return map;
    }
}
