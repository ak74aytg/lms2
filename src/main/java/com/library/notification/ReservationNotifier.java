package com.library.notification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Subject in the Observer pattern. */
public final class ReservationNotifier {
    private final List<NotificationObserver> observers = new ArrayList<>();

    public void subscribe(NotificationObserver observer) { observers.add(observer); }
    public void unsubscribe(NotificationObserver observer) { observers.remove(observer); }

    public void notifyBookAvailable(String patronId, String bookTitle) {
        Notification notification = new Notification(
                patronId,
                "Reserved book is now available: " + bookTitle,
                LocalDateTime.now());
        observers.forEach(observer -> observer.update(notification));
    }
}
