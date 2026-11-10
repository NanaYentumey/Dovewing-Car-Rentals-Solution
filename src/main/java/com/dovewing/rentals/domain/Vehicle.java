package com.dovewing.rentals.domain;

/**
 * A vehicle in the fleet. It has no status field on purpose: the depot works out
 * whether it is available or rented from its list of rentals.
 */
public record Vehicle(
        String model,
        String registration,
        double dailyRate,
        double deposit,
        VehicleCategory category) {

    public Vehicle {
        Checks.requireText(model, "Vehicle model");
        Checks.requireText(registration, "Vehicle registration");
        Checks.requireAmount(dailyRate, "Daily rate");
        Checks.requireAmount(deposit, "Deposit");
        Checks.requireNotNull(category, "Vehicle category");
    }

    public double costFor(int days) {
        Checks.requireAtLeastOne(days, "Rental days");
        double cost = dailyRate * days;
        Checks.requireAmount(cost, "Rental cost");
        return cost;
    }

    public double depositFor(Renter renter) {
        Checks.requireNotNull(renter, "Renter");
        return category.depositFor(deposit, renter.type());
    }
}
