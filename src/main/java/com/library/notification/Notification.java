package com.library.notification;

import java.time.LocalDateTime;

public record Notification(String patronId, String message, LocalDateTime createdAt) { }
