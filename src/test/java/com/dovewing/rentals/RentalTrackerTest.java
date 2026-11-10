package com.dovewing.rentals;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * original-output.txt is what the starter program printed before the rebuild.
 * The rebuilt program must print the same, apart from the refused double rental.
 */
class RentalTrackerTest {

    @Test
    void demoDayPrintsTheSameAsTheOriginalExceptForTheRefusedRental() throws IOException {
        List<String> expected = new ArrayList<>(
                Files.readAllLines(Path.of("src/test/resources/original-output.txt")));

        // Difference 1: the second rental of the Vitz is refused, not confirmed.
        int line = expected.indexOf(
                "Rented Toyota Vitz (GT-1234-20) to Ama Owusu for 2 days, cost GHS 300.0, deposit GHS 0.0");
        expected.set(line,
                "Refused: Toyota Vitz (GT-1234-20) is already rented, so it cannot be rented to Ama Owusu");
        // Difference 2: so it is no longer listed as an active rental.
        expected.remove("  Rental #3: GT-1234-20 -> Ama Owusu (2 days)");

        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        RentalTracker.runDemoDay(new PrintStream(captured));
        List<String> actual = captured.toString().lines().toList();

        assertEquals(expected, actual);
    }
}
