package com.library.domain;

import java.time.LocalDateTime;

public record Reservation(String isbn, String patronId, LocalDateTime reservedAt) {
    public Reservation {
        if (isbn == null || isbn.isBlank() || patronId == null || patronId.isBlank() || reservedAt == null) {
            throw new IllegalArgumentException("ISBN, patron ID and reservation time are required");
        }
    }
}
