package com.library.notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Observer attached to a patron; a real adapter could deliver these through email or SMS. */
public final class PatronNotificationObserver implements NotificationObserver {
    private final String patronId;
    private final List<Notification> notifications = new ArrayList<>();

    public PatronNotificationObserver(String patronId) { this.patronId = patronId; }

    @Override
    public void update(Notification notification) {
        if (patronId.equals(notification.patronId())) {
            notifications.add(notification);
        }
    }

    public List<Notification> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }
}
