package com.dovewing.rentals.frontdesk;

import com.dovewing.rentals.domain.Depot;
import com.dovewing.rentals.domain.Rental;
import com.dovewing.rentals.domain.Vehicle;
import java.io.PrintStream;

/** Prints the end-of-day report. It only reads the depot. */
public final class EndOfDayReport {

    private EndOfDayReport() {
    }

    public static void print(Depot depot, PrintStream out) {
        out.println();
        out.println("=== " + depot.name() + " - end of day ===");
        out.println("Fleet status:");
        for (Vehicle vehicle : depot.vehicles()) {
            out.println("  " + vehicle.model() + " (" + vehicle.registration() + ", "
                    + vehicle.category() + "): " + depot.statusOf(vehicle));
        }
        out.println("Active rentals:");
        for (Rental rental : depot.activeRentals()) {
            out.println("  Rental #" + rental.id() + ": " + rental.vehicle().registration()
                    + " -> " + rental.renter().name() + " (" + rental.days() + " days)");
        }
    }
}
