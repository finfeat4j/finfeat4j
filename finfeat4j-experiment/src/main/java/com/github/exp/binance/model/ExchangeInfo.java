package com.github.exp.binance.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExchangeInfo(List<Symbol> symbols) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Symbol(
        String symbol,
        String pair,
        String status,
        String baseAsset,
        String quoteAsset,
        String contractType,
        List<Filter> filters
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Filter(
        String filterType,
        String minPrice,
        String maxPrice,
        String tickSize,
        String minQty,
        String maxQty,
        String stepSize,
        String notional,
        String multiplierUp,
        String multiplierDown,
        String multiplierDecimal,
        Long limit
    ) {}
}
