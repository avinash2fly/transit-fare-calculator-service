package com.demo.transitfarecalculatorservice.trip;

import com.demo.transitfarecalculatorservice.fare.FareConfig;
import com.demo.transitfarecalculatorservice.helper.DateTimeHelper;
import com.demo.transitfarecalculatorservice.tap.Tap;
import com.demo.transitfarecalculatorservice.tap.TapType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    void tapsArePairedInTimeOrderNotInputOrder() {
        String otherPan = "4111111111111111";
        Tap tapOff = tap(1, "22-01-2023 13:05:00", TapType.OFF, "Stop2", PAN);
        Tap tapOn = tap(2, "22-01-2023 13:00:00", TapType.ON, "Stop1", PAN);
        Tap otherTapOff = tap(3, "22-01-2023 09:30:00", TapType.OFF, "Stop2", otherPan);
        Tap otherTapOn = tap(4, "22-01-2023 09:20:00", TapType.ON, "Stop3", otherPan);

        List<Trip> trips = matcher.match(List.of(tapOff, tapOn, otherTapOff, otherTapOn));

        Trip otherTrip = new Trip(
                otherTapOn.dateTimeUtc(), otherTapOff.dateTimeUtc(), "Stop3", "Stop2",
                new BigDecimal("5.50"), "Company1", "Bus37", otherPan, TripStatus.COMPLETED);
        Trip trip = new Trip(
                tapOn.dateTimeUtc(), tapOff.dateTimeUtc(), "Stop1", "Stop2",
                new BigDecimal("3.25"), "Company1", "Bus37", PAN, TripStatus.COMPLETED);

        assertEquals(List.of(otherTrip, trip), trips);
    }

    @Test
    void tripsAreSortedByStartTimeThenPan() {
        String panA = "3530111333300000";
        String panB = "4111111111111111";

        Tap sameStartFirstPanOn = tap(1, "22-01-2023 13:00:00", TapType.ON, "Stop1", PAN);
        Tap sameStartFirstPanOff = tap(2, "22-01-2023 13:05:00", TapType.OFF, "Stop2", PAN);
        Tap sameStartSecondPanOn = tap(3, "22-01-2023 13:00:00", TapType.ON, "Stop1", panB);
        Tap sameStartSecondPanOff = tap(4, "22-01-2023 13:10:00", TapType.OFF, "Stop2", panB);
        Tap nextDayOn = tap(5, "23-01-2023 08:00:00", TapType.ON, "Stop2", PAN);
        Tap nextDayOff = tap(6, "23-01-2023 08:05:00", TapType.OFF, "Stop3", PAN);
        Tap earlyIncompleteOn = tap(7, "22-01-2023 09:00:00", TapType.ON, "Stop3", panA);

        // random list of input to test sort
        List<Trip> trips = matcher.match(List.of(
                nextDayOff, sameStartFirstPanOn, earlyIncompleteOn, sameStartSecondPanOff,
                nextDayOn, sameStartSecondPanOn, sameStartFirstPanOff));

        Trip earlyIncomplete = new Trip(
                earlyIncompleteOn.dateTimeUtc(), null, "Stop3", null,
                new BigDecimal("7.30"), "Company1", "Bus37", panA, TripStatus.INCOMPLETE);
        Trip firstPan = new Trip(
                sameStartSecondPanOn.dateTimeUtc(), sameStartSecondPanOff.dateTimeUtc(), "Stop1", "Stop2",
                new BigDecimal("3.25"), "Company1", "Bus37", panB, TripStatus.COMPLETED);
        Trip secondPan = new Trip(
                sameStartFirstPanOn.dateTimeUtc(), sameStartFirstPanOff.dateTimeUtc(), "Stop1", "Stop2",
                new BigDecimal("3.25"), "Company1", "Bus37", PAN, TripStatus.COMPLETED);
        Trip nextDay = new Trip(
                nextDayOn.dateTimeUtc(), nextDayOff.dateTimeUtc(), "Stop2", "Stop3",
                new BigDecimal("5.50"), "Company1", "Bus37", PAN, TripStatus.COMPLETED);

        assertEquals(List.of(earlyIncomplete, firstPan, secondPan, nextDay), trips);
    }

    private Tap tap(long id, String dateTime, TapType type, String stop) {
        return tap(id, dateTime, type, stop, PAN);
    }

    private Tap tap(long id, String dateTime, TapType type, String stop, String pan) {
        return new Tap(id, DateTimeHelper.parse(dateTime), type, stop, "Company1", "Bus37", pan);
    }
}
