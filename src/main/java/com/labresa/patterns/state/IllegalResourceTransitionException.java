package com.labresa.patterns.state;

public class IllegalResourceTransitionException extends RuntimeException {
    public IllegalResourceTransitionException(String message) {
        super(message);
    }
}