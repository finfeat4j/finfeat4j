package com.github.exp.binance.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.finfeat4j.api.Bar;

import java.math.BigDecimal;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Reader {

    private final ObjectReader reader;

    public Reader(ObjectReader reader) {
        this.reader = reader;
    }

    public SocketEvent readEvent(String json) {
        try {
            return reader.readValue(json, SocketEvent.CombinedStreamEvent.class).data();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Stream<Bar> readBars(String json) {
        try {
            var node = reader.readTree(json);
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(node.iterator(), 0), false)
                    .map(Reader::parseKline);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ExchangeInfo exchangeInfo(String json) {
        try {
            return reader.readValue(json, ExchangeInfo.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *     [0] 1499040000000,      // Open time
     *     [1] "0.01634790",       // Open
     *     [2] "0.80000000",       // High
     *     [3] "0.01575800",       // Low
     *     [4] "0.01577100",       // Close
     *     [5] "148976.11427815",  // Volume
     *     [6] 1499644799999,      // Close time
     *     [7] "2434.19055334",    // Quote asset volume
     *     [8] 308,                // Number of trades
     *     [9] "1756.87402397",    // Taker buy base asset volume
     *     [10] "28.46694368",      // Taker buy quote asset volume
     *     [11] "17928899.62484339" // Ignore.
     *
     * @param node
     * @return
     */
    public static Bar.BaseBar parseKline(JsonNode node) {
        return new Bar.BaseBar(
                node.get(6).asLong(),
                new BigDecimal(node.get(1).asText()),
                new BigDecimal(node.get(4).asText()),
                new BigDecimal(node.get(2).asText()),
                new BigDecimal(node.get(3).asText()),
                new BigDecimal(node.get(5).asText()),
                node.get(8).asLong()
        );
    }

    public static Bar.BaseBar parseKlineStream(JsonNode node) {
        return new Bar.BaseBar(
                node.get(6).asLong(),
                new BigDecimal(node.get(1).asText()),
                new BigDecimal(node.get(4).asText()),
                new BigDecimal(node.get(2).asText()),
                new BigDecimal(node.get(3).asText()),
                new BigDecimal(node.get(5).asText()),
                node.get(8).asLong()
        );
    }

}
