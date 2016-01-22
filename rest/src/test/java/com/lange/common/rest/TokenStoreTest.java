package com.lange.common.rest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lange.common.dbc.DesignByContract;
import com.lange.common.functional.Pair;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by lange on 20/1/16.
 */
public class TokenStoreTest {

    @Before
    public void setUp() {
        DesignByContract.setEnableDesignByContract(true);
        TokenStore.getInstance().clearBearerToken();
        TokenStore.getInstance().clearConsumerKeyAndSecret();
    }

    @Test
    public void testInstantiation() {
        TokenStore store1 = TokenStore.getInstance();
        assertNotNull(store1);

        TokenStore store2 = TokenStore.getInstance();
        assertNotNull(store2);

        assertTrue(store1 == store2);
    }

    private JsonObject createBearerTokenJson() {
        String bearerTokenJsonStr = "{\"token_type\":\"bearer\",\"access_token\":\"0123456789ABCDEF\"}";
        JsonObject bearerToken = new JsonParser().parse(bearerTokenJsonStr).getAsJsonObject();
        assertNotNull(bearerToken);
        return bearerToken;
    }

    private JsonObject createBearerTokenNoTokenTypeJson() {
        String bearerTokenJsonStr = "{\"access_token\":\"0123456789ABCDEF\"}";
        JsonObject bearerToken = new JsonParser().parse(bearerTokenJsonStr).getAsJsonObject();
        assertNotNull(bearerToken);
        return bearerToken;
    }

    private JsonObject createBearerTokenBlankTokenTypeJson() {
        String bearerTokenJsonStr = "{\"token_type\":\"\",\"access_token\":\"0123456789ABCDEF\"}";
        JsonObject bearerToken = new JsonParser().parse(bearerTokenJsonStr).getAsJsonObject();
        assertNotNull(bearerToken);
        return bearerToken;
    }

    private JsonObject createBearerTokenNoAccessTokenJson() {
        String bearerTokenJsonStr = "{\"token_type\":\"bearer\"}";
        JsonObject bearerToken = new JsonParser().parse(bearerTokenJsonStr).getAsJsonObject();
        assertNotNull(bearerToken);
        return bearerToken;
    }

    private JsonObject createBearerTokenBlankAccessTokenJson() {
        String bearerTokenJsonStr = "{\"token_type\":\"bearer\",\"access_token\":\"\"}";
        JsonObject bearerToken = new JsonParser().parse(bearerTokenJsonStr).getAsJsonObject();
        assertNotNull(bearerToken);
        return bearerToken;
    }

    @Test
    public void testSetBearerToken() {
        TokenStore store = TokenStore.getInstance();
        assertFalse(store.getBearerToken().isPresent());
        store.setBearerToken(createBearerTokenJson());
        assertTrue(store.getBearerToken().isPresent());
        assertEquals("0123456789ABCDEF", store.getBearerToken().get());
    }

    @Test
    public void testSetBearerTokenDBCCheck() {
        TokenStore store = TokenStore.getInstance();
        assertFalse(store.getBearerToken().isPresent());

        try {
            store.setBearerToken(createBearerTokenNoTokenTypeJson());
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("JsonObject has no token_type attribute", expected.getMessage());
        }

        try {
            store.setBearerToken(createBearerTokenBlankTokenTypeJson());
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Token is not of type: bearer", expected.getMessage());
        }

        try {
            store.setBearerToken(createBearerTokenNoAccessTokenJson());
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("JsonObject has no access_token attribute", expected.getMessage());
        }

        try {
            store.setBearerToken(createBearerTokenBlankAccessTokenJson());
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Blank access_token", expected.getMessage());
        }
    }

    @Test
    public void testClearBearerToken() {
        TokenStore store = TokenStore.getInstance();
        assertFalse(store.getBearerToken().isPresent());
        store.setBearerToken(createBearerTokenJson());
        assertTrue(store.getBearerToken().isPresent());
        assertEquals("0123456789ABCDEF", store.getBearerToken().get());

        store.clearBearerToken();
        assertFalse(store.getBearerToken().isPresent());
    }

    @Test
    public void testSetConsumerKeyAndSecret() {

        assertFalse(TokenStore.getInstance().getConsumerKeyAndSecret().isPresent());

        Pair<String, String> consumerKeyAndSecret = Pair.create("abc", "def");
        TokenStore.getInstance().setConsumerKeyAndSecret(consumerKeyAndSecret);

        assertTrue(TokenStore.getInstance().getConsumerKeyAndSecret().isPresent());

        assertEquals(consumerKeyAndSecret, TokenStore.getInstance().getConsumerKeyAndSecret().get());

        TokenStore.getInstance().clearConsumerKeyAndSecret();
        assertFalse(TokenStore.getInstance().getConsumerKeyAndSecret().isPresent());


    }

}
