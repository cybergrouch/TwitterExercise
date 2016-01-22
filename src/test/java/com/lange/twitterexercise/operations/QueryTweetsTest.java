package com.lange.twitterexercise.operations;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.lange.common.dbc.DesignByContract;
import com.lange.common.functional.Pair;
import com.lange.common.rest.*;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.lange.common.dbc.DesignByContract.validate;
import static org.junit.Assert.*;

/**
 * Created by lange on 21/1/16.
 */
public class QueryTweetsTest extends BaseOperationsTest {

    private static final Logger LOGGER = Logger.getLogger(QueryTweetsTest.class.getName());

    @Test
    public void testInstantiation() {
        QueryTweets query = QueryTweets.create(Arrays.asList("abc", "def"), LocalDate.now());
        assertNotNull(query);
        assertTrue(query.getHandles().contains("abc"));
        assertTrue(query.getHandles().contains("def"));
    }

    @Test
    public void testInstantiationDBCCheck() {
        try {
            QueryTweets.create(null, LocalDate.now());
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not null requirement violated", expected.getMessage());
        }

        try {
            QueryTweets.create(Lists.newArrayList(), LocalDate.now());
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("List of handle is empty", expected.getMessage());
        }

        try {
            QueryTweets.create(Arrays.asList("abc", "def"), null);
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not null requirement violated", expected.getMessage());
        }
    }

    @Test
    public void testExecute() {

        Pair<String, String> consumerKeyAndSecret = Pair.create(System.getProperty("customer.key"), System.getProperty("customer.secret"));

        TokenStore.getInstance().clearBearerToken();
        TokenStore.getInstance().setConsumerKeyAndSecret(consumerKeyAndSecret);

        Oauth oauth = Oauth.create();

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

        QueryTweets query = QueryTweets.create(Arrays.asList("twitterapi", "head_tennis", "AustralianOpen"), LocalDate.now());

        CountDownLatch latch2 = new CountDownLatch(1);
        AsynchResponseContainer<IResponse<JsonObject>, RemoteException> serverResponseContainer2 = AsynchResponseContainer.create();

        query.execute(new IRequestAdapter.IResponseListener<JsonObject>() {
            @Override
            public void onSuccess(IResponse<JsonObject> result) {
                serverResponseContainer2.setSuccessResult(result);
                LOGGER.log(Level.INFO, String.format("Result: %s", result));

                latch2.countDown();
            }

            @Override
            public void onFailure(RemoteException exception) {
                LOGGER.log(Level.SEVERE, "RemoteException occured", exception);
                serverResponseContainer2.setFailureArtifact(exception);
                latch2.countDown();
            }
        });

        try {
            latch2.await();
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Latch timed out", e);
        }

    }
}
