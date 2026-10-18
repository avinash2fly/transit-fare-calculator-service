package com.demo.transitfarecalculatorservice.trip;

import com.demo.transitfarecalculatorservice.helper.DateTimeHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TripCsvWriterTest {

    private TripCsvWriter writer;

    @BeforeEach
    void setUp() {
        writer = new TripCsvWriter();
    }

    @Test
    void writesAHeaderAndOneLinePerTripWithBlanksForAnIncompleteTrip() {
        Trip completed = new Trip(
                DateTimeHelper.parse("22-01-2023 13:00:00"), DateTimeHelper.parse("22-01-2023 13:05:00"),
                "Stop1", "Stop2", new BigDecimal("3.25"), "Company1", "Bus37", "5500005555555559",
                TripStatus.COMPLETED);
        Trip cancelled = new Trip(
                DateTimeHelper.parse("23-01-2023 08:00:00"), DateTimeHelper.parse("23-01-2023 08:02:00"),
                "Stop1", "Stop1", new BigDecimal("0.00"), "Company1", "Bus37", "4111111111111111",
                TripStatus.CANCELLED);
        Trip incomplete = new Trip(
                DateTimeHelper.parse("22-01-2023 09:20:00"), null,
                "Stop3", null, new BigDecimal("7.30"), "Company1", "Bus36", "4111111111111111",
                TripStatus.INCOMPLETE);

        StringWriter out = new StringWriter();
        writer.write(List.of(completed, cancelled, incomplete), out);

        assertEquals("""
                Started, Finished, DurationSecs, FromStopId, ToStopId, ChargeAmount, CompanyId, BusID, PAN, Status
                22-01-2023 13:00:00, 22-01-2023 13:05:00, 300, Stop1, Stop2, $3.25, Company1, Bus37, 5500005555555559, COMPLETED
                23-01-2023 08:00:00, 23-01-2023 08:02:00, 120, Stop1, Stop1, $0.00, Company1, Bus37, 4111111111111111, CANCELLED
                22-01-2023 09:20:00, , , Stop3, , $7.30, Company1, Bus36, 4111111111111111, INCOMPLETE
                """, out.toString());
    }
}
