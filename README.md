# Transit Fare Calculator

- One-shot job: processes the file, then exits.
- Reads card taps from `taps.csv`.
- Pairs each tap on with its tap off to build trips.
- Prices each trip and writes `trips.csv`.

## How to run

#### Java Version: `25`
 
### Build package: 
```shell
./mvnw package
```

### Run with default names 

Reads `taps.csv` and writes `trips.csv` in the current folder.

```shell
java -jar target/transit-fare-calculator-service-0.0.1-SNAPSHOT.jar`
```

### Run with custom input and output names
```shell
java -jar target/transit-fare-calculator-service-0.0.1-SNAPSHOT.jar --input=<path/to/taps.csv> --output=<path/to/trips.csv>
```

## How to test

- Run: `./mvnw test`
- Covers:
  - Completed, cancelled and incomplete trips; fares in both directions.
  - Broken sequences: tap off with no tap on, second tap on, tap off on another bus or company.
  - Unsorted input and sorted output.
  - CSV reading and writing.
  - End to end: example input [sampleTaps.csv](src/test/resources/sampleTaps.csv) vs expected [sampleTrips.csv](src/test/resources/sampleTrips.csv).

## Example

Input: [taps.csv](taps.csv)

```
ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN
1, 22-01-2023 13:00:00, ON, Stop1, Company1, Bus37, 5500005555555559
2, 22-01-2023 13:05:00, OFF, Stop2, Company1, Bus37, 5500005555555559
3, 22-01-2023 09:20:00, ON, Stop3, Company1, Bus36, 4111111111111111
4, 23-01-2023 08:00:00, ON, Stop1, Company1, Bus37, 4111111111111111
5, 23-01-2023 08:02:00, OFF, Stop1, Company1, Bus37, 4111111111111111
6, 24-01-2023 16:30:00, OFF, Stop2, Company1, Bus37, 5500005555555559
```

Output: [trips.csv](trips.csv)

```
Started, Finished, DurationSecs, FromStopId, ToStopId, ChargeAmount, CompanyId, BusID, PAN, Status
22-01-2023 09:20:00, , , Stop3, , $7.30, Company1, Bus36, 4111111111111111, INCOMPLETE
22-01-2023 13:00:00, 22-01-2023 13:05:00, 300, Stop1, Stop2, $3.25, Company1, Bus37, 5500005555555559, COMPLETED
23-01-2023 08:00:00, 23-01-2023 08:02:00, 120, Stop1, Stop1, $0.00, Company1, Bus37, 4111111111111111, CANCELLED
```

- Taps 3 and 4: card taps on again without tapping off, so the first trip is INCOMPLETE.
- Tap 6: tap off with no tap on, so no trip.

## Assumptions

### Matching

| Assumption | Why |
|---|---|
| A trip is matched by card (PAN), company and bus together. | Bus ids are only unique within a company. |
| A second tap on closes the waiting trip as INCOMPLETE, even on the same bus. | The rider never tapped off, so a double tap is charged like a missed tap off. |
| A tap off on a different company or bus closes the waiting trip as INCOMPLETE. The tap off is ignored. | The rider left the first bus without tapping off, and a trip across two buses can't be priced. |
| A tap off with no waiting tap on is ignored and logged. | There is no start stop or time, so the trip can't be priced. Guessing could overcharge the rider. The spec only defines trips that start with a tap on. |
| A tap on still waiting at the end of the file is INCOMPLETE. | No tap off was seen. |
| Input can be in any order. | the example is not sorted. |

### Pricing

| Assumption | Why |
|---|---|
| Fares are the same in both directions. | As per spec. |
| Same start and end stop is CANCELLED, $0.00. | As per spec. |
| INCOMPLETE is charged the highest fare from the start stop. | As per spec. |
| Only Stop1, Stop2 and Stop3 exist. ( but configurable ) | They are the only stops with fares in the brief. |
| Money uses `BigDecimal`. | Exact cents, no floating-point errors. |

### Input and output

| Assumption | Why |
|---|---|
| Output is sorted by start time, then PAN. | Same output on every run, and easy to read. |
| Incomplete trips leave Finished, DurationSecs and ToStopId blank. | There is no tap off. |
| Skipped taps are logged as `WARN`, with the tap id and not the card number. | The skipped tap can be checked later, and card numbers are sensitive. |
| The whole file fits in memory. | Keeps the MVP simple. Large files would need streaming. |

### Rules the spec does not cover

| Assumption | Why |
|---|---|
| A tap off ends the trip. A later tap on, even on the same bus, starts a new trip. | A tap off means the rider has finished the journey. |
| Trips are never joined. Stop1 to Stop2, then Stop2 to Stop3, costs $3.25 + $5.50, not $7.30. | The spec has no transfer requiements, and each output row has only one bus. |
| A tap on or tap off on another bus ends the waiting trip. | Both mean the rider got off the first bus. |
| A trip is on one company and one bus. | Each output row has one CompanyId and one BusID. |
| Tap type, company and bus are used as given. We never guess ON or OFF from the order. | The input states them clearly and is assumed to be correct. |
| A tap off with no tap on is not charged. | The spec only charges trips that start with a tap on. |
| A tap on and off at the same stop is CANCELLED, with no time limit. | That is the spec. A real system might only allow this within a few minutes, but the spec has no such rule. |
| A tap on and its tap off are paired however far apart they are, even on different days. | The spec gives no time limit, and picking one would be a guess. |
| An unknown stop is an error, not a $0.00 fare. | Charging nothing would hide bad data. |
