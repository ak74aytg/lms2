package com.library.service;

import com.library.domain.Book;
import com.library.domain.BorrowingRecord;
import com.library.domain.Patron;
import com.library.domain.Reservation;
import com.library.exception.LibraryException;
import com.library.exception.NotFoundException;
import com.library.exception.UnavailableBookException;
import com.library.notification.ReservationNotifier;
import com.library.recommendation.RecommendationStrategy;
import com.library.repository.LibraryRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/** Application facade for catalog, patron, lending, reservation and recommendation use cases. */
public final class LibraryService {
    private static final Logger LOGGER = Logger.getLogger(LibraryService.class.getName());

    private final LibraryRepository repository;
    private final RecommendationStrategy recommendationStrategy;
    private final ReservationNotifier reservationNotifier;
    private final Map<String, List<Reservation>> reservationsByIsbn = new HashMap<>();
    private final Set<String> borrowedIsbns = new HashSet<>();
    private final Map<String, String> borrowerByIsbn = new HashMap<>();

    public LibraryService(LibraryRepository repository,
                          RecommendationStrategy recommendationStrategy,
                          ReservationNotifier reservationNotifier) {
        this.repository = repository;
        this.recommendationStrategy = recommendationStrategy;
        this.reservationNotifier = reservationNotifier;
    }

    public void addBook(Book book) {
        if (repository.findBookByIsbn(book.getIsbn()).isPresent()) {
            throw new LibraryException("A book with ISBN " + book.getIsbn() + " already exists");
        }
        repository.saveBook(book);
        LOGGER.info("Added book " + book.getIsbn());
    }

    public void updateBook(String isbn, String title, String author, int publicationYear, Set<String> categories) {
        Book book = getBook(isbn);
        book.updateDetails(title, author, publicationYear, categories);
        LOGGER.info("Updated book " + isbn);
    }

    public void removeBook(String isbn) {
        getBook(isbn);
        if (borrowedIsbns.contains(isbn)) {
            throw new LibraryException("A checked-out book cannot be removed");
        }
        repository.deleteBook(isbn);
        reservationsByIsbn.remove(isbn);
        LOGGER.info("Removed book " + isbn);
    }

    public void addPatron(Patron patron) {
        if (repository.findPatronById(patron.getId()).isPresent()) {
            throw new LibraryException("A patron with ID " + patron.getId() + " already exists");
        }
        repository.savePatron(patron);
        LOGGER.info("Added patron " + patron.getId());
    }

    public void updatePatron(String patronId, String name, String email) {
        Patron patron = getPatron(patronId);
        patron.updateInformation(name, email);
        LOGGER.info("Updated patron " + patronId);
    }

    public List<Book> searchBooks(String query) {
        if (query == null || query.isBlank()) return List.of();
        String term = query.trim().toLowerCase(Locale.ROOT);
        return repository.findAllBooks().stream()
                .filter(book -> book.getTitle().toLowerCase(Locale.ROOT).contains(term)
                        || book.getAuthor().toLowerCase(Locale.ROOT).contains(term)
                        || book.getIsbn().toLowerCase(Locale.ROOT).contains(term))
                .sorted(Comparator.comparing(Book::getTitle))
                .toList();
    }

    public void checkoutBook(String isbn, String patronId) {
        Book book = getBook(isbn);
        Patron patron = getPatron(patronId);
        if (borrowedIsbns.contains(isbn)) {
            throw new UnavailableBookException("Book is already checked out: " + isbn);
        }
        List<Reservation> queue = reservationsByIsbn.getOrDefault(isbn, List.of());
        if (!queue.isEmpty() && !queue.get(0).patronId().equals(patronId)) {
            throw new UnavailableBookException("Book is reserved for another patron");
        }
        if (!queue.isEmpty()) {
            queue.remove(0);
        }
        borrowedIsbns.add(isbn);
        borrowerByIsbn.put(isbn, patronId);
        patron.addBorrowingRecord(new BorrowingRecord(isbn, LocalDateTime.now(), null));
        LOGGER.info("Patron " + patronId + " checked out " + book.getIsbn());
    }

    public void returnBook(String isbn, String patronId) {
        Book book = getBook(isbn);
        Patron patron = getPatron(patronId);
        if (!borrowedIsbns.contains(isbn)) {
            throw new LibraryException("Book is not currently checked out: " + isbn);
        }
        if (!patronId.equals(borrowerByIsbn.get(isbn))) {
            throw new LibraryException("Book is checked out by another patron");
        }
        borrowedIsbns.remove(isbn);
        borrowerByIsbn.remove(isbn);
        patron.markReturned(isbn);
        notifyNextReservation(book);
        LOGGER.info("Patron " + patronId + " returned " + isbn);
    }

    public Reservation reserveBook(String isbn, String patronId) {
        Book book = getBook(isbn);
        getPatron(patronId);
        List<Reservation> queue = reservationsByIsbn.computeIfAbsent(isbn, ignored -> new ArrayList<>());
        if (queue.stream().anyMatch(reservation -> reservation.patronId().equals(patronId))) {
            throw new LibraryException("Patron already has a reservation for this book");
        }
        if (!borrowedIsbns.contains(isbn)) {
            throw new UnavailableBookException("Book is available; check it out instead");
        }
        Reservation reservation = new Reservation(book.getIsbn(), patronId, LocalDateTime.now());
        queue.add(reservation);
        LOGGER.info("Patron " + patronId + " reserved " + isbn);
        return reservation;
    }

    public List<Book> getAvailableBooks() {
        return repository.findAllBooks().stream()
                .filter(book -> !borrowedIsbns.contains(book.getIsbn()))
                .toList();
    }

    public List<Book> getBorrowedBooks() {
        return repository.findAllBooks().stream()
                .filter(book -> borrowedIsbns.contains(book.getIsbn()))
                .toList();
    }

    public List<Book> recommendBooks(String patronId, int limit) {
        return recommendationStrategy.recommend(getPatron(patronId), repository.findAllBooks(), limit);
    }

    public Collection<Patron> getPatrons() { return repository.findAllPatrons(); }

    private void notifyNextReservation(Book book) {
        List<Reservation> queue = reservationsByIsbn.getOrDefault(book.getIsbn(), List.of());
        if (!queue.isEmpty()) {
            reservationNotifier.notifyBookAvailable(queue.get(0).patronId(), book.getTitle());
        }
    }

    private Book getBook(String isbn) {
        return repository.findBookByIsbn(isbn)
                .orElseThrow(() -> new NotFoundException("Book not found: " + isbn));
    }

    private Patron getPatron(String patronId) {
        return repository.findPatronById(patronId)
                .orElseThrow(() -> new NotFoundException("Patron not found: " + patronId));
    }
}
