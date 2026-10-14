package com.library.exception;

public final class NotFoundException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) { super(message); }
}
