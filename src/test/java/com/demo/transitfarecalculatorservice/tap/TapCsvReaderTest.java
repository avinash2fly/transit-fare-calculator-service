package com.demo.transitfarecalculatorservice.tap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.demo.transitfarecalculatorservice.helper.DateTimeHelper;

class TapCsvReaderTest {

    private TapCsvReader reader;

    @BeforeEach
    void setUp() {
        reader = new TapCsvReader();
    }

    @Test
    void readsTapsAndTrimsTheSpacesAfterEachComma() {
        String csv = """
                ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN
                1, 22-01-2023 13:00:00, ON, Stop1, Company1, Bus37, 5500005555555559
                2, 22-01-2023 13:05:00, OFF, Stop2, Company1, Bus37, 5500005555555559
                """;

        List<Tap> taps = reader.read(new StringReader(csv));

        assertEquals(List.of(
                new Tap(1, DateTimeHelper.parse("22-01-2023 13:00:00"), TapType.ON,
                        "Stop1", "Company1", "Bus37", "5500005555555559"),
                new Tap(2, DateTimeHelper.parse("22-01-2023 13:05:00"), TapType.OFF,
                        "Stop2", "Company1", "Bus37", "5500005555555559")),
                taps);
    }
}
