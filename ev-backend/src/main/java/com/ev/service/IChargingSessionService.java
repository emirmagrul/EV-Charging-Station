package com.ev.service;

import com.ev.dto.ChargingSessionDto;

public interface IChargingSessionService {
    ChargingSessionDto getActiveSession(Long driverId);
    ChargingSessionDto startSession(Long reservationId);
    ChargingSessionDto endSession(Long sessionId);
}
