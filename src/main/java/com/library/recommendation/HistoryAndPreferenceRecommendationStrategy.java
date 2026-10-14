package com.library.recommendation;

import com.library.domain.Book;
import com.library.domain.BorrowingRecord;
import com.library.domain.Patron;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Strategy that scores category matches and authors previously borrowed by the patron. */
public final class HistoryAndPreferenceRecommendationStrategy implements RecommendationStrategy {
    @Override
    public List<Book> recommend(Patron patron, Collection<Book> catalog, int limit) {
        if (limit <= 0) return List.of();

        Set<String> borrowedIsbns = patron.getBorrowingHistory().stream()
                .map(BorrowingRecord::isbn).collect(Collectors.toSet());
        Set<String> borrowedAuthors = catalog.stream()
                .filter(book -> borrowedIsbns.contains(book.getIsbn()))
                .map(Book::getAuthor).collect(Collectors.toSet());

        return catalog.stream()
                .filter(book -> !borrowedIsbns.contains(book.getIsbn()))
                .sorted(Comparator.comparingInt((Book book) -> score(book, patron, borrowedAuthors))
                        .reversed().thenComparing(Book::getTitle))
                .limit(limit)
                .toList();
    }

    private int score(Book book, Patron patron, Set<String> borrowedAuthors) {
        Set<String> categories = new HashSet<>(book.getCategories());
        categories.retainAll(patron.getPreferredCategories());
        return categories.size() * 3 + (borrowedAuthors.contains(book.getAuthor()) ? 2 : 0);
    }
}
