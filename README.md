# Dovewing Car Rentals — Fleet and Rental Tracker

This is the starter repository for the **Java Basics** applied lab. Read the lab
brief alongside this repository; it explains the business, what you are being
asked to deliver, and how your work is assessed.

## What this is

A small program the Dovewing depot uses to track its fleet and its rentals. It works, but it has become risky to change. Your task is to diagnose it
and rebuild it well — **not** to change what it does, but to make it safe to
change and impossible to leave in an invalid state.

## Running it

The program is plain Java (no external dependencies). You need JDK 21.

**With Maven:**

```bash
mvn compile
mvn exec:java -Dexec.mainClass=RentalTracker
```

**Or directly with the JDK:**

```bash
javac -d target/classes src/main/java/RentalTracker.java
java -cp target/classes RentalTracker
```

Run it first, before you change anything, so you can see how it currently
behaves. Look carefully at the end-of-day report.

## What to hand in

See the lab brief for the full list: a written diagnosis, your rebuilt version
(behaving exactly as the original does today), a short note on your key design
decisions, and one improvement beyond the brief. You will talk through your work
in a live review.
