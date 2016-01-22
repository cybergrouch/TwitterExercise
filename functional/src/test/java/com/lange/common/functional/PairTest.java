package com.lange.common.functional;

import com.lange.common.dbc.DesignByContract;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by lange on 19/1/16.
 */
public class PairTest {

    @Before
    public void setUp() {
        DesignByContract.setEnableDesignByContract(true);
    }

    @Test
    public void testInstantiation() {
        Pair<String, String> pair1 = Pair.create("abc", "def");
        assertNotNull(pair1);

        assertEquals("abc", pair1.left);
        assertEquals("def", pair1.right);
    }

    @Test
    public void testInstantiationDBCCheck() {
        try {
            Pair.create(null, "def");
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not null requirement violated", expected.getMessage());
        }

        try {
            Pair.create("abc", null);
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not null requirement violated", expected.getMessage());
        }


        try {
            Pair.create(null, null);
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not null requirement violated", expected.getMessage());
        }
    }

    @Test
    public void testEquals() {
        Pair<String, String> pair1 = Pair.create("abc", "def");
        Pair<String, String> pair2 = Pair.create("abc", "def");

        assertFalse(pair1 == pair2);
        assertEquals(pair1, pair2);
    }
}
