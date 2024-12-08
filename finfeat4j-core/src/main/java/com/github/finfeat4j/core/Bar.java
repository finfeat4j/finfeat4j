package com.github.finfeat4j.core;

import java.math.BigDecimal;

/**
 * Basic Bar which should have:
 * 1. timestamp
 * 2. open
 * 3. close
 * 4. high
 * 5. low
 * 6. volume
 * 7. trades
 */
public interface Bar {

    long timestamp();

    BigDecimal open();

    BigDecimal close();

    BigDecimal high();

    BigDecimal low();

    BigDecimal vol();

    long trades();

    record BaseBar(
        long timestamp,
        BigDecimal open,
        BigDecimal close,
        BigDecimal high,
        BigDecimal low,
        BigDecimal vol,
        long trades
    ) implements Bar {

        public BaseBar {
            if (close == null || high == null || low == null || open == null || vol == null) {
                throw new IllegalArgumentException("Candle values cannot be null");
            }
            if (timestamp < 0 || trades < 0) {
                throw new IllegalArgumentException("Candle timestamp and trades must be positive");
            }
        }
    }

}
