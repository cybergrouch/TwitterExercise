package com.lange.twitterexercise.operations;

import com.google.gson.JsonObject;
import com.lange.common.dbc.DesignByContract;
import com.lange.common.functional.Or;
import com.lange.common.functional.Pair;
import com.lange.common.rest.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.lange.common.dbc.DesignByContract.validate;
import static org.junit.Assert.*;

/**
 * Created by lange on 21/1/16.
 */
public class OauthTest extends BaseOperationsTest {

    private static final Logger LOGGER = Logger.getLogger(OauthTest.class.getName());

    @Test
    public void testInstantiation() {
        Oauth oauth = Oauth.create(Pair.create("xvz1evFS4wEEPTGEFPHBog", "L8qq9PZyRg6ieKGEKhZolGC0vJWLw8iEJ88DRdyOg"));
        assertNotNull(oauth);

        assertTrue(oauth.getRequest().getHeaders().containsKey("Authorization"));
        assertEquals("Basic eHZ6MWV2RlM0d0VFUFRHRUZQSEJvZzpMOHFxOVBaeVJnNmllS0dFS2hab2xHQzB2SldMdzhpRUo4OERSZHlPZw==", oauth.getRequest().getHeaders().get("Authorization"));

        assertTrue(oauth.getRequest().getHeaders().containsKey("Content-Type"));
        assertEquals("application/x-www-form-urlencoded;charset=UTF-8", oauth.getRequest().getHeaders().get("Content-Type"));

        Optional<Pair<String, String>> contentTypeParameter = oauth.getRequest().getParameterByKey("grant_type");
        assertTrue(contentTypeParameter.isPresent());
        assertEquals("grant_type", contentTypeParameter.get().left);
        assertEquals("client_credentials", contentTypeParameter.get().right);

        TokenStore.getInstance().setConsumerKeyAndSecret(Pair.create("xvz1evFS4wEEPTGEFPHBog", "L8qq9PZyRg6ieKGEKhZolGC0vJWLw8iEJ88DRdyOg"));
        Oauth oauth2 = Oauth.create();

        assertTrue(oauth2.getRequest().getHeaders().containsKey("Authorization"));
        assertEquals("Basic eHZ6MWV2RlM0d0VFUFRHRUZQSEJvZzpMOHFxOVBaeVJnNmllS0dFS2hab2xHQzB2SldMdzhpRUo4OERSZHlPZw==", oauth.getRequest().getHeaders().get("Authorization"));

        assertTrue(oauth2.getRequest().getHeaders().containsKey("Content-Type"));
        assertEquals("application/x-www-form-urlencoded;charset=UTF-8", oauth.getRequest().getHeaders().get("Content-Type"));

        Optional<Pair<String, String>> contentTypeParameter2 = oauth.getRequest().getParameterByKey("grant_type");
        assertTrue(contentTypeParameter2.isPresent());
        assertEquals("grant_type", contentTypeParameter2.get().left);
        assertEquals("client_credentials", contentTypeParameter2.get().right);
    }

    @Test
    public void testInstantiationDBCCheck() {
        try {
            Oauth.create(null);
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not null requirement violated", expected.getMessage());
        }

        try {
            Oauth.create(Pair.create("", "abc"));
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not empty string requirement violated", expected.getMessage());
        }

        try {
            Oauth.create(Pair.create("abc", ""));
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not empty string requirement violated", expected.getMessage());
        }

        TokenStore.getInstance().clearConsumerKeyAndSecret();
        try {
            Oauth.create();
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Consumer Key and Secret not set in TokenStore", expected.getMessage());
        }
    }

    @Test
    public void testExecute() {
        TokenStore.getInstance().clearBearerToken();
        Optional<String> bearerToken = TokenStore.getInstance().getBearerToken();
        assertFalse(bearerToken.isPresent());

        Pair<String, String> consumerKeyAndSecret = Pair.create(System.getProperty("customer.key"), System.getProperty("customer.secret"));
        Oauth oauth = Oauth.create(consumerKeyAndSecret);

        oauth.execute();

        Optional<String> bearerToken2 = TokenStore.getInstance().getBearerToken();
        assertTrue(bearerToken2.isPresent());
    }

    @Test
    public void testExecuteWithListener() {

        TokenStore.getInstance().clearBearerToken();
        Optional<String> bearerToken = TokenStore.getInstance().getBearerToken();
        assertFalse(bearerToken.isPresent());

        Pair<String, String> consumerKeyAndSecret = Pair.create(System.getProperty("customer.key"), System.getProperty("customer.secret"));
        Oauth oauth = Oauth.create(consumerKeyAndSecret);

        CountDownLatch latch = new CountDownLatch(1);
        AsynchResponseContainer<IResponse<JsonObject>, RemoteException> serverResponseContainer = AsynchResponseContainer.create();

        oauth.execute(new IRequestAdapter.IResponseListener<JsonObject>() {
            @Override
            public void onSuccess(IResponse<JsonObject> result) {
                serverResponseContainer.setSuccessResult(result);
                latch.countDown();
            }

            @Override
            public void onFailure(RemoteException exception) {
                LOGGER.log(Level.SEVERE, "RemoteException occured", exception);
                serverResponseContainer.setFailureArtifact(exception);
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Latch timed out", e);
        }

        validate(serverResponseContainer.getContained(),
                x -> x.isPresent()
                        ? Optional.empty()
                        : Optional.of("Server Response is not available"));
        validate(serverResponseContainer.getContained().get().right,
                x -> !x.isPresent()
                        ? Optional.empty()
                        : Optional.of("Oauth resulted in exception"));

        IResponse<JsonObject> result = serverResponseContainer.getContained().get().left.get();

        assertNotNull(result);
        assertEquals(200, result.getStatusCode());
        Or<JsonObject, RemoteException> payload = result.getPayload();
        assertNotNull(payload);
        assertTrue(payload.left.isPresent());
        assertFalse(payload.right.isPresent());
        LOGGER.log(Level.INFO, payload.left.get().toString());

        Optional<String> bearerToken2 = TokenStore.getInstance().getBearerToken();
        assertTrue(bearerToken2.isPresent());
    }

    @Test
    public void testCalculateBearerTokenCredentials() {
        Pair<String, String> consumerKeyAndSecret = Pair.create("GioGiobU3KUXmhfhUNfb0Nfb0", "qpNFIPjqpNFgeSDpJViNDpJViahN597wVN597tJJAKCGnKCGnD");
        String bearerTokenCredentials = Oauth.calculateBearerTokenCredentials(consumerKeyAndSecret);
        assertNotNull(bearerTokenCredentials);
        assertEquals("R2lvR2lvYlUzS1VYbWhmaFVOZmIwTmZiMDpxcE5GSVBqcXBORmdlU0RwSlZpTkRwSlZpYWhONTk3d1ZONTk3dEpKQUtDR25LQ0duRA==", bearerTokenCredentials);
    }


}
