package com.gundomrays.philebot.psn.api.exception;

public class PsnUnauthorizedException extends PsnApiException {

    public PsnUnauthorizedException(String message) {
        super(401, message);
    }

}
