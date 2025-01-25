package com.github.exp.binance;

import com.binance.connector.futures.client.FuturesClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.exp.binance.model.ExchangeInfo;
import com.github.exp.binance.model.Reader;
import com.github.finfeat4j.api.Bar;
import org.slf4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import reactor.util.retry.Retry;

import java.io.File;
import java.io.FileWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class BinanceClient {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(BinanceClient.class);

    private final Reader reader = new Reader(new ObjectMapper().registerModule(new JavaTimeModule()).reader());

    private final FuturesClient client;
    private final SocketClient webSocketClient;

    public BinanceClient(FuturesClient client, SocketClient webSocketClient) {
        this.client = client;
        this.webSocketClient = webSocketClient;
    }

    public Flux<Tuple2<String, Bar>> connectWebsocket(Collection<String> symbols, KlineInterval interval) {
        return webSocketClient.connect(SocketClient.klineStreams(symbols.toArray(String[]::new), interval))
                .map(reader::readEvent)
                .filter(event -> event.kline() != null && event.kline().isClosed())
                .map(event -> {
                    var kline = event.kline();
                    return Tuples.of(event.symbol(), new Bar.BaseBar(
                            kline.closeTime().toEpochMilli(),
                            kline.open(),
                            kline.close(),
                            kline.high(),
                            kline.low(),
                            kline.volume(),
                            kline.trades()
                    ));
                });
    }


    public Flux<ExchangeInfo.Symbol> symbols(Set<String> contractTypes, Set<String> baseAssets, String quoteAsset) {
        var asset = Objects.requireNonNull(quoteAsset);
        return exchangeInfo()
                .flatMapMany(e -> Flux.fromIterable(e.symbols()))
                .filter(s -> contractTypes.contains(s.contractType()) && s.status().equals("TRADING") && s.quoteAsset().equals(asset) && baseAssets.contains(s.baseAsset()));
    }

    public Mono<ExchangeInfo> exchangeInfo() {
        return Mono.fromCallable(() -> client.market().exchangeInfo())
                .map(reader::exchangeInfo);
    }

    public Flux<Bar> loadBars(String symbol, KlineInterval interval, int total) {
        var duration = interval.getDuration();
        var now = Instant.now();
        long end = now.toEpochMilli();
        long start = Math.max(total > 0 ? Instant.now().minus(duration.multipliedBy(total)).toEpochMilli() : -1, 1567964700000L);
        Duration period = Duration.between(Instant.ofEpochMilli(start), now);
        AtomicLong totalToLoad = new AtomicLong((period.toMillis() / duration.toMillis()) + 1);
        AtomicLong startTime = new AtomicLong(start);
        if (totalToLoad.get() > 1) {
            return Flux.defer(() -> loadBars(symbol, interval, startTime.get(), end))
                    .filter(c -> c.timestamp() <= end && c.timestamp() >= start)
                    .doOnNext((Bar c) -> {
                        if (Instant.ofEpochMilli(c.timestamp()).isBefore(now)) {
                            totalToLoad.getAndDecrement();
                            startTime.set(c.timestamp());
                            if (duration.toMillis() > end - startTime.get()) {
                                totalToLoad.set(0);
                            }
                        }
                    })
                    .retryWhen(Retry.backoff(30, Duration.ofSeconds(60)))
                    .repeat(() -> totalToLoad.get() > 0);
        }
        return Flux.empty();
    }

    private Flux<Bar> loadBars(String symbol, KlineInterval interval, long start, long end) {
        var params = new LinkedHashMap<String, Object>();
        params.put("symbol", symbol);
        params.put("interval", interval.getValue());
        params.put("startTime", start);
        params.put("endTime", end);
        params.put("limit", 1500);
        try {
            return Flux.fromStream(reader.readBars(client.market().klines(params)));
        } catch (Exception e) {
            log.error("Error loading bars", e);
            return Flux.error(e);
        }
    }

    public Mono<Void> saveToFile(File file, String symbol, KlineInterval interval, int total) {
        return loadBars(symbol, interval, total)
                .collectList()
                .flatMap(bars -> {
                    try (var writer = new FileWriter(file, false)) {
                        writer.write("timestamp,open,close,high,low,vol,trades\n");
                        for (var b : bars) {
                            writer.write(String.format("%d,%s,%s,%s,%s,%s,%d\n",
                                    b.timestamp(),
                                    b.open().toPlainString(),
                                    b.close().toPlainString(),
                                    b.high().toPlainString(),
                                    b.low().toPlainString(),
                                    b.vol().toPlainString(),
                                    b.trades()));
                        }
                        return Mono.empty();
                    } catch (Exception e) {
                        log.error("Error writing to file", e);
                        return Mono.error(new RuntimeException(e));
                    }
                });
    }
}
