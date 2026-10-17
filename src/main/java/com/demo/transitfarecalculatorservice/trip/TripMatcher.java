package com.demo.transitfarecalculatorservice.trip;

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
                trips.add(completedTrip(tapOn, tap));
            }
        }
        return trips;
    }

    private Trip completedTrip(Tap tapOn, Tap tapOff) {
        return new Trip(
                tapOn.dateTimeUtc(),
                tapOff.dateTimeUtc(),
                tapOn.stopId(),
                tapOff.stopId(),
                fareCalculator.fareBetween(tapOn.stopId(), tapOff.stopId()),
                tapOn.companyId(),
                tapOn.busId(),
                tapOn.pan(),
                TripStatus.COMPLETED);
    }
}
