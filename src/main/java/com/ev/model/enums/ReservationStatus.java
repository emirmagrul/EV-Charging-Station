package com.ev.model.enums;

public enum ReservationStatus {
    PENDING, //Beklemede
    CONFIRMED, //Onaylandı
    COMPLETED, //Tamamlandı
    CANCELLED, //Kullanıcı tarafından iptal
    CANCELLED_BY_OPERATOR //Cihaz arızası nedeniyle iptal
}
