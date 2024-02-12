package com.gundomrays.philebot.command;

import com.gundomrays.philebot.xbox.xapi.XboxTitleService;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class PhilFindGameCommandTest {

    @Test
    public void testExecuteWhenNoGamesFound() {
        // Given
        XboxTitleService mockService = mock(XboxTitleService.class);
        PhilCommandService commandService = mock(PhilCommandService.class);
        PhilFindGameCommand testCommand = new PhilFindGameCommand(mockService, commandService);

        CommandRequest request = new CommandRequest();
        request.setArgument("GameNotFound");

        // Indicating that no game will be found
        when(mockService.searchGames(request.getArgument())).thenReturn(Collections.emptyMap());

        // When
        CommandResponse response = testCommand.execute(request);

        // Then
        assertEquals(response.getMessage(), "Igor tonet...");
    }

    @Test
    public void testExecuteWhenSingleGameFound() {
        // Given
        XboxTitleService mockService = mock(XboxTitleService.class);
        PhilCommandService commandService = mock(PhilCommandService.class);
        PhilFindGameCommand testCommand = new PhilFindGameCommand(mockService, commandService);

        CommandRequest request = new CommandRequest();
        request.setArgument("SingleGame");

        Map<String, String> singleGame = new HashMap<>();
        singleGame.put("GameName", "GameCommand1");

        when(mockService.searchGames(request.getArgument())).thenReturn(singleGame);

        // When
        when(commandService.command(anyString())).thenReturn(testCommand);
        CommandResponse response = testCommand.execute(request);

        // Then
        assertEquals("GameCommand1", singleGame.values().iterator().next());
    }

    @Test
    public void testExecuteWhenMultipleGamesFound() {
        // Given
        XboxTitleService mockService = mock(XboxTitleService.class);
        PhilCommandService commandService = mock(PhilCommandService.class);
        PhilFindGameCommand testCommand = new PhilFindGameCommand(mockService, commandService);

        CommandRequest request = new CommandRequest();
        request.setArgument("MultipleGames");

        Map<String, String> multipleGames = new HashMap<>();
        multipleGames.put("GameName1", "GameCommand1");
        multipleGames.put("GameName2", "GameCommand2");

        when(mockService.searchGames(request.getArgument())).thenReturn(multipleGames);

        // When
        CommandResponse response = testCommand.execute(request);

        // Then
        StringBuilder builder = new StringBuilder();
        multipleGames.forEach((name, cmd) -> builder.append("<code>")
                .append(name)
                .append(": ")
                .append("</code>")
                .append(cmd)
                .append("\n"));

        assertEquals(response.getMessage(), builder.toString());
    }
}