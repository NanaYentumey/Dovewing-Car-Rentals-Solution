package com.dovewing.rentals.domain;

/** The classes of vehicle the depot rents out. Each one has a deposit rule below. */
public enum VehicleCategory {
    ECONOMY,
    PREMIUM;

    // A new category will not compile until it is given a rule in this switch.
    double depositFor(double fullDeposit, RenterType renterType) {
        return switch (this) {
            case ECONOMY, PREMIUM -> renterType == RenterType.MEMBER ? 0 : fullDeposit;
        };
    }
}
