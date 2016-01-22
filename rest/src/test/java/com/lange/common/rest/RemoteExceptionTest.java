package com.lange.common.rest;

import com.lange.common.dbc.DesignByContract;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by lange on 20/1/16.
 */
public class RemoteExceptionTest {

    @Before
    public void setUp() {
        DesignByContract.setEnableDesignByContract(true);
    }

    @Test
    public void testInstantiation() {
        Exception cause = new RuntimeException();
        RemoteException exception = RemoteException.create("abc", cause);
        assertNotNull(exception);
        assertEquals("abc", exception.getMessage());
        assertEquals(cause, exception.getCause());

        RemoteException exception2 = RemoteException.create("abc");
        assertNotNull(exception2);
        assertEquals("abc", exception.getMessage());

    }

    @Test
    public void testInstantiationDBCCheck() {
        Exception cause = new RuntimeException();

        try {
            RemoteException.create("abc", null);
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not null requirement violated", expected.getMessage());
        }

        try {
            RemoteException.create(null, cause);
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not empty string requirement violated", expected.getMessage());
        }

        try {
            RemoteException.create("", cause);
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not empty string requirement violated", expected.getMessage());
        }

        try {
            RemoteException.create(null);
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not empty string requirement violated", expected.getMessage());
        }

        try {
            RemoteException.create("");
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not empty string requirement violated", expected.getMessage());
        }
    }
}
