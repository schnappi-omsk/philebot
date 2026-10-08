package com.gundomrays.philebot.psn.api.exception;

import lombok.Getter;

@Getter
public class PsnApiException extends RuntimeException {

    private final int status;

    public PsnApiException(int status, String message) {
        super(message);
        this.status = status;
    }

}
