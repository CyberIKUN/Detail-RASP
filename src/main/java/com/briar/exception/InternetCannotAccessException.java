package com.briar.exception;

import java.io.IOException;

public class InternetCannotAccessException extends IOException {

    public InternetCannotAccessException(String message) {
        super(message);
    }
}
