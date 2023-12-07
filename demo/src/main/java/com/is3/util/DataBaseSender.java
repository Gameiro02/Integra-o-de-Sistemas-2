package com.is3.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DataBaseSender {
    Base64Decoder encoder_decoder;

    public DataBaseSender() {
        encoder_decoder = new Base64Decoder();
    }

    public String sendPurchaseToDB(Integer purchase_id, Integer sock_id, Integer quantity, double price,
            String purchase_date, String sock_type) {

        // Converte o preço para double e codifica em Base64
        String encodedPrice = encoder_decoder.encodeDoubleToBase64(price);

        // Constrói a resposta com o preço codificado
        String response = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"int32\",\"optional\":false,\"field\":\"purchase_id\"},{\"type\":\"int32\",\"optional\":false,\"field\":\"sock_id\"},{\"type\":\"int32\",\"optional\":false,\"field\":\"quantity\"},{\"type\":\"bytes\",\"optional\":false,\"name\":\"org.apache.kafka.connect.data.Decimal\",\"version\":1,\"parameters\":{\"scale\":\"2\",\"connect.decimal.precision\":\"10\"},\"field\":\"price\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"purchase_date\"},{\"type\":\"string\",\"optional\":true,\"field\":\"sock_type\"}],\"optional\":false},\"payload\":{\"purchase_id\":"
                + purchase_id + ",\"sock_id\":" + sock_id + ",\"quantity\":" + quantity + ",\"price\":\""
                + encodedPrice + "\",\"purchase_date\":" + purchase_date + ",\"sock_type\":\"" + sock_type
                + "\"}}";

        return response;
    }

    public String sendSaleToDB(String sale_id, String sock_id, String buyer_id, String quantity_sold,
            String sale_price, String sale_date) {

        // "schema":{"type":"struct","fields":[{"type":"string","optional":true,"field":"sale_id"},{"type":"string","optional":true,"field":"sock_id"},{"type":"string","optional":true,"field":"buyer_id"},{"type":"string","optional":true,"field":"quantity_sold"},{"type":"string","optional":true,"field":"sale_price"},{"type":"string","optional":true,"field":"sale_date"}],"optional":false},"payload":{"sale_id":"3","sock_id":"101","buyer_id":"501","quantity_sold":"3","sale_price":"19.99","sale_date":"2023-12-05
        // 16:34:53.274"}}

        String response = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":true,\"field\":\"sale_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"sock_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"buyer_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"quantity_sold\"},{\"type\":\"string\",\"optional\":true,\"field\":\"sale_price\"},{\"type\":\"string\",\"optional\":true,\"field\":\"sale_date\"}],\"optional\":false},\"payload\":{\"sale_id\":\""
                + sale_id + "\",\"sock_id\":\"" + sock_id + "\",\"buyer_id\":\"" + buyer_id
                + "\",\"quantity_sold\":\"" + quantity_sold + "\",\"sale_price\":\"" + sale_price
                + "\",\"sale_date\":\"" + sale_date + "\"}}";

        return response;
    }

    public String sendSockFinancialsToDB(Integer sock_id, Double revenue, Double expenses, Double profit) {
        // Converte os valores financeiros para Base64
        String encodedRevenue = encoder_decoder.encodeDoubleToBase64(revenue);
        String encodedExpenses = encoder_decoder.encodeDoubleToBase64(expenses);
        String encodedProfit = encoder_decoder.encodeDoubleToBase64(profit);

        // Constrói a resposta com os valores financeiros codificados
        String response = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"int32\",\"optional\":false,\"field\":\"sock_id\"},{\"type\":\"bytes\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Decimal\",\"version\":1,\"parameters\":{\"scale\":\"2\",\"connect.decimal.precision\":\"10\"},\"field\":\"revenue_per_sale\"},{\"type\":\"bytes\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Decimal\",\"version\":1,\"parameters\":{\"scale\":\"2\",\"connect.decimal.precision\":\"10\"},\"field\":\"expenses_per_sale\"},{\"type\":\"bytes\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Decimal\",\"version\":1,\"parameters\":{\"scale\":\"2\",\"connect.decimal.precision\":\"10\"},\"field\":\"profit_per_sale\"}],\"optional\":false},\"payload\":{\"sock_id\":"
                + sock_id + ",\"revenue_per_sale\":\"" + encodedRevenue + "\",\"expenses_per_sale\":\""
                + encodedExpenses + "\",\"profit_per_sale\":\"" + encodedProfit + "\"}}";

        return response;
    }

    public String sendHighestProfitSupplierToDB(Integer supplier_id, String supplier_name, Double total_sales_profit) {
        // Converte o lucro total de vendas para Base64
        String encodedTotalSalesProfit = encoder_decoder.encodeDoubleToBase64(total_sales_profit);

        // Constrói a resposta com o lucro total de vendas codificado
        String response = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"int32\",\"optional\":false,\"field\":\"supplier_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"supplier_name\"},{\"type\":\"bytes\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Decimal\",\"version\":1,\"parameters\":{\"scale\":\"2\",\"connect.decimal.precision\":\"15\"},\"field\":\"total_sales_profit\"}],\"optional\":false},\"payload\":{\"supplier_id\":"
                + supplier_id + ",\"supplier_name\":\"" + supplier_name + "\",\"total_sales_profit\":\""
                + encodedTotalSalesProfit + "\"}}";

        return response;
    }

    public String sendHighestProfitSockTypeToDB(Integer sock_id, String sock_type, Double total_sales_profit) {
        // Converte o lucro total de vendas para Base64
        String encodedTotalSalesProfit = encoder_decoder.encodeDoubleToBase64(total_sales_profit);

        // Constrói a resposta com o lucro total de vendas codificado
        String response = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"int32\",\"optional\":false,\"field\":\"id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"sock_type\"},{\"type\":\"bytes\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Decimal\",\"version\":1,\"parameters\":{\"scale\":\"2\",\"connect.decimal.precision\":\"10\"},\"field\":\"profit\"}],\"optional\":false},\"payload\":{\"id\":"
                + sock_id + ",\"sock_type\":\"" + sock_type + "\",\"profit\":\"" + encodedTotalSalesProfit + "\"}}";

        return response;
    }

    public String sendFinancialMetricsLastHour(Integer metric_id, Double total_revenue, Double total_expenses,
            Double total_profit, LocalDateTime timestamp) {
        // Converte os valores financeiros para Base64
        String encodedTotalRevenue = encoder_decoder.encodeDoubleToBase64(total_revenue);
        String encodedTotalExpenses = encoder_decoder.encodeDoubleToBase64(total_expenses);
        String encodedTotalProfit = encoder_decoder.encodeDoubleToBase64(total_profit);

        // Converte LocalDateTime para milissegundos desde a epoch Unix
        long timestampMillis = timestamp.toInstant(ZoneOffset.UTC).toEpochMilli();

        // Constrói a resposta com os valores financeiros e o timestamp codificados
        String response = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"int32\",\"optional\":false,\"field\":\"metric_id\"},{\"type\":\"bytes\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Decimal\",\"version\":1,\"parameters\":{\"scale\":\"2\",\"connect.decimal.precision\":\"15\"},\"field\":\"total_revenue\"},{\"type\":\"bytes\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Decimal\",\"version\":1,\"parameters\":{\"scale\":\"2\",\"connect.decimal.precision\":\"15\"},\"field\":\"total_expenses\"},{\"type\":\"bytes\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Decimal\",\"version\":1,\"parameters\":{\"scale\":\"2\",\"connect.decimal.precision\":\"15\"},\"field\":\"total_profit\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"timestamp\"}],\"optional\":false},\"payload\":{\"metric_id\":"
                + metric_id + ",\"total_revenue\":\"" + encodedTotalRevenue + "\",\"total_expenses\":\""
                + encodedTotalExpenses + "\",\"total_profit\":\"" + encodedTotalProfit + "\",\"timestamp\":"
                + timestampMillis + "}}";

        return response;
    }

    public String sendAverageSpentPerSockType(Integer id, String sock_type, Double average_spent) {
        // Converte o valor médio gasto para Base64
        String encodedAverageSpent = encoder_decoder.encodeDoubleToBase64(average_spent);

        // Constrói a resposta com o valor médio gasto codificado
        String response = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"sock_type\"},{\"type\":\"bytes\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Decimal\",\"version\":1,\"parameters\":{\"scale\":\"2\",\"connect.decimal.precision\":\"10\"},\"field\":\"average_spent\"}],\"optional\":false},\"payload\":{\"sock_type\":\""
                + sock_type + "\",\"average_spent\":\"" + encodedAverageSpent + "\"}}";

        return response;
    }

    public String sendAverageSpentOverall(Integer id, Double average_spent) {
        // Converte o valor médio gasto para Base64
        String encodedAverageSpent = encoder_decoder.encodeDoubleToBase64(average_spent);

        // Constrói a resposta com o valor médio gasto codificado
        String response = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"int32\",\"optional\":false,\"field\":\"id\"},{\"type\":\"bytes\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Decimal\",\"version\":1,\"parameters\":{\"scale\":\"2\",\"connect.decimal.precision\":\"10\"},\"field\":\"average_spent\"}],\"optional\":false},\"payload\":{\"id\":"
                + id + ",\"average_spent\":\"" + encodedAverageSpent + "\"}}";

        return response;
    }
}
