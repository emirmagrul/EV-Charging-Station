package com.ev.service;

import com.ev.dto.ChargingSessionDto;

public interface IChargingSessionService {
    ChargingSessionDto startSession(Long reservationId);
    ChargingSessionDto endSession(Long sessionId, double energyConsumedKWh);
}
