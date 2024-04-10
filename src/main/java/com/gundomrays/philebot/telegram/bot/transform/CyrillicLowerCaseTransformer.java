package com.gundomrays.philebot.telegram.bot.transform;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CyrillicLowerCaseTransformer implements MessageTransformer {

    private static final Map<Character, Character> LETTERS = Map.of(
            'c', 'с',
            'e', 'е',
            'o', 'о',
            'p', 'р',
            'x', 'х',
            'a', 'а',
            'y', 'у'
    );

    @Override
    public String transform(String input) {
        final StringBuilder transformed = new StringBuilder();
        input.toLowerCase().chars().forEach(c -> {
            final Character cyrillic = LETTERS.get((char) c);
            if (cyrillic != null) {
                transformed.append(cyrillic);
            } else {
                transformed.append((char) c);
            }
        });
        return transformed.toString();
    }

}
