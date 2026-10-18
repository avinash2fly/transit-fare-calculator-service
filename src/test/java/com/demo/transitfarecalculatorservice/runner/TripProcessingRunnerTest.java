package com.demo.transitfarecalculatorservice.runner;

import com.demo.transitfarecalculatorservice.fare.FareConfig;
import com.demo.transitfarecalculatorservice.tap.TapCsvReader;
import com.demo.transitfarecalculatorservice.trip.TripCsvWriter;
import com.demo.transitfarecalculatorservice.trip.TripMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.DefaultApplicationArguments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TripProcessingRunnerTest {

    @TempDir
    Path folder;

    private TripProcessingRunner runner;

    @BeforeEach
    void setUp() {
        runner = new TripProcessingRunner(
                new TapCsvReader(),
                new TripMatcher(new FareConfig().fareCalculator()),
                new TripCsvWriter());
    }

    @Test
    void readsTheInputFileAndWritesTheTripsToTheOutputFile() throws IOException {
        Path input = folder.resolve("taps.csv");
        Path output = folder.resolve("trips.csv");
        Files.writeString(input, """
                ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN
                1, 22-01-2023 13:00:00, ON, Stop1, Company1, Bus37, 5500005555555559
                2, 22-01-2023 13:05:00, OFF, Stop2, Company1, Bus37, 5500005555555559
                """);

        runner.run(new DefaultApplicationArguments("--input=" + input, "--output=" + output));

        assertEquals("""
                Started, Finished, DurationSecs, FromStopId, ToStopId, ChargeAmount, CompanyId, BusID, PAN, Status
                22-01-2023 13:00:00, 22-01-2023 13:05:00, 300, Stop1, Stop2, $3.25, Company1, Bus37, 5500005555555559, COMPLETED
                """, Files.readString(output));
    }

    @Test
    void producesTheExpectedTripsForTheSampleTaps() throws Exception {
        Path sampleTaps = Path.of(getClass().getResource("/sampleTaps.csv").toURI());
        Path sampleTrips = Path.of(getClass().getResource("/sampleTrips.csv").toURI());
        Path output = folder.resolve("trips.csv");

        runner.run(new DefaultApplicationArguments("--input=" + sampleTaps, "--output=" + output));

        assertEquals(Files.readString(sampleTrips), Files.readString(output));
    }

    @Test
    void failsWithAClearMessageWhenTheInputFileIsMissing() {
        Path input = folder.resolve("missing.csv");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> runner.run(new DefaultApplicationArguments("--input=" + input)));

        assertEquals("Input file not found: " + input, error.getMessage());
    }
}
