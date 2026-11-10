package com.dovewing.rentals.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * The depot: its vehicles, its renters and its rentals.
 *
 * <p>This is the only class whose data changes, and all of it is private. A
 * vehicle's status is never stored; it is worked out from the rentals list, so it
 * can never disagree with the rentals.
 *
 * <p>The methods are synchronized so two threads cannot rent the same vehicle at
 * the same moment.
 */
public final class Depot {

    private final String name;
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Renter> renters = new ArrayList<>();
    private final List<Rental> rentals = new ArrayList<>();
    private int nextRenterId = 1;
    private int nextRentalId = 1;

    public Depot(String name) {
        Checks.requireText(name, "Depot name");
        this.name = name;
    }

    public String name() {
        return name;
    }

    public synchronized void registerVehicle(Vehicle vehicle) {
        Checks.requireNotNull(vehicle, "Vehicle");
        for (Vehicle existing : vehicles) {
            if (existing.registration().equalsIgnoreCase(vehicle.registration())) {
                throw new IllegalArgumentException(
                        "Registration " + vehicle.registration() + " is already in the fleet");
            }
        }
        vehicles.add(vehicle);
    }

    public synchronized Renter registerRenter(String name, RenterType type) {
        Renter renter = new Renter(nextRenterId, name, type);
        renters.add(renter);
        nextRenterId++;
        return renter;
    }

    public synchronized Rental rent(Renter renter, Vehicle vehicle, int days) {
        requireRegistered(renter);
        requireRegistered(vehicle);
        if (statusOf(vehicle) == VehicleStatus.RENTED) {
            throw new IllegalStateException(vehicle.registration() + " is already rented");
        }

        double cost = vehicle.costFor(days);
        double deposit = vehicle.depositFor(renter);
        Rental rental = new Rental(
                nextRentalId, renter, vehicle, days, cost, deposit, RentalStatus.ACTIVE);
        rentals.add(rental);
        nextRentalId++;
        return rental;
    }

    /** Returns true if a rental was closed, false if the vehicle was not out. */
    public synchronized boolean returnVehicle(Vehicle vehicle) {
        requireRegistered(vehicle);
        for (int i = 0; i < rentals.size(); i++) {
            Rental rental = rentals.get(i);
            if (rental.isActive() && rental.vehicle().equals(vehicle)) {
                rentals.set(i, rental.returned());
                return true;
            }
        }
        return false;
    }

    public synchronized VehicleStatus statusOf(Vehicle vehicle) {
        requireRegistered(vehicle);
        for (Rental rental : rentals) {
            if (rental.isActive() && rental.vehicle().equals(vehicle)) {
                return VehicleStatus.RENTED;
            }
        }
        return VehicleStatus.AVAILABLE;
    }

    // The getters below return copies, so callers cannot change the depot's own lists.

    public synchronized List<Vehicle> vehicles() {
        return List.copyOf(vehicles);
    }

    public synchronized List<Renter> renters() {
        return List.copyOf(renters);
    }

    public synchronized List<Rental> rentals() {
        return List.copyOf(rentals);
    }

    public synchronized List<Rental> activeRentals() {
        List<Rental> active = new ArrayList<>();
        for (Rental rental : rentals) {
            if (rental.isActive()) {
                active.add(rental);
            }
        }
        return List.copyOf(active);
    }

    public synchronized List<String> historyOf(Renter renter) {
        requireRegistered(renter);
        List<String> history = new ArrayList<>();
        for (Rental rental : rentals) {
            if (rental.renter().equals(renter)) {
                history.add("Rented " + rental.vehicle().model());
            }
        }
        return List.copyOf(history);
    }

    // contains() compares every field, so a copy of a vehicle with a lower rate,
    // or a copy of a renter changed to MEMBER, does not count as registered.

    private void requireRegistered(Vehicle vehicle) {
        Checks.requireNotNull(vehicle, "Vehicle");
        if (!vehicles.contains(vehicle)) {
            throw new IllegalArgumentException(
                    "Vehicle " + vehicle.registration() + " is not registered with this depot");
        }
    }

    private void requireRegistered(Renter renter) {
        Checks.requireNotNull(renter, "Renter");
        if (!renters.contains(renter)) {
            throw new IllegalArgumentException(
                    "Renter " + renter.name() + " is not registered with this depot");
        }
    }
}
