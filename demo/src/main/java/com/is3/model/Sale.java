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
public class Sale {

    @Getter @Setter
    private int sale_id;

    @Getter @Setter
    private int sock_id;

    @Getter @Setter
    private int buyer_id;

    @Getter @Setter
    private int quantity_sold;

    @Getter
    private double sale_price;

    @Getter @Setter
    private SockType sock_type;

    public void setPricePerPair(double sale_price) {
        if (sale_price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.sale_price = sale_price;
    }

    @Getter @Setter
    private LocalDateTime sale_date;

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