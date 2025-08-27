package com.ahd.backend.carcontracts.notification.event;

import com.ahd.backend.carcontracts.notification.model.AppNotification;

public record NotificationSaveEvent(AppNotification notification) { }