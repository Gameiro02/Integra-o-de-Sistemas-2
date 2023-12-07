package com.is3.util;

import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Serializer;

public class ProfitableSockTypeSerializer implements Serializer<ProfitableSockType> {
    private Gson gson = new Gson();

    @Override
    public byte[] serialize(String topic, ProfitableSockType data) {
        return (data == null) ? null : gson.toJson(data).getBytes();
    }
}