package com.dovewing.rentals;

import com.dovewing.rentals.domain.Depot;
import com.dovewing.rentals.domain.Renter;
import com.dovewing.rentals.domain.RenterType;
import com.dovewing.rentals.domain.Vehicle;
import com.dovewing.rentals.domain.VehicleCategory;
import com.dovewing.rentals.frontdesk.FrontDesk;
import java.io.PrintStream;

/** Dovewing Car Rentals - fleet and rental tracker. */
public final class RentalTracker {

    private RentalTracker() {
    }

    public static void main(String[] args) {
        runDemoDay(System.out);
    }

    // Takes the output as a parameter so the test can capture what is printed.
    static void runDemoDay(PrintStream out) {
        Depot depot = new Depot("Dovewing Car Rentals");
        FrontDesk desk = new FrontDesk(depot, out);

        Vehicle vitz = new Vehicle(
                "Toyota Vitz", "GT-1234-20", 150.00, 500.00, VehicleCategory.ECONOMY);
        Vehicle prado = new Vehicle(
                "Toyota Prado", "GR-5678-22", 600.00, 2000.00, VehicleCategory.PREMIUM);
        depot.registerVehicle(vitz);
        depot.registerVehicle(prado);

        Renter kofi = depot.registerRenter("Kofi Mensah", RenterType.CASUAL);
        Renter ama = depot.registerRenter("Ama Owusu", RenterType.MEMBER);

        // a normal day
        desk.rent(kofi, vitz, 3);
        desk.rent(ama, prado, 2);
        desk.returnVehicle(prado);

        // the Vitz is already out with Kofi, so this is now refused
        desk.rent(ama, vitz, 2);

        desk.printEndOfDayReport();
    }
}
