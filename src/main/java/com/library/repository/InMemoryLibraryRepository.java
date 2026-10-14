package com.library.repository;

import com.library.domain.Book;
import com.library.domain.Patron;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryLibraryRepository implements LibraryRepository {
    private final Map<String, Book> books = new LinkedHashMap<>();
    private final Map<String, Patron> patrons = new LinkedHashMap<>();

    @Override
    public void saveBook(Book book) { books.put(book.getIsbn(), book); }

    @Override
    public Optional<Book> findBookByIsbn(String isbn) { return Optional.ofNullable(books.get(isbn)); }

    @Override
    public void deleteBook(String isbn) { books.remove(isbn); }

    @Override
    public Collection<Book> findAllBooks() { return books.values(); }

    @Override
    public void savePatron(Patron patron) { patrons.put(patron.getId(), patron); }

    @Override
    public Optional<Patron> findPatronById(String patronId) { return Optional.ofNullable(patrons.get(patronId)); }

    @Override
    public Collection<Patron> findAllPatrons() { return patrons.values(); }
}
