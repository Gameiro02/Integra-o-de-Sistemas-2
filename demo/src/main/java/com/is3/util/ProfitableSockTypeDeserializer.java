package com.is3.util;

import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Deserializer;

public class ProfitableSockTypeDeserializer implements Deserializer<ProfitableSockType> {
    private Gson gson = new Gson();

    @Override
    public ProfitableSockType deserialize(String topic, byte[] data) {
        return (data == null) ? null : gson.fromJson(new String(data), ProfitableSockType.class);
    }
}
