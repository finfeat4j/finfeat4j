package com.github.exp.binance;

import org.slf4j.Logger;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SocketClient implements WebSocketHandler {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(SocketClient.class);

    private final String wssUrl;
    private final WebSocketClient client;
    private Disposable connection;
    private String[] streams;

    private final Sinks.Many<String> messageSink = Sinks.many().multicast().directBestEffort();

    public SocketClient(String wssUrl, WebSocketClient client) {
        this.wssUrl = wssUrl;
        this.client = client;
    }

    public Flux<String> connect(String[] streams) {
        this.streams = streams;
        if (connection != null && !connection.isDisposed()) {
            connection.dispose();
        }
        log.info("Connecting to {}", Arrays.toString(streams));
        var streamPath = Arrays.stream(streams).collect(Collectors.joining("/"));
        connection = client.execute(URI.create(String.format("%s/stream?streams=%s", wssUrl, streamPath)), this)
                .subscribeOn(Schedulers.single())
                .subscribe();
        return messageSink.asFlux();
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.receive()
            .filter((WebSocketMessage m) -> m.getType().equals(WebSocketMessage.Type.TEXT))
            .map(WebSocketMessage::getPayloadAsText)
            .doOnNext(messageSink::tryEmitNext)
            .doOnError(t -> {
                var result = messageSink.tryEmitError(t);
                log.info("Error: {}", result);
            })
            .doFinally((s) -> {
                log.info("Connection closed in doFinally");
                messageSink.tryEmitError(new RuntimeException("Connection closed"));
            })
            .then();
    }

    public String getConnectMessage(String[] streams) {
        var channels = Arrays.stream(streams)
            .map(s -> "\"" + s + "\"")
            .reduce((s1, s2) -> s1 + "," + s2)
            .orElseThrow();
        return String.format("{\"method\":\"SUBSCRIBE\",\"params\":[%s],\"id\":1}", channels);
    }

    public static String[] klineStreams(String[] symbols, KlineInterval interval) {
        return Arrays.stream(symbols)
            .map(s -> s.toLowerCase() + "@kline_" + interval.getValue())
            .toArray(String[]::new);
    }
}
