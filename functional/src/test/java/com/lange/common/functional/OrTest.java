package com.lange.common.functional;

import com.lange.common.dbc.DesignByContract;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by lange on 20/1/16.
 */
public class OrTest {

    @Before
    public void setUp() {
        DesignByContract.setEnableDesignByContract(true);
    }

    @Test
    public void testInstantiation() {
        Or<String, Integer> or1 = Or.createLeft("String");
        assertNotNull(or1);
        assertNotNull(or1.left);
        assertTrue(or1.left.isPresent());
        assertNotNull(or1.right);
        assertFalse(or1.right.isPresent());

        Or<String, Integer> or2 = Or.createRight(123);
        assertNotNull(or2);
        assertNotNull(or2.left);
        assertFalse(or2.left.isPresent());
        assertNotNull(or2.right);
        assertTrue(or2.right.isPresent());
    }

    @Test
    public void testInstantiationDBCCheck() {
        try {
            Or<String, Integer> or1 = Or.createLeft(null);
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not null requirement violated", expected.getMessage());
        }

        try {
            Or<String, Integer> or2 = Or.createRight(null);
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not null requirement violated", expected.getMessage());
        }

    }
}
