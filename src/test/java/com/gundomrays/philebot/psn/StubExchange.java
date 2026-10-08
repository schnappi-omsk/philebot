package com.gundomrays.philebot.psn;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

// Answers WebClient requests with queued responses and records the requests, so no network is used
public class StubExchange implements ExchangeFunction {

    private final Deque<ClientResponse> responses = new ArrayDeque<>();

    private final List<ClientRequest> requests = new ArrayList<>();

    public WebClient.Builder builder() {
        return WebClient.builder().exchangeFunction(this);
    }

    public StubExchange json(final String body) {
        responses.add(ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .build());
        return this;
    }

    public StubExchange fixture(final String name) {
        return json(resource(name));
    }

    public StubExchange status(final HttpStatus status, final String body) {
        responses.add(ClientResponse.create(status)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .build());
        return this;
    }

    public StubExchange redirect(final String location) {
        responses.add(ClientResponse.create(HttpStatus.FOUND).header(HttpHeaders.LOCATION, location).build());
        return this;
    }

    public List<ClientRequest> requests() {
        return requests;
    }

    @Override
    public Mono<ClientResponse> exchange(ClientRequest request) {
        requests.add(request);
        if (responses.isEmpty()) {
            return Mono.error(new IllegalStateException("Unexpected request " + request.url()));
        }
        return Mono.just(responses.poll());
    }

    public static String body(final ClientRequest request) {
        final MockClientHttpRequest mock = new MockClientHttpRequest(request.method(), request.url());
        request.body().insert(mock, new BodyInserter.Context() {
            @Override
            public List<HttpMessageWriter<?>> messageWriters() {
                return ExchangeStrategies.withDefaults().messageWriters();
            }

            @Override
            public Optional<org.springframework.http.server.reactive.ServerHttpRequest> serverRequest() {
                return Optional.empty();
            }

            @Override
            public Map<String, Object> hints() {
                return Map.of();
            }
        }).block();
        return mock.getBodyAsString().block();
    }

    public static String resource(final String name) {
        try (InputStream stream = StubExchange.class.getResourceAsStream("/psn/" + name)) {
            return new String(Objects.requireNonNull(stream, name).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
