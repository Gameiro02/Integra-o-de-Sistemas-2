package com.is3.model;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sock {
    @Getter @Setter
    private int sock_id;

    @Getter @Setter
    private SockType type;

    @Getter
    private double price;

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
    }

    @Getter @Setter
    private int quantity_available;

    @Getter @Setter
    private int supplier_id;

    public String toJson() {
        try {
            Gson gson = new Gson();
            return gson.toJson(this);
        } catch (JsonSyntaxException e) {
            System.err.println("Error serializing object to JSON: " + e.getMessage());
            return null;
        }
    }
}