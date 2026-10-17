package com.demo.transitfarecalculatorservice.trip;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.demo.transitfarecalculatorservice.fare.FareCalculator;
import com.demo.transitfarecalculatorservice.tap.Tap;
import com.demo.transitfarecalculatorservice.tap.TapType;

@Component
public class TripMatcher {

    private static final BigDecimal NO_CHARGE = new BigDecimal("0.00");

    private final FareCalculator fareCalculator;

    public TripMatcher(FareCalculator fareCalculator) {
        this.fareCalculator = fareCalculator;
    }

    public List<Trip> match(List<Tap> taps) {
        // For each card (PAN), the tap on that is still waiting for its tap off.
        Map<String, Tap> tapOnWaitingByPan = new HashMap<>();
        List<Trip> trips = new ArrayList<>();

        for (Tap tap : taps) {
            if (tap.type() == TapType.ON) {
                tapOnWaitingByPan.put(tap.pan(), tap);
            } else {
                Tap tapOn = tapOnWaitingByPan.remove(tap.pan());
                trips.add(finishedTrip(tapOn, tap));
            }
        }

        // Any tap on still waiting at the end never had a tap off.
        for (Tap tapOn : tapOnWaitingByPan.values()) {
            trips.add(incompleteTrip(tapOn));
        }
        return trips;
    }

    private Trip finishedTrip(Tap tapOn, Tap tapOff) {
        boolean sameStop = tapOn.stopId().equals(tapOff.stopId());
        if (sameStop) {
            return cancelledTrip(tapOn, tapOff);
        }
        return completedTrip(tapOn, tapOff);
    }

    private Trip completedTrip(Tap tapOn, Tap tapOff) {
        BigDecimal charge = fareCalculator.fareBetween(tapOn.stopId(), tapOff.stopId());
        return buildTrip(tapOn, tapOff, charge, TripStatus.COMPLETED);
    }

    private Trip cancelledTrip(Tap tapOn, Tap tapOff) {
        return buildTrip(tapOn, tapOff, NO_CHARGE, TripStatus.CANCELLED);
    }

    private Trip incompleteTrip(Tap tapOn) {
        return new Trip(
                tapOn.dateTimeUtc(),
                null,
                tapOn.stopId(),
                null,
                fareCalculator.maxFareFrom(tapOn.stopId()),
                tapOn.companyId(),
                tapOn.busId(),
                tapOn.pan(),
                TripStatus.INCOMPLETE);
    }

    private Trip buildTrip(Tap tapOn, Tap tapOff, BigDecimal charge, TripStatus status) {
        return new Trip(
                tapOn.dateTimeUtc(),
                tapOff.dateTimeUtc(),
                tapOn.stopId(),
                tapOff.stopId(),
                charge,
                tapOn.companyId(),
                tapOn.busId(),
                tapOn.pan(),
                status);
    }
}
