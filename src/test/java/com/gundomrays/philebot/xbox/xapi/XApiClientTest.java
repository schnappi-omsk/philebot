package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.xbox.domain.Activity;
import com.gundomrays.philebot.xbox.domain.Profile;
import com.gundomrays.philebot.xbox.domain.TitleHistory;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Objects;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XApiClientTest {

    /**
     * This test class is written for the XApiClient class.
     * Specifically, it tests the functionality of the method 'userByGamertag'.
     */

    @Test
    public void testUserByGamertag() throws Exception {
        // Create a mock WebClient
        XApiClient xApiClient = xapiClientMock(Profile.class);

        // Call the method to be tested with some input
        Profile profile = xApiClient.userByGamertag("Gamertag");

        // Using StepVerifier, verify that the profile returned is not null
        StepVerifier.create(Mono.just(profile))
                .expectSubscription()
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();
    }

    @Test
    void titleHistory() throws Exception {
        // Create a mock WebClient
        XApiClient xApiClient = xapiClientMock(TitleHistory.class);

        TitleHistory titleHistory = xApiClient.titleHistory("12456");

        StepVerifier.create(Mono.just(titleHistory))
                .expectSubscription()
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();
    }

    @Test
    void userActivity() throws Exception {
        XApiClient xApiClient = xapiClientMock(Activity.class);

        Activity activity = xApiClient.userActivity("123456");

        StepVerifier.create(Mono.just(activity))
                .expectSubscription()
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T>  XApiClient xapiClientMock(Class<T> clazz) throws Exception {
        WebClient webClientMock = mock(WebClient.class);

        // Create a mock WebClient.RequestHeadersUriSpec
        WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);

        // Create a mock WebClient.ResponseSpec
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);
        when(responseSpecMock.onStatus(any(), any())).thenReturn(responseSpecMock);

        // When webClient.get() method is called, return our mocked RequestHeadersUriSpec
        when(webClientMock.get()).thenReturn(requestHeadersUriSpecMock);

        // When uri method is called on RequestHeadersUriSpec object, return our mocked RequestHeadersUriSpec
        Function<UriBuilder, URI> any = any();
        when(requestHeadersUriSpecMock.uri(any)).thenReturn(requestHeadersUriSpecMock);

        // When retrieve method is called on RequestHeadersUriSpec object, return our mocked ResponseSpec
        when(requestHeadersUriSpecMock.retrieve()).thenReturn(responseSpecMock);

        // Mock the Mono<Profile> that should be returned from the API call
        Mono<T> classMono = Mono.just(clazz.getConstructor().newInstance());

        // When bodyToMono method is called on ResponseSpec object, return our mocked Mono<Profile>
        when(responseSpecMock.bodyToMono(clazz)).thenReturn(classMono);

        // Instantiate a XApiClient using the mocked WebClient
        return new XApiClient(webClientMock);
    }
}