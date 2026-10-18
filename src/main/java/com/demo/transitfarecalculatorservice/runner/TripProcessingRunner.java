package com.demo.transitfarecalculatorservice.runner;

import com.demo.transitfarecalculatorservice.tap.Tap;
import com.demo.transitfarecalculatorservice.tap.TapCsvReader;
import com.demo.transitfarecalculatorservice.trip.Trip;
import com.demo.transitfarecalculatorservice.trip.TripCsvWriter;
import com.demo.transitfarecalculatorservice.trip.TripMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class TripProcessingRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TripProcessingRunner.class);

    private final TapCsvReader tapCsvReader;
    private final TripMatcher tripMatcher;
    private final TripCsvWriter tripCsvWriter;

    public TripProcessingRunner(TapCsvReader tapCsvReader, TripMatcher tripMatcher, TripCsvWriter tripCsvWriter) {
        this.tapCsvReader = tapCsvReader;
        this.tripMatcher = tripMatcher;
        this.tripCsvWriter = tripCsvWriter;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        Path input = Path.of(option(args, "input", "taps.csv"));
        Path output = Path.of(option(args, "output", "trips.csv"));

        if (!Files.exists(input)) {
            throw new IllegalArgumentException("Input file not found: " + input);
        }

        List<Tap> taps;
        try (Reader reader = Files.newBufferedReader(input)) {
            taps = tapCsvReader.read(reader);
        }

        List<Trip> trips = tripMatcher.match(taps);

        try (Writer writer = Files.newBufferedWriter(output)) {
            tripCsvWriter.write(trips, writer);
        }
        log.info("Read {} taps from {} and wrote {} trips to {}", taps.size(), input, trips.size(), output);
    }

    private String option(ApplicationArguments args, String name, String defaultValue) {
        List<String> values = args.getOptionValues(name);
        if (values == null || values.isEmpty()) {
            return defaultValue;
        }
        return values.get(0);
    }
}
