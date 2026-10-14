package com.library.factory;

import com.library.domain.Book;

import java.util.Set;

/** Factory keeps construction and validation rules out of the application service. */
public final class BookFactory {
    private BookFactory() { }

    public static Book create(String title, String author, String isbn, int publicationYear, Set<String> categories) {
        return new Book(title, author, isbn, publicationYear, categories);
    }
}
