package com.library.domain;

import java.time.LocalDateTime;

public record BorrowingRecord(String isbn, LocalDateTime checkedOutAt, LocalDateTime returnedAt) {
    public BorrowingRecord {
        if (isbn == null || isbn.isBlank() || checkedOutAt == null) {
            throw new IllegalArgumentException("ISBN and checkout time are required");
        }
    }

    public boolean isReturned() { return returnedAt != null; }
}
