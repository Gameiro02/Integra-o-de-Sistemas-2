package com.is3.model;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.time.LocalDateTime;

import com.is3.util.LocalDateTimeAdapter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Purchase {
    @Getter @Setter
    private int purchase_id;

    @Getter @Setter
    private int sock_id;

    @Getter @Setter
    private int quantity;

    @Getter
    private double price;

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
    }

    @Getter @Setter
    private LocalDateTime purchase_date;

    @Getter @Setter
    private SockType sock_type;

    public String toJson() {
        try {
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
            return gson.toJson(this);
        } catch (JsonSyntaxException e) {
            System.err.println("Error serializing object to JSON: " + e.getMessage());
            return null;
        }
    }
}
