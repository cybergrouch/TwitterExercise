package com.lange.common.rest;

import com.lange.common.dbc.DesignByContract;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import java.util.logging.Logger;

import static com.lange.common.dbc.DesignByContract.ensureContracts;
import static com.lange.common.dbc.DesignByContract.requireNotNull;

/**
 * Created by lange on 21/1/16.
 */
public class HttpClientPool implements DesignByContract.Ensurable<HttpClientPool> {

    private static final Logger log = Logger.getLogger(HttpClientPool.class.getName());

    private final CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();

    private static class Holder {
        static final HttpClientPool INSTANCE = new HttpClientPool();
    }

    private HttpClientPool() {
        super();
        client.start();
    }

    public static HttpClientPool getInstance() {
        return ensureContracts(Holder.INSTANCE);
    }

    public CloseableHttpAsyncClient getHttpClient() {
        return client;
    }

    @Override
    public HttpClientPool ensurePostConditionContracts() {
        requireNotNull(client);
        return this;
    }
}
