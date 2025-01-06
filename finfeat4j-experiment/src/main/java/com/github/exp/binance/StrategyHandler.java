package com.github.exp.binance;

import com.github.exp.binance.model.ExchangeInfo;
import com.github.exp.telegram.Bot;
import com.github.finfeat4j.api.Bar;
import com.github.finfeat4j.fs.OnlineClassifier;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static reactor.function.TupleUtils.consumer;

public class StrategyHandler {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(StrategyHandler.class);

    private final BinanceClient client;
    private final Map<String, TradeSymbol> tradeSymbolMap = new ConcurrentHashMap<>();
    private final Map<String, OnlineClassifier> runners;
    private final Set<String> baseAssets;
    private final Set<String> contractTypes;
    private final String quoteAsset;
    private final Supplier<OnlineClassifier> defaultRunner;
    private final KlineInterval interval;
    private Disposable subscription;
    private Bot bot;

    public StrategyHandler(BinanceClient client, KlineInterval interval, Set<String> baseAssets, Set<String> contractTypes,
                           String quoteAsset, Supplier<OnlineClassifier> defaultRunner, Map<String, OnlineClassifier> runners, Bot bot) {
        this.client = client;
        this.interval = interval;
        this.baseAssets = baseAssets;
        this.contractTypes = contractTypes;
        this.quoteAsset = quoteAsset;
        this.defaultRunner = defaultRunner;
        this.runners = runners;
        this.bot = bot;
    }

    @PostConstruct
    public void connect() {
        final Retry retry = Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(30))
                .transientErrors(true)
                .doBeforeRetry(signal -> log.debug("Retrying... (" + signal.toString() + ")", signal.failure()));
        if (this.subscription != null && !this.subscription.isDisposed()) {
            this.subscription.dispose();
        }

        // Handle the retry without calling connect() recursively.
        this.subscription = this.init()
                .doOnError(t -> log.error("Error while connecting to WebSocket", t))
                .doOnTerminate(() -> log.info("WebSocket connection terminated"))  // Avoid recursive connect calls here
                .retryWhen(retry)
                .subscribe();
    }

    public Mono<Void> init() {
        this.tradeSymbolMap.clear();
        var connectWebSocket = Flux.defer(() -> client.connectWebsocket(tradeSymbolMap.keySet(), interval)
                .doOnNext(consumer(this::updateBar)));
        return this.client.symbols(contractTypes, baseAssets, quoteAsset)
                .map(TradeSymbol::new)
                .flatMap(s -> Mono.zip(Mono.just(s), client.loadBars(s.symbol(), KlineInterval.ONE_DAY, -1).collectList()))
                .doOnNext(consumer(this::initStrategyRunner))
                .thenMany(connectWebSocket)
                .then();
    }

    private void initStrategyRunner(TradeSymbol s, List<Bar> bars) {
        var runner = runners.computeIfAbsent(s.symbol(), k -> defaultRunner.get());
        runner.init(bars);
        var tradeResult = runner.getLastTradeMetric();
        var metrics = runner.getMetrics();
        var metricNames = runner.getMetricNames();
        var builder = new StringBuilder();
        for (int i = 0; i < metrics.length; i++) {
            builder.append(metricNames[i]).append(": ").append(metrics[i]).append("\n");
        }
        this.bot.sendMessage(s.symbol() + "\n" + tradeResult.toString() + "\n" + builder.toString());
        this.tradeSymbolMap.put(s.symbol(), s);
    }

    private void updateBar(String symbol, Bar bar) {
        var strategy = runners.get(symbol);
        strategy.update(bar);
        var tradeResult = strategy.getLastTradeMetric();
        var metrics = strategy.getMetrics();
        var metricNames = strategy.getMetricNames();
        var builder = new StringBuilder();
        for (int i = 0; i < metrics.length; i++) {
            builder.append(metricNames[i]).append(": ").append(metrics[i]).append("\n");
        }
        this.bot.sendMessage(symbol + "\n" + tradeResult.toString() + "\n" + builder.toString());
    }

    public record TradeSymbol(
            String symbol,
            BigDecimal minQty,
            int priceScale,
            int qtyScale,
            BigDecimal priceStep,
            BigDecimal qtyStep,
            BigDecimal minNotional) {

        public TradeSymbol(ExchangeInfo.Symbol symbol) {
            this(
                    symbol.symbol(),
                    getFilterValue(symbol, "LOT_SIZE", ExchangeInfo.Filter::minQty),
                    getFilterScale(symbol, "PRICE_FILTER", ExchangeInfo.Filter::tickSize),
                    getFilterScale(symbol, "LOT_SIZE", ExchangeInfo.Filter::stepSize),
                    getFilterValue(symbol, "PRICE_FILTER", ExchangeInfo.Filter::tickSize),
                    getFilterValue(symbol, "LOT_SIZE", ExchangeInfo.Filter::stepSize),
                    getFilterValue(symbol, "MIN_NOTIONAL", ExchangeInfo.Filter::notional)
            );
        }

        private static BigDecimal getFilterValue(ExchangeInfo.Symbol symbol, String filterType,
                                                 Function<ExchangeInfo.Filter, String> mapper) {
            return symbol.filters().stream()
                    .filter(f -> f.filterType().equals(filterType))
                    .findFirst()
                    .map(mapper)
                    .map(BigDecimal::new)
                    .orElseThrow(() -> new IllegalArgumentException("No " + filterType + " filter found"));
        }

        private static int getFilterScale(ExchangeInfo.Symbol symbol, String filterType,
                                          Function<ExchangeInfo.Filter, String> mapper) {
            return symbol.filters().stream()
                    .filter(f -> f.filterType().equals(filterType))
                    .findFirst()
                    .map(mapper)
                    .map(t -> new BigDecimal(t).stripTrailingZeros().scale())
                    .orElseThrow(() -> new IllegalArgumentException("No " + filterType + " filter found"));
        }
    }
}
