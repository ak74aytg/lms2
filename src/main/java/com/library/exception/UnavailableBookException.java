package com.library.exception;

public final class UnavailableBookException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public UnavailableBookException(String message) { super(message); }
}
