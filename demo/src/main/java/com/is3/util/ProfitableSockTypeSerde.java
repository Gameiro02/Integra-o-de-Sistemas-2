package com.is3.util;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class ProfitableSockTypeSerde implements Serde<ProfitableSockType> {
    @Override
    public Serializer<ProfitableSockType> serializer() {
        return new ProfitableSockTypeSerializer();
    }

    @Override
    public Deserializer<ProfitableSockType> deserializer() {
        return new ProfitableSockTypeDeserializer();
    }
}