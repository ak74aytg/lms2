package com.library.recommendation;

import com.library.domain.Book;
import com.library.domain.Patron;

import java.util.Collection;
import java.util.List;

public interface RecommendationStrategy {
    List<Book> recommend(Patron patron, Collection<Book> catalog, int limit);
}
