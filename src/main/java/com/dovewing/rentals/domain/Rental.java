package com.dovewing.rentals.domain;

/** One rental in the depot's list. It cannot be changed once created. */
public record Rental(
        int id,
        Renter renter,
        Vehicle vehicle,
        int days,
        double cost,
        double deposit,
        RentalStatus status) {

    public Rental {
        Checks.requireAtLeastOne(id, "Rental id");
        Checks.requireNotNull(renter, "Renter");
        Checks.requireNotNull(vehicle, "Vehicle");
        Checks.requireAtLeastOne(days, "Rental days");
        Checks.requireAmount(cost, "Rental cost");
        Checks.requireAmount(deposit, "Rental deposit");
        Checks.requireNotNull(status, "Rental status");
    }

    public boolean isActive() {
        return status == RentalStatus.ACTIVE;
    }

    /** Returns a copy of this rental with its status set to RETURNED. */
    public Rental returned() {
        return new Rental(id, renter, vehicle, days, cost, deposit, RentalStatus.RETURNED);
    }
}
