package com.lange.common.rest;

import com.lange.common.dbc.DesignByContract;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by lange on 19/1/16.
 */
public class ResponseTest {

    @Before
    public void setUp() {
        DesignByContract.setEnableDesignByContract(true);
    }

    @Test
    public void testInstantiation() {
        IResponse<String> response = Response.create(123, "abcs");
        assertNotNull(response);
        assertEquals(123, response.getStatusCode());
        assertTrue(response.getPayload().left.isPresent());
        assertEquals("abcs", response.getPayload().left.get());
        assertFalse(response.getPayload().right.isPresent());

        IResponse<Integer> response2 = Response.create(234, 1);
        assertNotNull(response2);
        assertEquals(234, response2.getStatusCode());
        assertTrue(response.getPayload().left.isPresent());
        assertEquals((Integer) 1, response2.getPayload().left.get());
        assertFalse(response.getPayload().right.isPresent());

        RemoteException expectedException = RemoteException.create("xxx", new NullPointerException());
        IResponse<String> response3 = Response.create(123, expectedException);
        assertNotNull(response3);
        assertEquals(123, response3.getStatusCode());
        assertFalse(response3.getPayload().left.isPresent());
        assertTrue(response3.getPayload().right.isPresent());
        RemoteException actualException = response3.getPayload().right.get();
        assertNotNull(actualException);
        assertEquals(expectedException, actualException);
    }

    @Test
    public void testInstantiationDBCCheck() {

        try {
            Response.create(123, null);
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not null requirement violated", expected.getMessage());
        }

        try {
            Response.create(-123, "abcde");
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Invalid status code [-123]", expected.getMessage());
        }

        try {
            Response.create(-123, RemoteException.create("abc", new NullPointerException()));
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Invalid status code [-123]", expected.getMessage());
        }

    }
}
