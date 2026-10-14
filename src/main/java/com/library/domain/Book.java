package com.library.domain;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** A catalog book. ISBN is the stable identity of a book in this in-memory system. */
public final class Book {
    private final String isbn;
    private String title;
    private String author;
    private int publicationYear;
    private final Set<String> categories;

    public Book(String title, String author, String isbn, int publicationYear, Set<String> categories) {
        this.isbn = requireText(isbn, "ISBN");
        this.title = requireText(title, "Title");
        this.author = requireText(author, "Author");
        if (publicationYear < 0) {
            throw new IllegalArgumentException("Publication year cannot be negative");
        }
        this.publicationYear = publicationYear;
        this.categories = normaliseCategories(categories);
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getPublicationYear() { return publicationYear; }
    public Set<String> getCategories() { return Collections.unmodifiableSet(categories); }

    public void updateDetails(String title, String author, int publicationYear, Set<String> categories) {
        this.title = requireText(title, "Title");
        this.author = requireText(author, "Author");
        if (publicationYear < 0) {
            throw new IllegalArgumentException("Publication year cannot be negative");
        }
        this.publicationYear = publicationYear;
        this.categories.clear();
        this.categories.addAll(normaliseCategories(categories));
    }

    private static Set<String> normaliseCategories(Set<String> categories) {
        Set<String> result = new LinkedHashSet<>();
        if (categories != null) {
            categories.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .map(value -> value.toLowerCase(Locale.ROOT))
                    .forEach(result::add);
        }
        return result;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Book book && isbn.equals(book.isbn);
    }

    @Override
    public int hashCode() { return isbn.hashCode(); }

    @Override
    public String toString() {
        return "%s by %s (%s)".formatted(title, author, isbn);
    }
}
