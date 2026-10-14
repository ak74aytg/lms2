package com.library.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class Patron {
    private final String id;
    private String name;
    private String email;
    private final Set<String> preferredCategories = new HashSet<>();
    private final List<BorrowingRecord> borrowingHistory = new ArrayList<>();

    public Patron(String id, String name, String email) {
        this.id = requireText(id, "Patron ID");
        updateInformation(name, email);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Set<String> getPreferredCategories() { return Collections.unmodifiableSet(preferredCategories); }
    public List<BorrowingRecord> getBorrowingHistory() {
        return Collections.unmodifiableList(borrowingHistory);
    }

    public void updateInformation(String name, String email) {
        this.name = requireText(name, "Name");
        this.email = requireText(email, "Email");
    }

    public void setPreferredCategories(Set<String> categories) {
        preferredCategories.clear();
        if (categories != null) {
            categories.stream().filter(category -> category != null && !category.isBlank())
                    .map(String::trim).map(value -> value.toLowerCase(Locale.ROOT)).forEach(preferredCategories::add);
        }
    }

    public void addBorrowingRecord(BorrowingRecord record) {
        borrowingHistory.add(record);
    }

    public void markReturned(String isbn) {
        for (int index = borrowingHistory.size() - 1; index >= 0; index--) {
            BorrowingRecord record = borrowingHistory.get(index);
            if (record.isbn().equals(isbn) && !record.isReturned()) {
                borrowingHistory.set(index,
                        new BorrowingRecord(record.isbn(), record.checkedOutAt(), java.time.LocalDateTime.now()));
                return;
            }
        }
        throw new IllegalStateException("No active borrowing found for ISBN " + isbn);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
