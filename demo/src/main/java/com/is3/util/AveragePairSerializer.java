package com.is3.util;

import org.apache.kafka.common.serialization.Serializer;
import com.google.gson.Gson;

public class AveragePairSerializer implements Serializer<AveragePair> {
    private Gson gson = new Gson();

    @Override
    public byte[] serialize(String topic, AveragePair data) {
        if (data == null) {
            return null;
        }
        return gson.toJson(data).getBytes();
    }
}
