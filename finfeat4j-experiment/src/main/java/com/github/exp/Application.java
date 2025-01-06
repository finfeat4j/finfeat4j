package com.github.exp;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.github.exp.binance.*;
import com.github.exp.telegram.Bot;
import com.github.finfeat4j.fs.OnlineClassifier;
import com.github.finfeat4j.validation.ValidationMetricSet;
import com.github.finfeat4j.strategy.MoaClassifier;
import com.github.finfeat4j.api.Classifier;
import com.github.finfeat4j.core.Dataset;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;
import reactor.netty.transport.NameResolverProvider;

import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@SpringBootApplication
public class Application {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Application.class);

    @Configuration
    @ConfigurationProperties(prefix = "config")
    public static class Config {
        private String chatId;
        private String botToken;


        private Map<String, StrategyConfig> strategies = new HashMap<>();

        public record StrategyConfig(Path sfaFile, String features, int trainSize) {}

        public Map<String, StrategyConfig> getStrategies() {
            return strategies;
        }

        public void setStrategies(Map<String, StrategyConfig> strategies) {
            this.strategies = strategies;
        }

        public String getBotToken() {
            return botToken;
        }

        public void setBotToken(String botToken) {
            this.botToken = botToken;
        }

        public String getChatId() {
            return chatId;
        }

        public void setChatId(String chatId) {
            this.chatId = chatId;
        }

    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public BinanceClient binanceClient(WebSocketClient client) {
        return new BinanceClient(new UMFuturesClientImpl(), new SocketClient("wss://fstream.binance.com", client));
    }

    @Bean
    WebSocketClient webSocketClient() {
        return new ReactorNettyWebSocketClient(
                HttpClient.create()
                        .resolver((NameResolverProvider.NameResolverSpec spec) -> {
                            spec
                                    .queryTimeout(Duration.ofSeconds(30))
                                    .cacheMaxTimeToLive(Duration.ofMinutes(2))
                                    .maxQueriesPerResolve(Integer.MAX_VALUE);
                        })
                        .disableRetry(false)
                        .responseTimeout(Duration.ofSeconds(10))
                        .doOnError((req, th) -> {
                            Application.log.error("REQUEST ERROR: {}", req.resourceUrl(), th);
                        }, (resp, th) -> {
                            Application.log.error("RESPONSE ERROR: {}", resp.resourceUrl(), th);
                        }).doOnConnected((c) -> {
                            c.addHandlerLast("idleState", new ReadTimeoutHandler(10, TimeUnit.SECONDS));
                        }).doOnDisconnected((c) -> {
                            Application.log.error("DISCONNECTED: {}", c.channel().remoteAddress());
                        }),
                WebsocketClientSpec.builder()
                        .handlePing(false)
                        .maxFramePayloadLength(Integer.MAX_VALUE)
        );
    }

    @Bean
    public TelegramBotsLongPollingApplication bots(Config config) {
        var botsApplication = new TelegramBotsLongPollingApplication();
        try {
            botsApplication.registerBot(config.botToken, new Bot(config));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return botsApplication;
    }

    @Bean
    public Bot bot(Config config) {
        return new Bot(config);
    }

    @Bean
    StrategyHandler strategyHandler(BinanceClient client, Config config, Bot bot) {
        var baseAssets = Set.of("BTC", "ETH");
        var contractTypes = Set.of("PERPETUAL");
        var defaultConfig = config.strategies.get("default");
        var defaultSfaFile = defaultConfig.sfaFile.toFile();
        var defaultFeatures = Dataset.features(defaultConfig.features, Dataset.DEFAULT);

        Supplier<ValidationMetricSet> metrics = ValidationMetricSet::new;
        Supplier<Classifier> strategySupplier = () -> new MoaClassifier("efdt.VFDT -d FastNominalAttributeClassObserver");
        Supplier<OnlineClassifier> defaultRunner = () -> new OnlineClassifier(
            IndicatorSupplier::get, metrics, strategySupplier, defaultSfaFile, defaultFeatures, 0.0004d, defaultConfig.trainSize()
        );
        var runners = new HashMap<String, OnlineClassifier>();
        for (var strategy : config.strategies.entrySet()) {
            if (strategy.getKey().equals("default")) {
                continue;
            }
            var value = strategy.getValue();
            var sfaPath = value.sfaFile;
            var sfaFile = defaultSfaFile;
            if (sfaPath != null) {
                sfaFile = sfaPath.toFile();
            }
            var f = value.features;
            var trainSize = value.trainSize();
            if (trainSize == 0) {
                trainSize = defaultConfig.trainSize();
            }
            var features = defaultFeatures;
            if (f != null) {
                features = Dataset.features(f, Dataset.DEFAULT);
            }
            runners.put(strategy.getKey(), new OnlineClassifier(
                IndicatorSupplier::get, metrics, strategySupplier, sfaFile, features, 0.0004d, trainSize
            ));
        }
        return new StrategyHandler(client, KlineInterval.ONE_DAY, baseAssets, contractTypes, "USDT", defaultRunner, runners, bot);
    }

    @Bean
    DisposableBean contextShutdownGate() {
        CountDownLatch latch = new CountDownLatch(1);
        Thread await = new Thread(() -> {
            try {
                latch.await();
            } catch(InterruptedException ie) {
                Application.log.error("contextAwait interrupted", ie);
            }
        });
        await.setDaemon(false);
        await.start();
        return () -> {
            Application.log.info("Disposing context shutdown gate");
            latch.countDown();
        };
    }
}
