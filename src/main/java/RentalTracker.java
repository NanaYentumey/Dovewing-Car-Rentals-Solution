// Dovewing Car Rentals - fleet and rental tracker
// Original program, left behind by a former colleague. It works, but the front
// desk keeps a paper backup because they do not trust it. Run it first to see why,
// and look closely at the active rentals in the end-of-day report.

import java.util.*;

public class RentalTracker {

    public static void main(String[] args) {
        Depot d = new Depot("Dovewing Car Rentals");

        Vehicle vitz = new Vehicle("Toyota Vitz", "GT-1234-20", 150.00, 500.00, "ECONOMY");
        Vehicle prado = new Vehicle("Toyota Prado", "GR-5678-22", 600.00, 2000.00, "PREMIUM");
        d.addVehicle(vitz);
        d.addVehicle(prado);

        Renter kofi = new Renter("Kofi Mensah", "CASUAL");
        Renter ama = new Renter("Ama Owusu", "MEMBER");
        d.addRenter(kofi);
        d.addRenter(ama);

        // a normal day
        d.rent(kofi, vitz, 3);
        d.rent(ama, prado, 2);
        d.returnVehicle(prado);

        // ... and the sort of thing that makes them keep a paper backup:
        d.rent(ama, vitz, 2); // the Vitz is already out with Kofi, but nothing stops this

        d.printReport();
    }
}

class Vehicle {
    public String model;
    public String reg;
    public double dailyRate;
    public double deposit;
    public String category; // "ECONOMY" or "PREMIUM"
    public String status;   // "AVAILABLE" or "RENTED"
    public int id;
    static int counter = 1;

    public Vehicle(String model, String reg, double dailyRate, double deposit, String category) {
        this.id = counter;
        counter++;
        this.model = model;
        this.reg = reg;
        this.dailyRate = dailyRate;
        this.deposit = deposit;
        this.category = category;
        this.status = "AVAILABLE";
    }
}

class Renter {
    public String name;
    public String kind; // "CASUAL" or "MEMBER"
    public int id;
    public List<String> history = new ArrayList<>();
    static int counter = 1;

    public Renter(String name, String kind) {
        this.id = counter;
        counter++;
        this.name = name;
        this.kind = kind;
    }
}

class Rental {
    public int id;
    public String renterName;
    public String vehicleReg;
    public int days;
    public double cost;
    public double deposit;
    public String status; // "ACTIVE" or "RETURNED"
    static int counter = 1;

    public Rental(String renterName, String vehicleReg, int days, double cost, double deposit) {
        this.id = counter;
        counter++;
        this.renterName = renterName;
        this.vehicleReg = vehicleReg;
        this.days = days;
        this.cost = cost;
        this.deposit = deposit;
        this.status = "ACTIVE";
    }
}

class Depot {
    public String name;
    public List<Vehicle> vehicles = new ArrayList<>();
    public List<Renter> renters = new ArrayList<>();
    private List<Rental> rentals = new ArrayList<>();

    public Depot(String name) {
        this.name = name;
    }

    public void addVehicle(Vehicle v) {
        vehicles.add(v);
    }

    public void addRenter(Renter r) {
        renters.add(r);
    }

    // Rent a vehicle to a renter for a number of days. The rules depend on the class of vehicle.
    public void rent(Renter r, Vehicle v, int days) {
        double cost = 0, deposit = 0;
        if (v.category.equals("ECONOMY")) {
            cost = v.dailyRate * days;
            deposit = v.deposit;
            if (r.kind.equals("MEMBER")) {
                deposit = 0; // members have their deposit waived
            }
            v.status = "RENTED";
            System.out.println("Rented " + v.model + " (" + v.reg + ") to " + r.name
                    + " for " + days + " days, cost GHS " + cost + ", deposit GHS " + deposit);
        } else if (v.category.equals("PREMIUM")) {
            cost = v.dailyRate * days;
            deposit = v.deposit;
            if (r.kind.equals("MEMBER")) {
                deposit = 0; // members have their deposit waived
            }
            v.status = "RENTED";
            System.out.println("Rented " + v.model + " (" + v.reg + ") to " + r.name
                    + " for " + days + " days, cost GHS " + cost + ", deposit GHS " + deposit);
        }
        r.history.add("Rented " + v.model);
        rentals.add(new Rental(r.name, v.reg, days, cost, deposit));
    }

    public void returnVehicle(Vehicle v) {
        v.status = "AVAILABLE";
        for (Rental rt : rentals) {
            if (rt.vehicleReg.equals(v.reg) && rt.status.equals("ACTIVE")) {
                rt.status = "RETURNED";
            }
        }
        System.out.println("Returned " + v.model + " (" + v.reg + ")");
    }

    public List<Rental> getRentals() {
        return rentals;
    }

    public void printReport() {
        System.out.println();
        System.out.println("=== " + name + " - end of day ===");
        System.out.println("Fleet status:");
        for (Vehicle v : vehicles) {
            System.out.println("  " + v.model + " (" + v.reg + ", " + v.category + "): " + v.status);
        }
        System.out.println("Active rentals:");
        for (Rental rt : rentals) {
            if (rt.status.equals("ACTIVE")) {
                System.out.println("  Rental #" + rt.id + ": " + rt.vehicleReg + " -> " + rt.renterName
                        + " (" + rt.days + " days)");
            }
        }
    }
}
