package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.xbox.domain.Activity;
import com.gundomrays.philebot.xbox.domain.Profile;
import com.gundomrays.philebot.xbox.domain.TitleHistory;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class XApiClientTest {
    WebClient webClient = Mockito.mock(WebClient.class);
    WebClient.RequestHeadersUriSpec uriSpecMock = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.RequestHeadersSpec headersSpecMock = Mockito.mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpecMock = Mockito.mock(WebClient.ResponseSpec.class);
    XApiClient client = new XApiClient(webClient);

    @Test
    void userByGamertagExisting() {
        String testGamertag = "testGamertag";
        Profile expectedProfile = new Profile();
        expectedProfile.setGamertag(testGamertag);
        Mockito.when(webClient.get()).thenReturn(uriSpecMock);
        Mockito.when(uriSpecMock.uri(Mockito.any(Function.class))).thenReturn(headersSpecMock);
        Mockito.when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);
        Mockito.when(responseSpecMock.bodyToMono(Profile.class)).thenReturn(Mono.just(expectedProfile));
        Profile profile = client.userByGamertag(testGamertag);
        assertNotNull(profile);
        assertEquals(testGamertag, profile.getGamertag());
    }

    @Test
    void userByGamertagNonExisting() {
        String testGamertag = "testGamertag";
        Mockito.when(webClient.get()).thenReturn(uriSpecMock);
        Mockito.when(uriSpecMock.uri(Mockito.any(Function.class))).thenReturn(headersSpecMock);
        Mockito.when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);
        Mockito.when(responseSpecMock.bodyToMono(Profile.class)).thenReturn(Mono.empty());
        assertThrows(RuntimeException.class, () -> client.userByGamertag(testGamertag));
    }

    @Test
    void titleHistoryExisting() {
        String testXuid = "testXuid";
        TitleHistory expectedTitleHistory = new TitleHistory();
        Mockito.when(webClient.get()).thenReturn(uriSpecMock);
        Mockito.when(uriSpecMock.uri(Mockito.any(Function.class))).thenReturn(headersSpecMock);
        Mockito.when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);
        Mockito.when(responseSpecMock.bodyToMono(TitleHistory.class)).thenReturn(Mono.just(expectedTitleHistory));
        TitleHistory titleHistory = client.titleHistory(testXuid);
        assertNotNull(titleHistory);
    }

    @Test
    void titleHistoryNonExisting() {
        String testXuid = "testXuid";
        Mockito.when(webClient.get()).thenReturn(uriSpecMock);
        Mockito.when(uriSpecMock.uri(Mockito.any(Function.class))).thenReturn(headersSpecMock);
        Mockito.when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);
        Mockito.when(responseSpecMock.bodyToMono(TitleHistory.class)).thenReturn(Mono.empty());
        assertThrows(RuntimeException.class, () -> client.titleHistory(testXuid));
    }

    @Test
    void userActivityExisting() {
        String testXuid = "testXuid";
        Activity expectedActivity = new Activity();
        Mockito.when(webClient.get()).thenReturn(uriSpecMock);
        Mockito.when(uriSpecMock.uri(Mockito.any(Function.class))).thenReturn(headersSpecMock);
        Mockito.when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);
        Mockito.when(responseSpecMock.bodyToMono(Activity.class)).thenReturn(Mono.just(expectedActivity));
        Activity activity = client.userActivity(testXuid);
        assertNotNull(activity);
    }

    @Test
    void userActivityNonExisting() {
        String testXuid = "testXuid";
        Mockito.when(webClient.get()).thenReturn(uriSpecMock);
        Mockito.when(uriSpecMock.uri(Mockito.any(Function.class))).thenReturn(headersSpecMock);
        Mockito.when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);
        Mockito.when(responseSpecMock.bodyToMono(Activity.class)).thenReturn(Mono.empty());
        assertThrows(RuntimeException.class, () -> client.userActivity(testXuid));
    }
}