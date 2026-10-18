package com.demo.transitfarecalculatorservice.trip;

import com.demo.transitfarecalculatorservice.helper.DateTimeHelper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

@Component
public class TripCsvWriter {

    // Values are separated by ", " as in the example output. A null value is written as blank.
    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setDelimiter(", ")
            .setRecordSeparator("\n")
            .setHeader("Started", "Finished", "DurationSecs", "FromStopId", "ToStopId",
                    "ChargeAmount", "CompanyId", "BusID", "PAN", "Status")
            .get();

    public void write(List<Trip> trips, Writer writer) {
        try (CSVPrinter printer = new CSVPrinter(writer, FORMAT)) {
            for (Trip trip : trips) {
                printer.printRecord(
                        DateTimeHelper.format(trip.started()),
                        DateTimeHelper.format(trip.finished()),
                        trip.durationSecs(),
                        trip.fromStopId(),
                        trip.toStopId(),
                        trip.formattedChargeAmount(),
                        trip.companyId(),
                        trip.busId(),
                        trip.pan(),
                        trip.status());
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not write trips", e);
        }
    }
}
