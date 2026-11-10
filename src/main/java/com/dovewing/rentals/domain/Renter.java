package com.dovewing.rentals.domain;

/** A person who rents from the depot. The depot gives each renter their id. */
public record Renter(int id, String name, RenterType type) {

    public Renter {
        Checks.requireAtLeastOne(id, "Renter id");
        Checks.requireText(name, "Renter name");
        Checks.requireNotNull(type, "Renter type");
    }
}
