package com.demo.transitfarecalculatorservice.tap;

import com.demo.transitfarecalculatorservice.helper.DateTimeHelper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class TapCsvReader {

    // The first row holds the column names; values are written as "a, b, c" so spaces are trimmed.
    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .get();

    public List<Tap> read(Reader reader) {
        List<Tap> taps = new ArrayList<>();
        try (CSVParser parser = FORMAT.parse(reader)) {
            for (CSVRecord row : parser) {
                taps.add(toTap(row));
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read taps", e);
        }
        return taps;
    }

    private Tap toTap(CSVRecord row) {
        return new Tap(
                Long.parseLong(row.get("ID")),
                DateTimeHelper.parse(row.get("DateTimeUTC")),
                TapType.valueOf(row.get("TapType")),
                row.get("StopId"),
                row.get("CompanyId"),
                row.get("BusID"),
                row.get("PAN"));
    }
}
