package com.is3.util;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class AveragePairSerde implements Serde<AveragePair> {
    @Override
    public Serializer<AveragePair> serializer() {
        return new AveragePairSerializer();
    }

    @Override
    public Deserializer<AveragePair> deserializer() {
        return new AveragePairDeserializer();
    }
}
