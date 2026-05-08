package com.ev.controller;

import com.ev.dto.NotificationDto;
import com.ev.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService notificationService;

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<NotificationDto>> getMyNotifications(@PathVariable Long driverId) {
        return ResponseEntity.ok(notificationService.getNotificationsByDriver(driverId));
    }

    @GetMapping("/operator/{operatorId}")
    public ResponseEntity<List<NotificationDto>> getOperatorNotifications(@PathVariable Long operatorId) {
        return ResponseEntity.ok(notificationService.getNotificationsByOperator(operatorId));
    }

    @GetMapping("/driver/{driverId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long driverId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(driverId));
    }

    @GetMapping("/operator/{operatorId}/unread-count")
    public ResponseEntity<Long> getOperatorUnreadCount(@PathVariable Long operatorId) {
        return ResponseEntity.ok(notificationService.getOperatorUnreadCount(operatorId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/driver/{driverId}/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long driverId) {
        notificationService.markAllAsRead(driverId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/operator/{operatorId}/mark-all-read")
    public ResponseEntity<Void> markAllOperatorAsRead(@PathVariable Long operatorId) {
        notificationService.markAllOperatorAsRead(operatorId);
        return ResponseEntity.ok().build();
    }
}