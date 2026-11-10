package com.dovewing.rentals.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DepotTest {

    private Depot depot;
    private Vehicle vitz;
    private Vehicle prado;
    private Renter kofi;
    private Renter ama;

    @BeforeEach
    void setUp() {
        depot = new Depot("Dovewing Car Rentals");
        vitz = new Vehicle("Toyota Vitz", "GT-1234-20", 150.00, 500.00, VehicleCategory.ECONOMY);
        prado = new Vehicle("Toyota Prado", "GR-5678-22", 600.00, 2000.00, VehicleCategory.PREMIUM);
        depot.registerVehicle(vitz);
        depot.registerVehicle(prado);
        kofi = depot.registerRenter("Kofi Mensah", RenterType.CASUAL);
        ama = depot.registerRenter("Ama Owusu", RenterType.MEMBER);
    }

    // --- The rule: a vehicle is available or rented, never both ---

    @Test
    void rentedVehicleCannotBeRentedAgain() {
        depot.rent(kofi, vitz, 3);

        assertThrows(IllegalStateException.class, () -> depot.rent(ama, vitz, 2));

        assertEquals(1, depot.rentals().size());
        assertEquals(VehicleStatus.RENTED, depot.statusOf(vitz));
    }

    @Test
    void statusChangesWithRentAndReturn() {
        assertEquals(VehicleStatus.AVAILABLE, depot.statusOf(vitz));

        depot.rent(kofi, vitz, 3);
        assertEquals(VehicleStatus.RENTED, depot.statusOf(vitz));
        assertEquals(VehicleStatus.AVAILABLE, depot.statusOf(prado));

        depot.returnVehicle(vitz);
        assertEquals(VehicleStatus.AVAILABLE, depot.statusOf(vitz));
        assertTrue(depot.activeRentals().isEmpty());
    }

    @Test
    void returnedVehicleCanBeRentedAgain() {
        depot.rent(kofi, vitz, 3);
        depot.returnVehicle(vitz);

        depot.rent(ama, vitz, 2);

        assertEquals(1, depot.activeRentals().size());
    }

    @Test
    void twoVehiclesCannotHaveTheSamePlate() {
        Vehicle clash = new Vehicle("Kia Rio", "gt-1234-20", 100.00, 300.00, VehicleCategory.ECONOMY);

        assertThrows(IllegalArgumentException.class, () -> depot.registerVehicle(clash));
    }

    // --- Pricing: every vehicle class with every renter type ---

    @Test
    void casualRenterPaysDepositOnEconomy() {
        Rental rental = depot.rent(kofi, vitz, 3);

        assertEquals(450.00, rental.cost());
        assertEquals(500.00, rental.deposit());
    }

    @Test
    void casualRenterPaysDepositOnPremium() {
        Rental rental = depot.rent(kofi, prado, 2);

        assertEquals(1200.00, rental.cost());
        assertEquals(2000.00, rental.deposit());
    }

    @Test
    void memberPaysNoDepositOnEconomy() {
        Rental rental = depot.rent(ama, vitz, 2);

        assertEquals(300.00, rental.cost());
        assertEquals(0.00, rental.deposit());
    }

    @Test
    void memberPaysNoDepositOnPremium() {
        Rental rental = depot.rent(ama, prado, 2);

        assertEquals(1200.00, rental.cost());
        assertEquals(0.00, rental.deposit());
    }

    // --- Rentals list ---

    @Test
    void rentalIdsCountUpFromOne() {
        assertEquals(1, depot.rent(kofi, vitz, 3).id());
        assertEquals(2, depot.rent(ama, prado, 2).id());
    }

    @Test
    void returningMarksTheRentalAsReturned() {
        depot.rent(ama, prado, 2);

        assertTrue(depot.returnVehicle(prado));

        assertEquals(RentalStatus.RETURNED, depot.rentals().get(0).status());
    }

    @Test
    void returningAVehicleThatIsNotOutChangesNothing() {
        assertFalse(depot.returnVehicle(prado));

        assertTrue(depot.rentals().isEmpty());
        assertEquals(VehicleStatus.AVAILABLE, depot.statusOf(prado));
    }

    @Test
    void historyListsEachRentalByModel() {
        depot.rent(ama, prado, 2);
        depot.returnVehicle(prado);
        depot.rent(ama, vitz, 1);

        assertEquals(List.of("Rented Toyota Prado", "Rented Toyota Vitz"), depot.historyOf(ama));
        assertTrue(depot.historyOf(kofi).isEmpty());
    }

    // --- Outside code cannot change what the depot protects ---

    @Test
    void returnedListsCannotBeChanged() {
        depot.rent(kofi, vitz, 3);

        assertThrows(UnsupportedOperationException.class, () -> depot.vehicles().clear());
        assertThrows(UnsupportedOperationException.class, () -> depot.renters().clear());
        assertThrows(UnsupportedOperationException.class, () -> depot.rentals().clear());
        assertThrows(UnsupportedOperationException.class, () -> depot.activeRentals().clear());
        assertThrows(UnsupportedOperationException.class, () -> depot.historyOf(kofi).clear());
    }

    @Test
    void fakeMemberIsRefused() {
        Renter fake = new Renter(kofi.id(), kofi.name(), RenterType.MEMBER);

        assertThrows(IllegalArgumentException.class, () -> depot.rent(fake, vitz, 3));
    }

    @Test
    void copyOfAVehicleWithALowerRateIsRefused() {
        Vehicle fake = new Vehicle(vitz.model(), vitz.registration(), 0.00, 0.00, vitz.category());

        assertThrows(IllegalArgumentException.class, () -> depot.rent(kofi, fake, 3));
        assertThrows(IllegalArgumentException.class, () -> depot.returnVehicle(fake));
    }

    @Test
    void renterFromAnotherDepotIsRefused() {
        Renter stranger = new Depot("Elsewhere").registerRenter("Yaw Boateng", RenterType.MEMBER);

        assertThrows(IllegalArgumentException.class, () -> depot.rent(stranger, vitz, 3));
    }

    // --- Bad input ---

    @Test
    void rentalMustBeAtLeastOneDay() {
        assertThrows(IllegalArgumentException.class, () -> depot.rent(kofi, vitz, 0));
        assertThrows(IllegalArgumentException.class, () -> depot.rent(kofi, vitz, -3));
        assertTrue(depot.rentals().isEmpty());
    }

    @Test
    void moneyCannotBeNegativeOrNotANumber() {
        assertThrows(IllegalArgumentException.class,
                () -> new Vehicle("Kia Rio", "GW-1-23", -1.00, 300.00, VehicleCategory.ECONOMY));
        assertThrows(IllegalArgumentException.class,
                () -> new Vehicle("Kia Rio", "GW-1-23", 100.00, Double.NaN, VehicleCategory.ECONOMY));
        assertThrows(IllegalArgumentException.class,
                () -> new Vehicle("Kia Rio", "GW-1-23", Double.POSITIVE_INFINITY, 300.00,
                        VehicleCategory.ECONOMY));
    }

    @Test
    void namesCannotBeEmptyOrContainLineBreaks() {
        assertThrows(IllegalArgumentException.class,
                () -> depot.registerRenter(null, RenterType.CASUAL));
        assertThrows(IllegalArgumentException.class,
                () -> depot.registerRenter("  ", RenterType.CASUAL));
        assertThrows(IllegalArgumentException.class,
                () -> depot.registerRenter("Kofi\n  Rental #9: fake", RenterType.CASUAL));
    }

    @Test
    void nullsAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> depot.registerVehicle(null));
        assertThrows(IllegalArgumentException.class, () -> depot.registerRenter("Esi", null));
        assertThrows(IllegalArgumentException.class, () -> depot.rent(null, vitz, 1));
        assertThrows(IllegalArgumentException.class, () -> depot.rent(kofi, null, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Vehicle("Kia Rio", "GW-1-23", 100.00, 300.00, null));
    }
}
