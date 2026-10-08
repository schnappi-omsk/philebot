package com.gundomrays.philebot.psn.api.exception;

// The account does not exist or its trophies are hidden from the bot account by privacy settings
public class PsnAccessDeniedException extends PsnApiException {

    public PsnAccessDeniedException(int status, String message) {
        super(status, message);
    }

}
