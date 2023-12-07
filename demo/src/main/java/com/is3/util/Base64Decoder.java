package com.is3.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Base64;
import com.fasterxml.jackson.core.Base64Variants;

public class Base64Decoder {
    public double decodeBase64ToDouble(String base64String) {
        // Decodifica a string Base64 para um array de bytes
        byte[] bytes = Base64.getDecoder().decode(base64String);

        // Converte o array de bytes em um BigInteger
        BigInteger bigIntegerValue = new BigInteger(bytes);

        // Calcula o valor double considerando a escala
        return bigIntegerValue.doubleValue() / Math.pow(10, 2);
    }

    public String encodeDoubleToBase64(double value) {
        // Converte o valor double para BigDecimal, aplicando a escala
        BigDecimal bigDecimalValue = BigDecimal.valueOf(value).setScale(2, BigDecimal.ROUND_HALF_UP);

        // Converte o BigDecimal para BigInteger (removendo a parte fracionária)
        BigInteger bigIntegerValue = bigDecimalValue.unscaledValue();

        // Converte o BigInteger para um array de bytes
        byte[] bytes = bigIntegerValue.toByteArray();

        // Codifica o array de bytes para uma string Base64 usando a variante
        // MIME_NO_LINEFEEDS
        return Base64Variants.MIME_NO_LINEFEEDS.encode(bytes);
    }
}
