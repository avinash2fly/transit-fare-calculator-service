package com.demo.transitfarecalculatorservice.trip;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.demo.transitfarecalculatorservice.fare.FareCalculator;
import com.demo.transitfarecalculatorservice.tap.Tap;
import com.demo.transitfarecalculatorservice.tap.TapType;

@Component
public class TripMatcher {

    private static final Logger log = LoggerFactory.getLogger(TripMatcher.class);

    private static final BigDecimal NO_CHARGE = new BigDecimal("0.00");

    private final FareCalculator fareCalculator;

    public TripMatcher(FareCalculator fareCalculator) {
        this.fareCalculator = fareCalculator;
    }

    public List<Trip> match(List<Tap> taps) {
        // For each card (PAN), the tap on that is still waiting for its tap off.
        Map<String, Tap> tapOnWaitingByPan = new HashMap<>();
        List<Trip> trips = new ArrayList<>();

        // Input is not in time order, and pairing a tap off with its tap on depends on it.
        List<Tap> tapsInTimeOrder = taps.stream()
                .sorted(Comparator.comparing(Tap::dateTimeUtc))
                .toList();

        for (Tap tap : tapsInTimeOrder) {
            Tap tapOn = tapOnWaitingByPan.remove(tap.pan());

            if (tap.type() == TapType.ON) {
                // A new tap on means the rider left the earlier bus without tapping off.
                if (tapOn != null) {
                    trips.add(incompleteTrip(tapOn));
                }
                tapOnWaitingByPan.put(tap.pan(), tap);
            } else if (tapOn == null) {
                log.warn("Ignoring tap off with id {}: no tap on is waiting for this card", tap.id());
            } else if (sameBus(tapOn, tap)) {
                trips.add(finishedTrip(tapOn, tap));
            } else {
                // A tap off on another bus shows the rider left the first bus without tapping off.
                log.warn("Ignoring tap off {}: it is on {}/{} but tap on {} was on {}/{}",
                        tap.id(), tap.companyId(), tap.busId(), tapOn.id(), tapOn.companyId(), tapOn.busId());
                trips.add(incompleteTrip(tapOn));
            }
        }

        // Any tap on still waiting at the end never had a tap off.
        for (Tap tapOn : tapOnWaitingByPan.values()) {
            trips.add(incompleteTrip(tapOn));
        }
        return trips;
    }

    /** Bus ids are only unique within a company, so both are compared. */
    private boolean sameBus(Tap tapOn, Tap tapOff) {
        return tapOn.companyId().equals(tapOff.companyId()) && tapOn.busId().equals(tapOff.busId());
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
