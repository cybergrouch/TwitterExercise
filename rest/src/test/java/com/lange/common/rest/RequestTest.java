package com.lange.common.rest;

import com.lange.common.dbc.DesignByContract;
import com.lange.common.functional.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;


/**
 * Created by lange on 19/1/16.
 */
public class RequestTest {

    @Before
    public void setUp() {
        DesignByContract.setEnableDesignByContract(true);
    }

    @Test
    public void testInstantiation() {
        IRequest request = Request.create(IRequest.Method.GET, "https", "www.google.com", 80, "/abc");
        assertNotNull(request);

        assertEquals("www.google.com", request.getBaseUrl());
        assertEquals("/abc", request.getEndPoint());
        assertEquals("https", request.getProtocol());
        assertEquals(IRequest.Method.GET, request.getMethod());
    }

    @Test
    public void testInstantiationDBCCheck() {
        try {
            Request.create(null, "https", "www.google.com", 80, "/abc");
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not null requirement violated", expected.getMessage());
        }

        try {
            Request.create(IRequest.Method.GET, null, "www.google.com", 80, "/abc");
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not empty string requirement violated", expected.getMessage());
        }

        try {
            Request.create(IRequest.Method.GET, "", "www.google.com", 80, "/abc");
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not empty string requirement violated", expected.getMessage());
        }

        try {
            Request.create(IRequest.Method.GET, "http", null, 80, "/abc");
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not empty string requirement violated", expected.getMessage());
        }

        try {
            Request.create(IRequest.Method.GET, "http", "", 80, "/abc");
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not empty string requirement violated", expected.getMessage());
        }

        try {
            Request.create(IRequest.Method.GET, "https", "www.google.com", -123, "/abc");
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Invalid port [-123]", expected.getMessage());
        }

        try {
            Request.create(IRequest.Method.GET, "http", "www.google.com", 80, null);
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not empty string requirement violated", expected.getMessage());
        }

        try {
            Request.create(IRequest.Method.GET, "http", "www.google.com", 80, "");
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not empty string requirement violated", expected.getMessage());
        }

    }

    @Test
    public void testHeaders() {
        IRequest request = Request.create(IRequest.Method.GET, "https", "www.google.com", 80, "/abc");
        Map<String, String> headers = request.getHeaders();
        assertNotNull(headers);
        request.putHeaders(Pair.create("header1", "header1Value"), Pair.create("header2", "header2Value"));
        assertFalse(headers.containsKey("header1"));

        headers = request.getHeaders();
        assertTrue(headers.containsKey("header1"));
        assertEquals("header1Value", headers.get("header1"));
        assertTrue(headers.containsKey("header2"));
        assertEquals("header2Value", headers.get("header2"));

        request.putHeaders(Pair.create("header1", "header1ValueB"));
        assertTrue(headers.containsKey("header1"));
        assertEquals("header1Value", headers.get("header1"));
        assertTrue(headers.containsKey("header2"));
        assertEquals("header2Value", headers.get("header2"));

        headers = request.getHeaders();
        assertTrue(headers.containsKey("header1"));
        assertEquals("header1ValueB", headers.get("header1"));
        assertTrue(headers.containsKey("header2"));
        assertEquals("header2Value", headers.get("header2"));

        request.clearHeaders();
        assertTrue(headers.containsKey("header1"));
        assertEquals("header1ValueB", headers.get("header1"));
        assertTrue(headers.containsKey("header2"));
        assertEquals("header2Value", headers.get("header2"));

        headers = request.getHeaders();
        assertFalse(headers.containsKey("header1"));
        assertFalse(headers.containsKey("header2"));
    }

    @Test
    public void testParameters() {
        IRequest request = Request.create(IRequest.Method.GET, "https", "www.google.com", 80, "/abc");
        List<Pair<String, String>> parameters = request.getParameters();
        assertNotNull(parameters);

        Pair<String, String> pair1 = Pair.create("header1", "header1Value");
        Pair<String, String> pair2 = Pair.create("header2", "header2Value");
        Pair<String, String> pair1b = Pair.create("header1", "header1ValueB");

        request.putParameters(pair1, pair2);
        assertFalse(parameters.contains(pair1));
        assertFalse(parameters.contains(pair2));


        parameters = request.getParameters();
        assertTrue(parameters.contains(pair1));
        assertTrue(parameters.contains(pair2));

        request.putParameters(pair1b);
        assertTrue(parameters.contains(pair1));
        assertTrue(parameters.contains(pair2));
        assertFalse(parameters.contains(pair1b));

        parameters = request.getParameters();
        assertFalse(parameters.contains(pair1));
        assertTrue(parameters.contains(pair2));
        assertTrue(parameters.contains(pair1b));

        request.clearParameters();
        assertFalse(parameters.contains(pair1));
        assertTrue(parameters.contains(pair2));
        assertTrue(parameters.contains(pair1b));

        parameters = request.getParameters();
        assertFalse(parameters.contains(pair1));
        assertFalse(parameters.contains(pair2));
        assertFalse(parameters.contains(pair1b));
    }
}
