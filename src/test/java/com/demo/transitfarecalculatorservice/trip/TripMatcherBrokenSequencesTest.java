package com.demo.transitfarecalculatorservice.trip;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.demo.transitfarecalculatorservice.fare.FareConfig;
import com.demo.transitfarecalculatorservice.helper.DateTimeHelper;
import com.demo.transitfarecalculatorservice.tap.Tap;
import com.demo.transitfarecalculatorservice.tap.TapType;

class TripMatcherBrokenSequencesTest {

    private static final String PAN = "5500005555555559";

    private TripMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new TripMatcher(new FareConfig().fareCalculator());
    }

    @Test
    void tapOnOnAnotherBusClosesTheOpenTripAsIncomplete() {
        Tap firstTapOn = tap(1, "22-01-2023 08:00:00", TapType.ON, "Stop1", "Company1", "Bus1");
        Tap secondTapOn = tap(2, "22-01-2023 09:00:00", TapType.ON, "Stop2", "Company2", "Bus2");
        Tap tapOff = tap(3, "22-01-2023 09:10:00", TapType.OFF, "Stop3", "Company2", "Bus2");

        List<Trip> trips = matcher.match(List.of(firstTapOn, secondTapOn, tapOff));

        Trip incompleteTrip = new Trip(
                firstTapOn.dateTimeUtc(), null, "Stop1", null,
                new BigDecimal("7.30"), "Company1", "Bus1", PAN, TripStatus.INCOMPLETE);
        Trip completedTrip = new Trip(
                secondTapOn.dateTimeUtc(), tapOff.dateTimeUtc(), "Stop2", "Stop3",
                new BigDecimal("5.50"), "Company2", "Bus2", PAN, TripStatus.COMPLETED);

        // Same order as sorting by start time, so this stays valid once the output is sorted.
        assertEquals(List.of(incompleteTrip, completedTrip), trips);
    }

    @Test
    void tapOffOnAnotherBusClosesTheOpenTripAsIncompleteAndIsIgnored() {
        Tap tapOn = tap(1, "22-01-2023 08:00:00", TapType.ON, "Stop1", "Company1", "Bus1");
        Tap tapOff = tap(2, "22-01-2023 08:10:00", TapType.OFF, "Stop2", "Company1", "Bus2");

        List<Trip> trips = matcher.match(List.of(tapOn, tapOff));

        Trip expectedTrip = new Trip(
                tapOn.dateTimeUtc(), null, "Stop1", null,
                new BigDecimal("7.30"), "Company1", "Bus1", PAN, TripStatus.INCOMPLETE);

        assertEquals(List.of(expectedTrip), trips);
    }

    @Test
    void tapOffOfAnotherCompanyWithTheSameBusIdIsNotMatched() {
        Tap tapOn = tap(1, "22-01-2023 08:00:00", TapType.ON, "Stop2", "Company1", "Bus1");
        Tap tapOff = tap(2, "22-01-2023 08:10:00", TapType.OFF, "Stop3", "Company2", "Bus1");

        List<Trip> trips = matcher.match(List.of(tapOn, tapOff));

        Trip expectedTrip = new Trip(
                tapOn.dateTimeUtc(), null, "Stop2", null,
                new BigDecimal("5.50"), "Company1", "Bus1", PAN, TripStatus.INCOMPLETE);

        assertEquals(List.of(expectedTrip), trips);
    }

    @Test
    void tapOffOnTheOriginalBusAfterAnIgnoredTapOffIsAlsoIgnored() {
        Tap tapOn = tap(1, "22-01-2023 08:00:00", TapType.ON, "Stop1", "Company1", "Bus1");
        Tap otherBusTapOff = tap(2, "22-01-2023 08:05:00", TapType.OFF, "Stop2", "Company1", "Bus2");
        Tap originalBusTapOff = tap(3, "22-01-2023 08:10:00", TapType.OFF, "Stop2", "Company1", "Bus1");

        List<Trip> trips = matcher.match(List.of(tapOn, otherBusTapOff, originalBusTapOff));

        Trip expectedTrip = new Trip(
                tapOn.dateTimeUtc(), null, "Stop1", null,
                new BigDecimal("7.30"), "Company1", "Bus1", PAN, TripStatus.INCOMPLETE);

        assertEquals(List.of(expectedTrip), trips);
    }

    @Test
    void tapOffWithoutTapOnIsIgnored() {
        Tap tapOff = tap(1, "22-01-2023 08:00:00", TapType.OFF, "Stop1", "Company1", "Bus1");

        List<Trip> trips = matcher.match(List.of(tapOff));

        assertEquals(List.of(), trips);
    }

    private Tap tap(long id, String dateTime, TapType type, String stop, String companyId, String busId) {
        return new Tap(id, DateTimeHelper.parse(dateTime), type, stop, companyId, busId, PAN);
    }
}
