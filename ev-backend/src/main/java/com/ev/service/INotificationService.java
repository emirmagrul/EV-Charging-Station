package com.ev.service;

import com.ev.dto.NotificationDto;
import com.ev.model.enums.NotificationType;

import java.util.List;

public interface INotificationService {
    void sendNotification(Long driverId, String title, String message, NotificationType type);
    void sendOperatorNotification(Long operatorId, String title, String message, NotificationType type);
    List<NotificationDto> getNotificationsByDriver(Long driverId);
    List<NotificationDto> getNotificationsByOperator(Long operatorId);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long driverId);
    void markAllOperatorAsRead(Long operatorId);
    long getUnreadCount(Long driverId);
    long getOperatorUnreadCount(Long operatorId);
}