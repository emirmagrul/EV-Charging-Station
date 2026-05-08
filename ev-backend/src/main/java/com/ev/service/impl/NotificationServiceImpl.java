package com.ev.service.impl;

import com.ev.dto.NotificationDto;
import com.ev.model.EVDriver;
import com.ev.model.Notification;
import com.ev.model.StationOperator;
import com.ev.model.enums.NotificationType;
import com.ev.repository.EVDriverRepository;
import com.ev.repository.NotificationRepository;
import com.ev.repository.StationOperatorRepository;
import com.ev.service.INotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final EVDriverRepository evDriverRepository;
    private final StationOperatorRepository operatorRepository;

    @Override
    @Transactional
    public void sendNotification(Long driverId, String title, String message, NotificationType type) {
        EVDriver driver = evDriverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Sürücü bulunamadı!"));

        Notification notification = new Notification();
        notification.setDriver(driver);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);

        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void sendOperatorNotification(Long operatorId, String title, String message, NotificationType type) {
        StationOperator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("Operatör bulunamadı!"));

        Notification notification = new Notification();
        notification.setOperator(operator);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);

        notificationRepository.save(notification);
    }

    @Override
    public List<NotificationDto> getNotificationsByDriver(Long driverId) {
        return notificationRepository.findByDriverIdOrderByCreatedAtDesc(driverId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDto> getNotificationsByOperator(Long operatorId) {
        return notificationRepository.findByOperatorIdOrderByCreatedAtDesc(operatorId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Bildirim bulunamadı!"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long driverId) {
        List<Notification> unread = notificationRepository.findByDriverIdOrderByCreatedAtDesc(driverId)
                .stream().filter(n -> !n.isRead()).collect(Collectors.toList());

        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional
    public void markAllOperatorAsRead(Long operatorId) {
        List<Notification> unread = notificationRepository.findByOperatorIdOrderByCreatedAtDesc(operatorId)
                .stream().filter(n -> !n.isRead()).collect(Collectors.toList());

        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    public long getUnreadCount(Long driverId) {
        return notificationRepository.countByDriverIdAndReadFalse(driverId);
    }

    @Override
    public long getOperatorUnreadCount(Long operatorId) {
        return notificationRepository.countByOperatorIdAndReadFalse(operatorId);
    }

    private NotificationDto mapToDto(Notification n) {
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setType(n.getType());
        dto.setRead(n.isRead());
        dto.setCreatedAt(n.getCreatedAt());
        if (n.getDriver() != null) {
            dto.setDriverId(n.getDriver().getId());
        }
        if (n.getOperator() != null) {
            dto.setOperatorId(n.getOperator().getId());
        }
        return dto;
    }
}