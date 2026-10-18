package com.demo.transitfarecalculatorservice.trip;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public record Trip(
        LocalDateTime started,
        LocalDateTime finished,
        String fromStopId,
        String toStopId,
        BigDecimal chargeAmount,
        String companyId,
        String busId,
        String pan,
        TripStatus status) {

    /** The charge with a dollar sign, e.g. "$3.25". */
    public String formattedChargeAmount() {
        return "$" + chargeAmount.toPlainString();
    }

    /** Null for an incomplete trip, which has no finish time. */
    public Long durationSecs() {
        if (finished == null) {
            return null;
        }
        return Duration.between(started, finished).getSeconds();
    }
}
