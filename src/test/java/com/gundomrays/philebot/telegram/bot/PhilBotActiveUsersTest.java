package com.gundomrays.philebot.telegram.bot;

import com.gundomrays.philebot.psn.domain.PsnProfile;
import com.gundomrays.philebot.xbox.domain.Profile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.telegram.telegrambots.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PhilBotActiveUsersTest {

    private static final Long CHAT_ID = -100L;

    @TempDir
    private Path tempDir;

    private TelegramClient mockTelegramClient;

    private UserActivityService mockUserActivityService;

    private PhilBot philBot;

    @BeforeEach
    public void setUp() throws Exception {
        mockTelegramClient = Mockito.mock(TelegramClient.class);
        mockUserActivityService = Mockito.mock(UserActivityService.class);

        // Bot username is used as the MapDB file name, so keep the file in the temp directory
        philBot = new PhilBot(mockTelegramClient, tempDir.resolve("philbot-test").toString());
        setField("chatId", CHAT_ID);
        setField("userActivityService", mockUserActivityService);

        Mockito.when(mockUserActivityService.activationMessage(Mockito.any(PsnProfile.class))).thenReturn("psn back");
        Mockito.when(mockUserActivityService.deactivationMessage(Mockito.any(PsnProfile.class))).thenReturn("psn left");
    }

    @AfterEach
    public void tearDown() throws Exception {
        Field field = BaseAbilityBot.class.getDeclaredField("db");
        field.setAccessible(true);
        ((DBContext) field.get(philBot)).close();
    }

    @Test
    public void testPsnUserWithoutXboxProfileIsDeactivatedWithMessage() throws Exception {
        PsnProfile psnUser = psnProfile(2L, true);
        Mockito.when(mockUserActivityService.registeredUsers()).thenReturn(List.of());
        Mockito.when(mockUserActivityService.registeredPsnUsers()).thenReturn(List.of(psnUser));
        chatMembers(Set.of());

        philBot.updateActiveUsers();

        Mockito.verify(mockUserActivityService).deactivatePsnUser(psnUser);
        assertEquals(List.of("psn left"), sentMessages());
    }

    @Test
    public void testPsnUserWithoutXboxProfileIsActivatedWithMessage() throws Exception {
        PsnProfile psnUser = psnProfile(2L, false);
        Mockito.when(mockUserActivityService.registeredUsers()).thenReturn(List.of());
        Mockito.when(mockUserActivityService.registeredPsnUsers()).thenReturn(List.of(psnUser));
        chatMembers(Set.of(2L));

        philBot.updateActiveUsers();

        Mockito.verify(mockUserActivityService).activatePsnUser(psnUser);
        assertEquals(List.of("psn back"), sentMessages());
    }

    @Test
    public void testUserWithBothProfilesGetsOneMessage() throws Exception {
        Profile xboxUser = new Profile();
        xboxUser.setId("xuid");
        xboxUser.setTgUsername("tg_player");
        xboxUser.setTgId(1L);
        xboxUser.setActive(false);
        PsnProfile psnUser = psnProfile(1L, false);
        Mockito.when(mockUserActivityService.registeredUsers()).thenReturn(List.of(xboxUser));
        Mockito.when(mockUserActivityService.registeredPsnUsers()).thenReturn(List.of(psnUser));
        Mockito.when(mockUserActivityService.activationMessage(xboxUser)).thenReturn("xbox back");
        chatMembers(Set.of(1L));

        philBot.updateActiveUsers();

        Mockito.verify(mockUserActivityService).activateUser(xboxUser);
        Mockito.verify(mockUserActivityService).activatePsnUser(psnUser);
        assertEquals(List.of("xbox back"), sentMessages());
    }

    @Test
    public void testUnchangedPsnUserIsLeftAlone() throws Exception {
        PsnProfile psnUser = psnProfile(2L, true);
        Mockito.when(mockUserActivityService.registeredUsers()).thenReturn(List.of());
        Mockito.when(mockUserActivityService.registeredPsnUsers()).thenReturn(List.of(psnUser));
        chatMembers(Set.of(2L));

        philBot.updateActiveUsers();

        Mockito.verify(mockUserActivityService, Mockito.never()).deactivatePsnUser(Mockito.any());
        Mockito.verify(mockUserActivityService, Mockito.never()).activatePsnUser(Mockito.any());
        assertEquals(List.of(), sentMessages());
    }

    private void chatMembers(final Set<Long> present) throws Exception {
        Mockito.when(mockTelegramClient.execute(Mockito.any(GetChatMember.class))).thenAnswer(invocation -> {
            GetChatMember request = invocation.getArgument(0);
            ChatMember member = Mockito.mock(ChatMember.class);
            Mockito.when(member.getStatus()).thenReturn(present.contains(request.getUserId()) ? "member" : "left");
            return member;
        });
    }

    private List<String> sentMessages() {
        return Mockito.mockingDetails(mockTelegramClient).getInvocations().stream()
                .flatMap(invocation -> Arrays.stream(invocation.getArguments()))
                .filter(SendMessage.class::isInstance)
                .map(argument -> ((SendMessage) argument).getText())
                .toList();
    }

    private PsnProfile psnProfile(final Long tgId, final boolean active) {
        PsnProfile profile = new PsnProfile();
        profile.setAccountId("account-" + tgId);
        profile.setTgId(tgId);
        profile.setTgUsername("tg_" + tgId);
        profile.setActive(active);
        return profile;
    }

    private void setField(final String name, final Object value) throws Exception {
        Field field = PhilBot.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(philBot, value);
    }
}
