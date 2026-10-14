package com.library.repository;

import com.library.domain.Book;
import com.library.domain.Patron;

import java.util.Collection;
import java.util.Optional;

public interface LibraryRepository {
    void saveBook(Book book);
    Optional<Book> findBookByIsbn(String isbn);
    void deleteBook(String isbn);
    Collection<Book> findAllBooks();

    void savePatron(Patron patron);
    Optional<Patron> findPatronById(String patronId);
    Collection<Patron> findAllPatrons();
}
