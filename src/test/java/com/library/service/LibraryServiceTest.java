package com.library.service;

import com.library.domain.Book;
import com.library.domain.Patron;
import com.library.exception.UnavailableBookException;
import com.library.notification.PatronNotificationObserver;
import com.library.notification.ReservationNotifier;
import com.library.recommendation.HistoryAndPreferenceRecommendationStrategy;
import com.library.repository.InMemoryLibraryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryServiceTest {
    private LibraryService library;
    private Patron alice;
    private Patron bob;
    private PatronNotificationObserver bobNotifications;

    @BeforeEach
    void setUp() {
        ReservationNotifier notifier = new ReservationNotifier();
        bobNotifications = new PatronNotificationObserver("bob");
        notifier.subscribe(bobNotifications);
        library = new LibraryService(new InMemoryLibraryRepository(),
                new HistoryAndPreferenceRecommendationStrategy(), notifier);
        alice = new Patron("alice", "Alice", "alice@example.com");
        bob = new Patron("bob", "Bob", "bob@example.com");
        library.addPatron(alice);
        library.addPatron(bob);
        library.addBook(new Book("Clean Code", "Robert Martin", "9780132350884", 2008, Set.of("software", "programming")));
        library.addBook(new Book("Clean Architecture", "Robert Martin", "9780134494166", 2017, Set.of("software")));
        library.addBook(new Book("Dune", "Frank Herbert", "9780441013593", 1965, Set.of("science fiction")));
    }

    @Test
    void searchesAndUpdatesBooks() {
        assertEquals(2, library.searchBooks("martin").size());
        library.updateBook("9780441013593", "Dune Messiah", "Frank Herbert", 1969, Set.of("science fiction"));
        assertEquals("Dune Messiah", library.searchBooks("Messiah").get(0).getTitle());
    }

    @Test
    void checksOutAndReturnsBookWithHistory() {
        library.checkoutBook("9780132350884", "alice");
        assertEquals(1, library.getBorrowedBooks().size());
        assertEquals(1, alice.getBorrowingHistory().size());
        library.returnBook("9780132350884", "alice");
        assertEquals(0, library.getBorrowedBooks().size());
        assertTrue(alice.getBorrowingHistory().get(0).isReturned());
    }

    @Test
    void notifiesNextReservationWhenBookReturns() {
        library.checkoutBook("9780132350884", "alice");
        library.reserveBook("9780132350884", "bob");
        library.returnBook("9780132350884", "alice");
        assertEquals(1, bobNotifications.getNotifications().size());
        assertTrue(bobNotifications.getNotifications().get(0).message().contains("Clean Code"));
    }

    @Test
    void reservationQueueControlsCheckout() {
        library.checkoutBook("9780132350884", "alice");
        library.reserveBook("9780132350884", "bob");
        library.returnBook("9780132350884", "alice");
        assertThrows(UnavailableBookException.class,
                () -> library.checkoutBook("9780132350884", "alice"));
        library.checkoutBook("9780132350884", "bob");
    }

    @Test
    void recommendationsUsePatronHistoryAndPreferences() {
        alice.setPreferredCategories(Set.of("software"));
        library.checkoutBook("9780132350884", "alice");
        assertEquals("Clean Architecture", library.recommendBooks("alice", 1).get(0).getTitle());
    }
}
