package com.windsome.exception;

import org.springframework.security.core.AuthenticationException;

public class AccountDeactivatedException extends AuthenticationException {
    public AccountDeactivatedException(String message) {
        super(message);
    }
}
