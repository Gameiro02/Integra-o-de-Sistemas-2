package com.is3.util;

import org.apache.kafka.common.serialization.Deserializer;
import com.google.gson.Gson;

public class AveragePairDeserializer implements Deserializer<AveragePair> {
    private Gson gson = new Gson();

    @Override
    public AveragePair deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        return gson.fromJson(new String(data), AveragePair.class);
    }
}
