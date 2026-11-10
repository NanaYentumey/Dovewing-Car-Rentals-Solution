# Dovewing Car Rentals: Fleet and Rental Tracker

[![CI](https://github.com/NanaYentumey/Dovewing-Car-Rentals-Solution/actions/workflows/ci.yml/badge.svg)](https://github.com/NanaYentumey/Dovewing-Car-Rentals-Solution/actions/workflows/ci.yml)

A small program the Dovewing depot uses to track its fleet and its rentals. This is the rebuilt version of the single-file starter: the same features and the same output, reorganised so that a vehicle is always either available or rented, never both, and so that a new vehicle class can be added without a rewrite.

- [DIAGNOSIS.md](DIAGNOSIS.md): what was wrong with the starter, ranked by risk, and the design sketch drawn before the rebuild ([docs/design-sketch.jpg](docs/design-sketch.jpg)).
- This README: how to run it, the design notes, the evidence that behaviour is preserved, and the one improvement beyond the brief.

## Running it

You need JDK 21 or later. The program has no runtime dependencies; JUnit is used for the tests only.

**With Maven:**

```bash
mvn compile exec:java
```

```bash
mvn test
```

**Or directly with the JDK (runs the program, not the tests):**

This path does not need Maven and does not need `mvn` to have run first. Run both commands from the project root. `javac` creates `target/classes` itself if the folder does not exist.

```bash
javac --release 21 -d target/classes -sourcepath src/main/java src/main/java/com/dovewing/rentals/RentalTracker.java
```

```bash
java -cp target/classes com.dovewing.rentals.RentalTracker
```

## Structure

```text
com.dovewing.rentals
    RentalTracker                 main: the same demo day as the original

com.dovewing.rentals.domain       the rules; no printing
    Depot                         fleet, renters and rentals list; the only class whose data changes
    Vehicle, Renter, Rental       immutable records, checked when created
    VehicleCategory               ECONOMY, PREMIUM, and the deposit rule for each
    RenterType                    CASUAL, MEMBER (deposit waived)
    VehicleStatus                 AVAILABLE, RENTED
    RentalStatus                  ACTIVE, RETURNED
    Checks                        shared input checks (package-private)

com.dovewing.rentals.frontdesk    the printing; no rules
    FrontDesk                     prints confirmations and refusals
    EndOfDayReport                prints the end-of-day report
```

## Design notes

### 1. A vehicle's status is derived, not stored

The root cause of "available and rented at the same time" was that the original stored the answer twice: `Vehicle.status` and `Rental.status`, with nothing tying them together.

*Alternative considered:* keep a private `status` field on `Vehicle` and have `Depot` update it alongside the rental. That would work today, but it keeps two copies of one fact and relies on every future change remembering to update both. That is exactly the trap the original fell into.

*What I chose:* `Vehicle` has no status at all. `Depot.statusOf(vehicle)` looks in the rental ledger: if there is an active rental for the vehicle it is `RENTED`, otherwise `AVAILABLE`. One fact, one place, so there is nothing to fall out of step. `Depot.rent` then enforces the rule by refusing to add a second active rental (it throws `IllegalStateException`).

### 2. One mutable class, everything else immutable

`Vehicle`, `Renter` and `Rental` are records: their fields are final and checked in the constructor, so an invalid one cannot exist and a valid one cannot be changed. `Depot` is the only class with changing state, all of it `private`. Returning a vehicle does not alter a `Rental`; the depot swaps the ledger entry for a copy marked `RETURNED`.

Everything the depot hands out is either an immutable record or an unmodifiable copy of a list (`List.copyOf`). There is no setter, no public field and no way to reach the internal lists, so outside code cannot change a status, a rate, or the ledger.

*Why records rather than classes with getters:* less code to get wrong, and value equality for free, which design note 4 depends on.

### 3. Enums instead of strings

`"ECONOMY"`, `"MEMBER"`, `"RENTED"` and `"ACTIVE"` were free text, so a typo such as `"Premium"` compiled and produced a free, unrecorded-as-rented car. They are now enums. A misspelt category is a compile error, and the enum names print exactly as the old strings did, so the report is unchanged.

### 4. The depot trusts only what it registered

Records have public constructors, so anyone can build `new Renter(1, "Kofi Mensah", RenterType.MEMBER)` to try for a waived deposit, or a copy of the Vitz with a daily rate of zero. `Depot` therefore checks that what it is handed is in its own list (`vehicles.contains(vehicle)`). For records, `contains` compares every field, so anything that differs is refused. Renter ids are issued by the depot (`registerRenter`), not chosen by the caller, and replace the original's global `static` counters.

### 5. Rules and printing are separated

The original `Depot` both decided and printed. Now `Depot` returns results (`Rental`, `VehicleStatus`, `true`/`false`) and `FrontDesk` / `EndOfDayReport` turn them into the original wording. This is what makes the rules testable, and it means rewording a message cannot break a rule.

### 6. The rule is checked twice, on purpose

`FrontDesk.rent` asks `depot.statusOf(vehicle)` first and prints a `Refused:` line if the car is out, so the person at the desk is told and the program carries on. `Depot.rent` checks again and throws `IllegalStateException`. The second check is the one that matters: it means the rule holds even if some future caller forgets to ask first.

### How the commercial-van class is added

The deposit rule for every vehicle class lives in one small method, `VehicleCategory.depositFor`. Economy and premium share one line there, which also removed the copy-pasted branches. A van with its own deposit rule is one new constant and one new line. For example, if vans take the full deposit from everyone, members included:

```java
public enum VehicleCategory {
    ECONOMY,
    PREMIUM,
    COMMERCIAL_VAN;

    double depositFor(double fullDeposit, RenterType renterType) {
        return switch (this) {
            case ECONOMY, PREMIUM -> renterType == RenterType.MEMBER ? 0 : fullDeposit;
            case COMMERCIAL_VAN -> fullDeposit;
        };
    }
}
```

That is the whole change. `Depot`, `Rental`, `FrontDesk` and `EndOfDayReport` are not touched, and the existing pricing tests keep proving economy and premium still behave. The `switch` has no `default`, so if someone adds `COMMERCIAL_VAN` and forgets its rule, the code does not compile. The original's silent "no branch matched, charge nothing" cannot happen.

In the original, the same change meant editing `Depot.rent`, the one method every rental passes through: adding a third `else if` branch, copying the cost, waiver, status and print lines a third time, and remembering the new `"VAN"` string everywhere a category is compared. Any slip there affects economy and premium too. The brief describes that kind of accident: a tweak to premium pricing that broke how economy returns were handled.

If the van rule turns out to depend on something else (for example the number of days), `depositFor` gains a parameter. That is still a change in one small method, not in `Depot.rent`.

### What changed from the sketch

This is the sketch drawn before the rebuild, from [DIAGNOSIS.md](DIAGNOSIS.md):

![Design sketch: planned classes and how they relate](docs/design-sketch.jpg)

The overall structure in [DIAGNOSIS.md](DIAGNOSIS.md) held. While building I made it simpler in four places and added two things:

- **No `DepositPolicy` interface.** The sketch gave each category a separate policy object. With only one rule today, a `switch` inside `VehicleCategory` does the same job in fewer lines and is checked by the compiler.
- **Lists instead of maps in `Depot`.** The fleet and renters are plain `ArrayList`s. The depot is small, and `contains` already does the exact comparison I need.
- **`returnVehicle` returns `boolean`**, not `Optional<Rental>`: nothing needed the closed rental itself.
- **`EndOfDayReport.print(depot, out)`** prints directly instead of returning a list of lines.
- Added `Checks`, a package-private helper, so validation is written once rather than repeated in each record.
- Added `Depot.renters()`, `rentals()` and `historyOf(renter)`, read-only views that keep features the original had (`Depot.renters`, `getRentals()`, `Renter.history`) without exposing the lists behind them.

## Behaviour preserved, with evidence

The starter's console output was captured before any change and is kept verbatim in [src/test/resources/original-output.txt](src/test/resources/original-output.txt). A `diff` of that file against the rebuilt program's output shows exactly two differences, both the rule now being enforced:

```diff
 Rented Toyota Vitz (GT-1234-20) to Kofi Mensah for 3 days, cost GHS 450.0, deposit GHS 500.0
 Rented Toyota Prado (GR-5678-22) to Ama Owusu for 2 days, cost GHS 1200.0, deposit GHS 0.0
 Returned Toyota Prado (GR-5678-22)
-Rented Toyota Vitz (GT-1234-20) to Ama Owusu for 2 days, cost GHS 300.0, deposit GHS 0.0
+Refused: Toyota Vitz (GT-1234-20) is already rented, so it cannot be rented to Ama Owusu

 === Dovewing Car Rentals - end of day ===
 Fleet status:
   Toyota Vitz (GT-1234-20, ECONOMY): RENTED
   Toyota Prado (GR-5678-22, PREMIUM): AVAILABLE
 Active rentals:
   Rental #1: GT-1234-20 -> Kofi Mensah (3 days)
-  Rental #3: GT-1234-20 -> Ama Owusu (2 days)
```

This comparison is automated. [RentalTrackerTest.java](src/test/java/com/dovewing/rentals/RentalTrackerTest.java) reads `original-output.txt`, applies exactly these two differences, and asserts that the demo day prints the result line for line. It runs whenever the tests run (`mvn test`, or any later Maven phase such as `mvn package`). It does not run on `mvn compile`. GitHub also runs it on every push: see "Automated checks" below.

Things deliberately kept as they were, even where I would question them:

- **Wording and layout of every printed line.** Kept. The text is identical.
- **Cost is the daily rate multiplied by the days, and the deposit is waived for members.** Kept. Same arithmetic, still `double`, so amounts print as `450.0`.
- **Returning a car that is not out still prints `Returned ...`.** Kept. It changes no state. Flagged in the diagnosis for the owner to decide; not mine to change in a rebuild.
- **Rental numbers count up from 1.** Kept. A refused rental no longer uses up a number.
- **Renter history of `"Rented <model>"` entries.** Kept. Now `depot.historyOf(renter)`.

Inputs the original accepted and the rebuild refuses. Each one would otherwise create a state the business could not recognise or reopen the status rule:

- **Renting a vehicle that is already out.** This is the rule the owner said must never be broken.
- **Two vehicles with the same plate.** The status of "GT-1234-20" would be ambiguous.
- **Renting or returning a vehicle or renter the depot has not registered.** The original listed such rentals as active for a car missing from the fleet.
- **A rental of zero or negative days.** The original recorded a negative charge.
- **Money that is negative or not a real number (NaN or infinite).** A rate or deposit like that has no meaning.
- **A name, model or registration that is empty, longer than 100 characters, or contains a line break.** A line break could fake a line in the printed report.
- **An unknown category, renter type or status.** These are now enums; the original silently charged nothing.

## The one improvement beyond the brief: an automated regression suite

**What it is.** 21 JUnit tests in two classes, run by `mvn test`:

- [RentalTrackerTest.java](src/test/java/com/dovewing/rentals/RentalTrackerTest.java) (1 test) compares the whole demo day with the original's recorded output.
- [DepotTest.java](src/test/java/com/dovewing/rentals/domain/DepotTest.java) (20 tests) covers the status rule, the price for each of the two vehicle classes with each of the two renter types, the rentals list, encapsulation (lists cannot be modified, forged renters and vehicles are refused) and input validation.

**Automated checks.** GitHub Actions runs `mvn verify` on JDK 21 on every push to `main` ([.github/workflows/ci.yml](.github/workflows/ci.yml)). The badge at the top of this README shows the result of the latest run and links to its log, so the test count and the output comparison can be checked without cloning the repository.

The summary printed by `mvn test`:

```text
[INFO] Running com.dovewing.rentals.domain.DepotTest
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.dovewing.rentals.RentalTrackerTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Why this, for this business.** The brief says a tweak to premium pricing once broke how economy returns were handled, and that the staff now keep a paper backup because they do not trust the tool. Restructuring makes that kind of break less likely; tests are what make it *noticed*, within seconds, by whoever makes the next change. When the commercial-van class is added next quarter, the person adding it runs one command and knows economy and premium still charge what they did before. A tool that can show it still works after each change is one the staff have a reason to trust again. That is why I chose this over a visible new feature: a new feature would have changed what the tool does, which this lab forbids, and would not have addressed the reason the tool is distrusted.

The original could not have been tested this way, because its rules and its printing were the same lines of code.

## Known limits

- **Money is still a `double`. This is deliberately out of scope.** Moving to `BigDecimal` would change how amounts print (`450.0` today), and this lab forbids changing the original's behaviour. Every rate in the program is a whole number of cedis, so no rounding error occurs today. If rates ever include pesewas, move to `BigDecimal` and agree the print format with the owner first.
- State lives in memory only, as in the original; nothing is saved between runs.

## Commit order

The brief asks for the diagnosis to be committed before the first code change. In this repository:

- `5739e80` "docs: add diagnosis and design sketch" adds `DIAGNOSIS.md` and the sketch.
- `00b86dd` "refactor: rebuild tracker into encapsulated domain and front-desk classes" is the first commit that changes code, and it comes after `5739e80`.

To check, list the commits oldest first:

```bash
git log --oneline --reverse
```

Commit messages use one convention throughout: a type prefix (`chore:`, `docs:`, `refactor:`, `test:`) followed by a short summary.
