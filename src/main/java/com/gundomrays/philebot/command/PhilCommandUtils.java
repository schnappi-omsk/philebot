package com.gundomrays.philebot.command;

public class PhilCommandUtils {

    public static CommandResponse textResponse(final String message) {
        final CommandResponse response = new CommandResponse();
        response.setMessage(message);
        return response;
    }

    public static CommandResponse captionedPhotoResponse(final String caption, final String photoUrl) {
        final CommandResponse response = new CommandResponse();
        response.setMessage(caption);
        response.setMediaUrl(photoUrl);
        return response;
    }

    public static String additionalSpaces(final String value, final int longest) {
        final int spacesCount = longest - value.length();
        return " ".repeat(spacesCount);
    }

    public static String commandName(final String input) {
        int pingIdx = input.indexOf('@');
        return pingIdx > -1 ? input.substring(0, pingIdx) : input;
    }

}
