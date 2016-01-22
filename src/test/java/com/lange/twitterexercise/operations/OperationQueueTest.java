package com.lange.twitterexercise.operations;

import com.google.gson.JsonObject;
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
 * Created by lange on 22/1/16.
 */
public class OperationQueueTest extends BaseOperationsTest {

    private static final Logger LOGGER = Logger.getLogger(OperationQueueTest.class.getName());

    @Test
    public void testEnqueue() {
        TokenStore.getInstance().clearBearerToken();
        Optional<String> bearerToken = TokenStore.getInstance().getBearerToken();
        assertFalse(bearerToken.isPresent());

        Pair<String, String> consumerKeyAndSecret = Pair.create(System.getProperty("customer.key"), System.getProperty("customer.secret"));
        Oauth oauth = Oauth.create(consumerKeyAndSecret);

        CountDownLatch latch = new CountDownLatch(1);
        AsynchResponseContainer<IResponse<JsonObject>, RemoteException> serverResponseContainer = AsynchResponseContainer.create();

        IRequestAdapter.IResponseListener<JsonObject> listener = new IRequestAdapter.IResponseListener<JsonObject>() {
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
        };

        OperationQueue.enqueue(OperationQueue.PoolType.IO_POOL, oauth.getExecutable(listener));

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
}
