package com.lange.common.dbc;

import java.util.Optional;
import java.util.function.Function;

/**
 * A lightweight DesignByContract library
 * <p>
 * Created by lange on 19/1/16.
 */
public class DesignByContract {

    private static boolean enableDesignByContract = true;

    /**
     * @return true if DesignByContract is turned on, false otherwise.
     */
    public static boolean isEnabled() {
        return enableDesignByContract;
    }

    /**
     * Enables or disables Design By Contract
     *
     * @param enableDesignByContract true to enable DBC checks, false otherwise.
     */
    public static void setEnableDesignByContract(boolean enableDesignByContract) {
        DesignByContract.enableDesignByContract = enableDesignByContract;
    }

    /**
     * Checks that the value object passed is not null
     *
     * @param value the value object to be checked
     * @param <T>   any object
     */
    public static <T> void requireNotNull(T value) {
        if (!isEnabled()) {
            return;
        }
        if (value == null) {
            throw new DesignByContractException("Not null requirement violated");
        }
    }

    /**
     * Validates the value via a validator function instance.
     *
     * @param value      the object instance to be checked
     * @param validators the Function object instance that tests the value object
     * @param <T>        the type of the value object being tested
     */
    public static <T> void validate(T value, Function<T, Optional<String>>... validators) {
        if (!isEnabled()) {
            return;
        }
        requireNotNull(value);
        requireNotNull(validators);
        for (Function<T, Optional<String>> validator : validators) {
            Optional<String> validatorResult = validator.apply(value);
            if (validatorResult.isPresent()) {
                throw new DesignByContractException(validatorResult.orElse(String.format("Some validation failed for object [%s]", value)));
            }
        }
    }

    /**
     * Checks that the String instance passed is not null and blank
     *
     * @param value the value String to be checked
     */
    public static void requireNotEmpty(String value) {
        if (!isEnabled()) {
            return;
        }
        if (value == null || value.trim().isEmpty()) {
            throw new DesignByContractException("Not empty string requirement violated");
        }
    }

    /**
     * Attempts to check the state of the Ensurable object after construction
     *
     * @param ensurable the object to check
     * @param <T>       the castable type of the Ensurable object
     * @return the same Ensurable object casted as a particular type if DBC check is
     * successful (or if DBC is turned off). Otherwise, the DBC checks would throw out
     * DesignByContractException.
     */
    public static <T> T ensureContracts(Ensurable<T> ensurable) {
        if (!isEnabled()) {
            return (T) ensurable;
        }
        requireNotNull(ensurable);
        return ensurable.ensurePostConditionContracts();
    }

    public static void requireShouldNotReachHere() {
        if (!isEnabled()) {
            return;
        }
        throw new DesignByContractException("Code line should not be reached");
    }

    public static void forceFail(String message) {
        if (!isEnabled()) {
            return;
        }
        throw new DesignByContractException(String.format("Fail: [%s]", message));
    }

    public static class DesignByContractException extends RuntimeException {
        public DesignByContractException(String s) {
            super(s);
        }
    }

    /**
     * Ensurable objects are capable of making checks on internal states after object construction.
     *
     * @param <T>
     */
    public static interface Ensurable<T> {

        /**
         * @return the actual instance if the post conditions after object consturction are met.
         * Otherwise, if DesignByContract is on, a DesignByContractException is thrown.
         */
        public T ensurePostConditionContracts();
    }
}
