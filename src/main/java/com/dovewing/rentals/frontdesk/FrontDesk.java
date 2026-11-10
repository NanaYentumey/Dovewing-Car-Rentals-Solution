package com.dovewing.rentals.frontdesk;

import com.dovewing.rentals.domain.Depot;
import com.dovewing.rentals.domain.Rental;
import com.dovewing.rentals.domain.Renter;
import com.dovewing.rentals.domain.Vehicle;
import com.dovewing.rentals.domain.VehicleStatus;
import java.io.PrintStream;

/** Prints what happens at the front desk. The rules themselves are in Depot. */
public final class FrontDesk {

    private final Depot depot;
    private final PrintStream out;

    public FrontDesk(Depot depot, PrintStream out) {
        if (depot == null || out == null) {
            throw new IllegalArgumentException("Depot and output must not be null");
        }
        this.depot = depot;
        this.out = out;
    }

    public void rent(Renter renter, Vehicle vehicle, int days) {
        if (depot.statusOf(vehicle) == VehicleStatus.RENTED) {
            out.println("Refused: " + vehicle.model() + " (" + vehicle.registration()
                    + ") is already rented, so it cannot be rented to " + renter.name());
            return;
        }
        Rental rental = depot.rent(renter, vehicle, days);
        out.println("Rented " + vehicle.model() + " (" + vehicle.registration() + ") to "
                + renter.name() + " for " + days + " days, cost GHS " + rental.cost()
                + ", deposit GHS " + rental.deposit());
    }

    public void returnVehicle(Vehicle vehicle) {
        depot.returnVehicle(vehicle);
        out.println("Returned " + vehicle.model() + " (" + vehicle.registration() + ")");
    }

    public void printEndOfDayReport() {
        EndOfDayReport.print(depot, out);
    }
}
