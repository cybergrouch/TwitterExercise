package com.lange.common.dbc;

import org.junit.Test;

import java.util.Optional;
import java.util.function.Function;

import static com.lange.common.dbc.DesignByContract.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by lange on 19/1/16.
 */
public class DesignByContractTest {

    @Test
    public void testRequireNotNull() {
        setEnableDesignByContract(true);
        requireNotNull("abc");

        try {
            requireNotNull(null);
            fail("Did not throw out DesignByContractException for null value");
        } catch (DesignByContract.DesignByContractException e) {
            assertEquals("Not null requirement violated", e.getMessage());
        }

        setEnableDesignByContract(false);
        requireNotNull("abc");

        try {
            requireNotNull(null);
        } catch (DesignByContract.DesignByContractException e) {
            fail("Should not throw out DesignByContractException for null value since DBC is off");
        }
    }

    @Test
    public void testRequireNotEmpty() {
        setEnableDesignByContract(true);
        requireNotEmpty("abc");

        try {
            requireNotEmpty(null);
            fail("Did not throw out DesignByContractException for null String not empty value");
        } catch (DesignByContract.DesignByContractException e) {
            assertEquals("Not empty string requirement violated", e.getMessage());
        }

        try {
            requireNotEmpty("");
            fail("Did not throw out DesignByContractException for blank String not empty value");
        } catch (DesignByContract.DesignByContractException e) {
            assertEquals("Not empty string requirement violated", e.getMessage());
        }

        setEnableDesignByContract(false);
        requireNotNull("abc");

        try {
            requireNotEmpty(null);
        } catch (DesignByContract.DesignByContractException e) {
            fail("Should not throw out DesignByContractException for null String value since DBC is off");
        }

        try {
            requireNotEmpty("");
        } catch (DesignByContract.DesignByContractException e) {
            fail("Should not throw out DesignByContractException for blank String value since DBC is off");
        }
    }

    @Test
    public void testEnsureContracts() {

        Ensurable<Ensurable> ensurable1 = new Ensurable<Ensurable>() {
            @Override
            public Ensurable ensurePostConditionContracts() {
                return this;
            }
        };

        Ensurable<Ensurable> ensurable2 = new Ensurable<Ensurable>() {
            @Override
            public Ensurable ensurePostConditionContracts() {
                requireNotNull(null);
                return this;
            }
        };

        setEnableDesignByContract(true);

        ensureContracts(ensurable1);

        try {
            ensureContracts(null);
            fail("Did not throw out DesignByContractException for null value");
        } catch (DesignByContract.DesignByContractException e) {
            assertEquals("Not null requirement violated", e.getMessage());
        }

        try {
            ensureContracts(ensurable2);
            fail("Did not throw out DesignByContractException for null value");
        } catch (DesignByContract.DesignByContractException e) {
            assertEquals("Not null requirement violated", e.getMessage());
        }

        setEnableDesignByContract(false);

        ensureContracts(ensurable1);

        try {
            ensureContracts(null);
        } catch (DesignByContract.DesignByContractException e) {
            fail("Should not throw out DesignByContractException since DBC is off");
        }

        try {
            ensureContracts(ensurable2);
        } catch (DesignByContract.DesignByContractException e) {
            fail("Should not throw out DesignByContractException since DBC is off");
        }

    }

    @Test
    public void testValidator() {
        setEnableDesignByContract(true);

        Function<Integer, Optional<String>> validator = x -> x > 10 ? Optional.empty() : Optional.of("Value is less than or equal to 10");
        validate(100, validator);

        try {
            validate(null, validator);
            fail("Did not throw out DesignByContractException for value");
        } catch (DesignByContract.DesignByContractException e) {
            assertEquals("Not null requirement violated", e.getMessage());
        }

        try {
            validate(100, null);
            fail("Did not throw out DesignByContractException for value");
        } catch (DesignByContract.DesignByContractException e) {
            assertEquals("Not null requirement violated", e.getMessage());
        }

        try {
            validate(10, validator);
            fail("Did not throw out DesignByContractException for value");
        } catch (DesignByContract.DesignByContractException e) {
            assertEquals("Value is less than or equal to 10", e.getMessage());
        }

        setEnableDesignByContract(false);
        validate(100, validator);

        try {
            validate(null, validator);
        } catch (DesignByContract.DesignByContractException e) {
            fail("Should not throw out DesignByContractException for value since DBC is off");
        }

        try {
            validate(100, null);
        } catch (DesignByContract.DesignByContractException e) {
            fail("Should not throw out DesignByContractException for value since DBC is off");
        }

        try {
            validate(10, validator);
        } catch (DesignByContract.DesignByContractException e) {
            fail("Should not throw out DesignByContractException for value since DBC is off");
        }
    }

    @Test
    public void testRequireShouldNotReachHere() {
        setEnableDesignByContract(true);

        try {
            requireShouldNotReachHere();
            fail("Did not throw out DesignByContractException");
        } catch (DesignByContractException expected) {
            assertEquals("Code line should not be reached", expected.getMessage());
        }

        setEnableDesignByContract(false);

        try {
            requireShouldNotReachHere();
        } catch (DesignByContractException expected) {
            fail("Should not throw out DesignByContractException for value since DBC is off");

        }
    }

    @Test
    public void testFail() {
        setEnableDesignByContract(true);

        try {
            forceFail("ABC");
            fail("Did not throw out DesignByContractException");
        } catch (DesignByContractException expected) {
            assertEquals("Fail: [ABC]", expected.getMessage());
        }

        setEnableDesignByContract(false);

        try {
            forceFail("DEF");
        } catch (DesignByContractException expected) {
            fail("Should not throw out DesignByContractException for value since DBC is off");

        }
    }
}
