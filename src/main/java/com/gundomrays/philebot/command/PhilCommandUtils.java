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

}
