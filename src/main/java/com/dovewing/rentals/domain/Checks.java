package com.dovewing.rentals.domain;

/** Input checks shared by the classes in this package. */
final class Checks {

    private static final int MAX_TEXT_LENGTH = 100;

    private Checks() {
    }

    static void requireNotNull(Object value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " must not be null");
        }
    }

    static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be empty");
        }
        if (value.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException(field + " is too long");
        }
        // A hidden line break in a name could fake an extra line in the printed report.
        for (char c : value.toCharArray()) {
            if (Character.isISOControl(c)) {
                throw new IllegalArgumentException(field + " must not contain control characters");
            }
        }
    }

    static void requireAmount(double value, String field) {
        if (value < 0 || Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(field + " must be zero or more, but was " + value);
        }
    }

    static void requireAtLeastOne(int value, String field) {
        if (value < 1) {
            throw new IllegalArgumentException(field + " must be at least 1, but was " + value);
        }
    }
}
