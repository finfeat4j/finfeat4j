package com.github.exp.binance.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SocketEvent(
        @JsonProperty("e") String eventType,
        @JsonProperty("E") Instant eventTime,
        @JsonProperty("s") String symbol,
        @JsonProperty("k") Kline kline
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Kline(
        @JsonProperty("t") Instant openTime,
        @JsonProperty("T") Instant closeTime,
        @JsonProperty("o") BigDecimal open,
        @JsonProperty("c") BigDecimal close,
        @JsonProperty("h") BigDecimal high,
        @JsonProperty("l") BigDecimal low,
        @JsonProperty("v") BigDecimal volume,
        @JsonProperty("n") long trades,
        @JsonProperty("x") boolean isClosed
    ) {}

    public record CombinedStreamEvent(
        String stream,
        SocketEvent data
    ) {}
}
