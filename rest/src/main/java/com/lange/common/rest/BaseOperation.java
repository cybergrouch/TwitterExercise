package com.lange.common.rest;

import com.google.gson.JsonObject;
import com.lange.common.functional.Pair;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.lange.common.dbc.DesignByContract.requireNotNull;
import static com.lange.common.dbc.DesignByContract.requireShouldNotReachHere;

/**
 * Created by lange on 22/1/16.
 */
public abstract class BaseOperation<T extends BaseOperation> {

    private static final Logger LOGGER = Logger.getLogger(BaseOperation.class.getName());

    protected final IRequest<JsonObject> request;

    protected BaseOperation(IRequest<JsonObject> request) {
        this.request = request;
    }

    public IRequest<JsonObject> getRequest() {
        return this.request;
    }

    public IRequestAdapter.IResponseListener<JsonObject> createCustomListener() {
        return new IRequestAdapter.IResponseListener<JsonObject>() {

            @Override
            public void onSuccess(IResponse<JsonObject> result) {
            }

            @Override
            public void onFailure(RemoteException exception) {
            }
        };
    }

    public void execute(IRequestAdapter.IResponseListener<JsonObject> listener) {
        requireNotNull(listener);
        IRequestAdapter<JsonObject> adapter = HttpClientRequestAdapter.create(getRequest(), HttpClientPool.getInstance().getHttpClient());

        IRequestAdapter.IResponseListener<JsonObject> customListener = createCustomListener();
        IRequestAdapter.IResponseListener<JsonObject> wrapperListener = new IRequestAdapter.IResponseListener<JsonObject>() {
            @Override
            public void onSuccess(IResponse<JsonObject> result) {
                try {
                    customListener.onSuccess(result);
                } finally {
                    listener.onSuccess(result);
                }
            }

            @Override
            public void onFailure(RemoteException exception) {
                try {
                    customListener.onFailure(exception);
                } finally {
                    listener.onFailure(exception);
                }
            }
        };
        adapter.execute(wrapperListener);
    }

    public boolean execute() {
        IRequestAdapter<JsonObject> adapter = HttpClientRequestAdapter.create(getRequest(), HttpClientPool.getInstance().getHttpClient());

        AsynchResponseContainer<IResponse<JsonObject>, RemoteException> container = AsynchResponseContainer.create();
        CountDownLatch latch = new CountDownLatch(1);

        IRequestAdapter.IResponseListener<JsonObject> customListener = createCustomListener();
        IRequestAdapter.IResponseListener<JsonObject> wrapperListener = new IRequestAdapter.IResponseListener<JsonObject>() {
            @Override
            public void onSuccess(IResponse<JsonObject> result) {
                try {
                    container.setSuccessResult(result);
                    customListener.onSuccess(result);
                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void onFailure(RemoteException exception) {
                try {
                    container.setFailureArtifact(exception);
                    customListener.onFailure(exception);
                } finally {
                    latch.countDown();
                }
            }
        };
        adapter.execute(wrapperListener);

        try {
            latch.await();
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Latch timed out", e);
            requireShouldNotReachHere();
        }

        return !container.getContained().get().right.isPresent();
    }

    public Runnable getExecutable(IRequestAdapter.IResponseListener<JsonObject> listener) {
        return new Runnable() {
            @Override
            public void run() {
                BaseOperation.this.execute(listener);
            }
        };
    }

    public Runnable getExecutable() {
        return new Runnable() {
            @Override
            public void run() {
                BaseOperation.this.execute();
            }
        };
    }

    public abstract T cloneOperation();

    public T cloneOperation(Pair<String, String>... parameters) {
        T query = cloneOperation();
        query.getRequest().putParameters(parameters);
        return query;
    }
}
