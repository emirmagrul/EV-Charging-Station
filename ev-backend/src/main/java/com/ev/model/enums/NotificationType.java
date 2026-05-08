package com.ev.model.enums;

public enum NotificationType {
    RESERVATION_CANCELLED, // Operatör veya sistem tarafından iptal
    RESERVATION_CONFIRMED, // Ödeme başarılı
    SESSION_STARTED,       // Şarj başladı
    SESSION_FINISHED,      // Şarj bitti ve iade yapıldı
    SYSTEM_ALERT           // Genel duyurular
}