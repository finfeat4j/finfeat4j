package com.github.exp.binance;

import java.time.Duration;

public enum KlineInterval {
    ONE_MINUTE("1m", Duration.ofMinutes(1)),
    THREE_MINUTES("3m", Duration.ofMinutes(3)),
    FIVE_MINUTES("5m", Duration.ofMinutes(5)),
    FIFTEEN_MINUTES("15m", Duration.ofMinutes(15)),
    THIRTY_MINUTES("30m", Duration.ofMinutes(30)),
    ONE_HOUR("1h", Duration.ofHours(1)),
    TWO_HOURS("2h", Duration.ofHours(2)),
    FOUR_HOURS("4h", Duration.ofHours(4)),
    SIX_HOURS("6h", Duration.ofHours(6)),
    EIGHT_HOURS("8h", Duration.ofHours(8)),
    TWELVE_HOURS("12h", Duration.ofHours(12)),
    ONE_DAY("1d", Duration.ofDays(1)),
    THREE_DAYS("3d", Duration.ofDays(3)),
    ONE_WEEK("1w", Duration.ofDays(7));

    private final String value;
    private final Duration duration;

    KlineInterval(String value, Duration duration) {
        this.value = value;
        this.duration = duration;
    }

    public String getValue() {
        return value;
    }

    public Duration getDuration() {
        return duration;
    }
}
