package com.demo.transitfarecalculatorservice.trip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.demo.transitfarecalculatorservice.fare.FareConfig;
import com.demo.transitfarecalculatorservice.helper.DateTimeHelper;
import com.demo.transitfarecalculatorservice.tap.Tap;
import com.demo.transitfarecalculatorservice.tap.TapType;

class TripMatcherTest {

    private static final String PAN = "5500005555555559";

    private TripMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new TripMatcher(new FareConfig().fareCalculator());
    }

    @Test
    void tapOnThenTapOffAtDifferentStopsIsACompletedTrip() {
        Tap tapOn = tap(1, "22-01-2023 13:00:00", TapType.ON, "Stop1");
        Tap tapOff = tap(2, "22-01-2023 13:05:00", TapType.OFF, "Stop2");

        List<Trip> trips = matcher.match(List.of(tapOn, tapOff));

        Trip expectedTrip = new Trip(
                tapOn.dateTimeUtc(), tapOff.dateTimeUtc(), "Stop1", "Stop2",
                new BigDecimal("3.25"), "Company1", "Bus37", PAN, TripStatus.COMPLETED);

        assertEquals(List.of(expectedTrip), trips);
        assertEquals(300L, expectedTrip.durationSecs());
    }

    @Test
    void tapOnThenTapOffAtTheSameStopIsACancelledTripAndIsNotCharged() {
        Tap tapOn = tap(1, "23-01-2023 08:00:00", TapType.ON, "Stop1");
        Tap tapOff = tap(2, "23-01-2023 08:02:00", TapType.OFF, "Stop1");

        List<Trip> trips = matcher.match(List.of(tapOn, tapOff));

        Trip expectedTrip = new Trip(
                tapOn.dateTimeUtc(), tapOff.dateTimeUtc(), "Stop1", "Stop1",
                new BigDecimal("0.00"), "Company1", "Bus37", PAN, TripStatus.CANCELLED);

        assertEquals(List.of(expectedTrip), trips);
    }

    @Test
    void tapOnWithoutTapOffIsAnIncompleteTripChargedTheMaxFareFromThatStop() {
        Tap tapOn = tap(1, "22-01-2023 09:20:00", TapType.ON, "Stop3");

        List<Trip> trips = matcher.match(List.of(tapOn));

        Trip expectedTrip = new Trip(
                tapOn.dateTimeUtc(), null, "Stop3", null,
                new BigDecimal("7.30"), "Company1", "Bus37", PAN, TripStatus.INCOMPLETE);

        assertEquals(List.of(expectedTrip), trips);
        assertNull(expectedTrip.durationSecs());
    }

    private static Tap tap(long id, String dateTime,TapType type, String stop) {
        return new Tap(id, DateTimeHelper.parse(dateTime), type, stop, "Company1", "Bus37", PAN);
    }
}
