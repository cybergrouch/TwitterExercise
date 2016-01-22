package com.lange.common.rest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lange.common.dbc.DesignByContract;
import com.lange.common.functional.Or;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by lange on 20/1/16.
 */
public class HttpClientRequestAdapterTest {

    @Before
    public void setUp() {
        DesignByContract.setEnableDesignByContract(true);
        TokenStore.getInstance().setBearerToken(createBearerTokenJson());
    }

    private JsonObject createBearerTokenJson() {
        String bearerTokenJsonStr = "{\"token_type\":\"bearer\",\"access_token\":\"0123456789ABCDEF\"}";
        JsonObject bearerToken = new JsonParser().parse(bearerTokenJsonStr).getAsJsonObject();
        assertNotNull(bearerToken);
        return bearerToken;
    }

    @Test
    public void testInstantiation() {
        IRequest<JsonObject> request = Request.create(IRequest.Method.GET, "http", "http://httpbin.org", 80, "/ip");

        IRequestAdapter<JsonObject> adapter = HttpClientRequestAdapter.create(request, HttpClientPool.getInstance().getHttpClient());
        assertNotNull(adapter);
    }

    @Test
    public void testInstantiationDBCCheck() {
        try {
            HttpClientRequestAdapter.create(null, null);
            fail("Did not throw DesignByContractException");
        } catch (Throwable expected) {
            assertTrue(DesignByContract.DesignByContractException.class.isAssignableFrom(expected.getClass()));
            assertEquals("Not null requirement violated", expected.getMessage());
        }
    }

    @Test
    public void testGenerateHttpUriRequest() {
        IRequest<JsonObject> request = Request.create(IRequest.Method.GET, "http", "http://httpbin.org", 80, "/ip");

        HttpClientRequestAdapter adapter = (HttpClientRequestAdapter) HttpClientRequestAdapter.create(request, HttpClientPool.getInstance().getHttpClient());
        assertNotNull(adapter);

        HttpUriRequest httpUriRequest = adapter.generateHttpUriRequest();
        assertThat(httpUriRequest, CoreMatchers.instanceOf(HttpGet.class));

        Header userAgentHeader = httpUriRequest.getFirstHeader("User-Agent");
        assertNotNull(userAgentHeader);
        String userAgentHeaderValue = userAgentHeader.getValue();
        assertNotNull(userAgentHeaderValue);
        assertEquals("Lange Client", userAgentHeaderValue);

        Header tokenHeader = httpUriRequest.getFirstHeader("Authorization");
        assertNotNull(tokenHeader);
        String tokenHeaderValue = tokenHeader.getValue();
        assertNotNull(tokenHeaderValue);
        assertEquals("Bearer 0123456789ABCDEF", tokenHeaderValue);

    }

    @Test
    public void testReadJsonPayload() {

        HttpResponse httpResponseMock = mock(HttpResponse.class);
        HttpEntity httpEntityMock = mock(HttpEntity.class);

        String jsonStr = "{ 'x':'1', 'y':'2' }";
        InputStream inputStream = new ByteArrayInputStream(jsonStr.getBytes());

        when(httpResponseMock.getEntity()).thenReturn(httpEntityMock);
        try {
            doReturn(inputStream).when(httpEntityMock).getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }

        IRequest<JsonObject> request = Request.create(IRequest.Method.GET, "http", "http://httpbin.org", 80, "/ip");

        HttpClientRequestAdapter adapter = (HttpClientRequestAdapter) HttpClientRequestAdapter.create(request, HttpClientPool.getInstance().getHttpClient());
        assertNotNull(adapter);

        Or<JsonObject, RemoteException> jsonObjectOrRemoteException = adapter.readJsonPayload(httpResponseMock);
        assertNotNull(jsonObjectOrRemoteException);
        assertTrue(jsonObjectOrRemoteException.left.isPresent());
        JsonObject json = jsonObjectOrRemoteException.left.get();
        assertNotNull(json);
        assertTrue(json.has("x"));
        assertEquals("1", json.get("x").getAsString());
        assertTrue(json.has("y"));
        assertEquals("2", json.get("y").getAsString());
        assertFalse(jsonObjectOrRemoteException.right.isPresent());
    }

    @Test
    public void testReadJsonPayloadExceptionCase() {

        HttpResponse httpResponseMock = mock(HttpResponse.class);
        HttpEntity httpEntityMock = mock(HttpEntity.class);

        when(httpResponseMock.getEntity()).thenReturn(httpEntityMock);
        try {
            doThrow(new IOException()).when(httpEntityMock).getContent();
        } catch (IOException e) {
            fail();
        }

        IRequest<JsonObject> request = Request.create(IRequest.Method.GET, "http", "http://httpbin.org", 80, "/ip");

        HttpClientRequestAdapter adapter = (HttpClientRequestAdapter) HttpClientRequestAdapter.create(request, HttpClientPool.getInstance().getHttpClient());
        assertNotNull(adapter);

        Or<JsonObject, RemoteException> jsonObjectOrRemoteException = adapter.readJsonPayload(httpResponseMock);
        assertNotNull(jsonObjectOrRemoteException);
        assertFalse(jsonObjectOrRemoteException.left.isPresent());
        assertTrue(jsonObjectOrRemoteException.right.isPresent());
        RemoteException exception = jsonObjectOrRemoteException.right.get();
        assertNotNull(exception);
        assertEquals("Error in parsing the JSON payload", exception.getMessage());
        assertThat(exception.getCause(), CoreMatchers.instanceOf(IOException.class));
    }

    @Test
    public void testConvertToIResponse() {
        HttpResponse httpResponseMock = mock(HttpResponse.class);
        HttpEntity httpEntityMock = mock(HttpEntity.class);
        StatusLine statusLineMock = mock(StatusLine.class);

        String jsonStr = "{ 'x':'1', 'y':'2' }";
        InputStream inputStream = new ByteArrayInputStream(jsonStr.getBytes());

        when(httpResponseMock.getEntity()).thenReturn(httpEntityMock);
        when(httpResponseMock.getStatusLine()).thenReturn(statusLineMock);
        when(statusLineMock.getStatusCode()).thenReturn(202);
        try {
            doReturn(inputStream).when(httpEntityMock).getContent();
        } catch (IOException e) {
            fail();
        }

        IRequest<JsonObject> request = Request.create(IRequest.Method.GET, "http", "http://httpbin.org", 80, "/ip");

        HttpClientRequestAdapter adapter = (HttpClientRequestAdapter) HttpClientRequestAdapter.create(request, HttpClientPool.getInstance().getHttpClient());
        assertNotNull(adapter);

        IResponse<JsonObject> jsonObjectIResponse = adapter.convertToIResponse(httpResponseMock);
        assertNotNull(jsonObjectIResponse);
        assertEquals(202, jsonObjectIResponse.getStatusCode());
        Or<JsonObject, RemoteException> payload = jsonObjectIResponse.getPayload();
        assertNotNull(payload);
        assertTrue(payload.left.isPresent());
        JsonObject jsonObject = payload.left.get();
        assertNotNull(jsonObject);
        assertTrue(jsonObject.has("x"));
        assertEquals("1", jsonObject.get("x").getAsString());
        assertFalse(payload.right.isPresent());
    }

    @Test
    public void testConvertToIResponseExceptionCase() {
        HttpResponse httpResponseMock = mock(HttpResponse.class);
        HttpEntity httpEntityMock = mock(HttpEntity.class);
        StatusLine statusLineMock = mock(StatusLine.class);

        when(httpResponseMock.getEntity()).thenReturn(httpEntityMock);
        when(statusLineMock.getStatusCode()).thenReturn(504);
        try {
            doThrow(new IOException()).when(httpEntityMock).getContent();
        } catch (IOException e) {
            fail();
        }

        IRequest<JsonObject> request = Request.create(IRequest.Method.GET, "http", "http://httpbin.org", 80, "/ip");

        HttpClientRequestAdapter adapter = (HttpClientRequestAdapter) HttpClientRequestAdapter.create(request, HttpClientPool.getInstance().getHttpClient());
        assertNotNull(adapter);

        IResponse<JsonObject> jsonObjectIResponse = adapter.convertToIResponse(httpResponseMock);
        assertNotNull(jsonObjectIResponse);
        assertEquals(400, jsonObjectIResponse.getStatusCode());
        Or<JsonObject, RemoteException> payload = jsonObjectIResponse.getPayload();
        assertNotNull(payload);
        assertFalse(payload.left.isPresent());
        assertTrue(payload.right.isPresent());

        RemoteException remoteExeption = payload.right.get();
        assertNotNull(remoteExeption);
        assertEquals("Error in parsing the JSON payload", remoteExeption.getMessage());
        Throwable cause = remoteExeption.getCause();
        assertNotNull(cause);
        assertThat(cause, CoreMatchers.instanceOf(IOException.class));
    }

    @Test
    public void testGetHttpClient() {
        IRequest<JsonObject> request = Request.create(IRequest.Method.GET, "http", "http://httpbin.org", 80, "/ip");

        HttpClientRequestAdapter adapter = (HttpClientRequestAdapter) HttpClientRequestAdapter.create(request, HttpClientPool.getInstance().getHttpClient());
        assertNotNull(adapter);

        CloseableHttpAsyncClient httpClient = adapter.getHttpClient();
        assertNotNull(httpClient);
    }

    @Test
    public void testExecute() {
        HttpResponse httpResponseMock = mock(HttpResponse.class);
        HttpEntity httpEntityMock = mock(HttpEntity.class);
        StatusLine statusLineMock = mock(StatusLine.class);

        String jsonStr = "{ 'x':'1', 'y':'2' }";
        InputStream inputStream = new ByteArrayInputStream(jsonStr.getBytes());

        when(httpResponseMock.getEntity()).thenReturn(httpEntityMock);
        when(httpResponseMock.getStatusLine()).thenReturn(statusLineMock);
        when(statusLineMock.getStatusCode()).thenReturn(200);
        try {
            doReturn(inputStream).when(httpEntityMock).getContent();
        } catch (IOException e) {
            fail();
        }

        IRequest<JsonObject> request = Request.create(IRequest.Method.GET, "http", "http://httpbin.org", 80, "/ip");

        HttpClientRequestAdapter adapter = spy((HttpClientRequestAdapter) HttpClientRequestAdapter.create(request, HttpClientPool.getInstance().getHttpClient()));
        assertNotNull(adapter);

        CloseableHttpAsyncClient httpClientMock = mock(CloseableHttpAsyncClient.class);
        when(adapter.getHttpClient()).thenReturn(httpClientMock);

        doAnswer(x -> {
            FutureCallback<HttpResponse> callback = (FutureCallback<HttpResponse>) x.getArguments()[1];
            callback.completed(httpResponseMock);
            return null;
        }).when(httpClientMock).execute(any(HttpUriRequest.class), any(FutureCallback.class));


        AsynchResponseContainer<IResponse<JsonObject>, RemoteException> responseContainer = AsynchResponseContainer.create();
        CountDownLatch latch = new CountDownLatch(1);

        adapter.execute(new IRequestAdapter.IResponseListener<JsonObject>() {
            @Override
            public void onSuccess(IResponse<JsonObject> result) {
                responseContainer.setSuccessResult(result);
                latch.countDown();
            }

            @Override
            public void onFailure(RemoteException exception) {
                responseContainer.setFailureArtifact(exception);
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            fail("Latch interrupted");
        }

        Or<IResponse<JsonObject>, RemoteException> serverResponse = responseContainer.getContained().get();
        if (serverResponse.right.isPresent()) {
            fail();
        }

        IResponse<JsonObject> result = serverResponse.left.get();
        assertNotNull(result);
        assertEquals(200, result.getStatusCode());
        Or<JsonObject, RemoteException> payload = result.getPayload();
        assertNotNull(payload);
        assertTrue(payload.left.isPresent());
        JsonObject jsonObject = payload.left.get();
        assertNotNull(jsonObject);
        assertTrue(jsonObject.has("x"));
        assertEquals("1", jsonObject.get("x").getAsString());
        assertFalse(payload.right.isPresent());
    }

    @Test
    public void testExecutionClientFailCase() {

        IRequest<JsonObject> request = Request.create(IRequest.Method.GET, "http", "http://httpbin.org", 80, "/ip");

        HttpClientRequestAdapter adapter = spy((HttpClientRequestAdapter) HttpClientRequestAdapter.create(request, HttpClientPool.getInstance().getHttpClient()));
        assertNotNull(adapter);

        CloseableHttpAsyncClient httpClientMock = mock(CloseableHttpAsyncClient.class);
        when(adapter.getHttpClient()).thenReturn(httpClientMock);

        doAnswer(x -> {
            FutureCallback<IResponse<JsonObject>> callback = (FutureCallback<IResponse<JsonObject>>) x.getArguments()[1];
            callback.failed(new NullPointerException());
            return null;
        }).when(httpClientMock).execute(any(HttpUriRequest.class), any(FutureCallback.class));

        AsynchResponseContainer<IResponse<JsonObject>, RemoteException> responseContainer = AsynchResponseContainer.create();
        CountDownLatch latch = new CountDownLatch(1);

        adapter.execute(new IRequestAdapter.IResponseListener<JsonObject>() {
            @Override
            public void onSuccess(IResponse<JsonObject> result) {
                responseContainer.setSuccessResult(result);
                latch.countDown();
            }

            @Override
            public void onFailure(RemoteException remoteException) {
                responseContainer.setFailureArtifact(remoteException);
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            fail("Latch interrupted");
        }

        Or<IResponse<JsonObject>, RemoteException> serverResponse = responseContainer.getContained().get();
        if (serverResponse.left.isPresent()) {
            fail();
        }

        RemoteException remoteException = serverResponse.right.get();
        assertNotNull(remoteException);
        assertEquals("Execution Failure", remoteException.getMessage());
        Throwable cause = remoteException.getCause();
        assertNotNull(cause);
        assertThat(cause, CoreMatchers.instanceOf(NullPointerException.class));
    }

    @Test
    public void testExecutionResponseParseFailCase() {
        HttpResponse httpResponseMock = mock(HttpResponse.class);
        StatusLine statusLineMock = mock(StatusLine.class);
        when(httpResponseMock.getStatusLine()).thenReturn(statusLineMock);

        IRequest<JsonObject> request = Request.create(IRequest.Method.GET, "http", "http://httpbin.org", 80, "/ip");

        HttpClientRequestAdapter adapter = spy((HttpClientRequestAdapter) HttpClientRequestAdapter.create(request, HttpClientPool.getInstance().getHttpClient()));
        assertNotNull(adapter);

        doReturn(Or.createRight(RemoteException.create("Error in parsing the JSON payload", new NullPointerException())))
                .when(adapter).readJsonPayload(any(HttpResponse.class));

        CloseableHttpAsyncClient httpClientMock = mock(CloseableHttpAsyncClient.class);
        when(adapter.getHttpClient()).thenReturn(httpClientMock);

        doAnswer(x -> {
            FutureCallback<HttpResponse> callback = (FutureCallback<HttpResponse>) x.getArguments()[1];
            callback.completed(httpResponseMock);
            return null;
        }).when(httpClientMock).execute(any(HttpUriRequest.class), any(FutureCallback.class));

        AsynchResponseContainer<IResponse<JsonObject>, RemoteException> responseContainer = AsynchResponseContainer.create();
        CountDownLatch latch = new CountDownLatch(1);

        adapter.execute(new IRequestAdapter.IResponseListener<JsonObject>() {
            @Override
            public void onSuccess(IResponse<JsonObject> result) {
                responseContainer.setSuccessResult(result);
                latch.countDown();
            }

            @Override
            public void onFailure(RemoteException exception) {
                responseContainer.setFailureArtifact(exception);
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            fail("Latch interrupted");
        }

        Or<IResponse<JsonObject>, RemoteException> serverResponse = responseContainer.getContained().get();
        if (serverResponse.left.isPresent()) {
            fail();
        }

        RemoteException remoteException = serverResponse.right.get();
        assertNotNull(remoteException);
        assertEquals("Error in parsing the JSON payload", remoteException.getMessage());
        Throwable cause = remoteException.getCause();
        assertNotNull(cause);
        assertThat(cause, CoreMatchers.instanceOf(NullPointerException.class));
    }

    @Test
    public void testExecutionCancelledCase() {
        HttpResponse httpResponseMock = mock(HttpResponse.class);
        StatusLine statusLineMock = mock(StatusLine.class);
        when(httpResponseMock.getStatusLine()).thenReturn(statusLineMock);

        IRequest<JsonObject> request = Request.create(IRequest.Method.GET, "http", "http://httpbin.org", 80, "/ip");

        HttpClientRequestAdapter adapter = spy((HttpClientRequestAdapter) HttpClientRequestAdapter.create(request, HttpClientPool.getInstance().getHttpClient()));
        assertNotNull(adapter);

        doReturn(Or.createRight(RemoteException.create("Error in parsing the JSON payload", new NullPointerException())))
                .when(adapter).readJsonPayload(any(HttpResponse.class));

        CloseableHttpAsyncClient httpClientMock = mock(CloseableHttpAsyncClient.class);
        when(adapter.getHttpClient()).thenReturn(httpClientMock);

        doAnswer(x -> {
            FutureCallback<HttpResponse> callback = (FutureCallback<HttpResponse>) x.getArguments()[1];
            callback.cancelled();
            return null;
        }).when(httpClientMock).execute(any(HttpUriRequest.class), any(FutureCallback.class));

        AsynchResponseContainer<IResponse<JsonObject>, RemoteException> responseContainer = AsynchResponseContainer.create();
        CountDownLatch latch = new CountDownLatch(1);

        adapter.execute(new IRequestAdapter.IResponseListener<JsonObject>() {
            @Override
            public void onSuccess(IResponse<JsonObject> result) {
                responseContainer.setSuccessResult(result);
                latch.countDown();
            }

            @Override
            public void onFailure(RemoteException exception) {
                responseContainer.setFailureArtifact(exception);
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            fail("Latch interrupted");
        }

        Or<IResponse<JsonObject>, RemoteException> serverResponse = responseContainer.getContained().get();
        if (serverResponse.left.isPresent()) {
            fail();
        }

        RemoteException remoteException = serverResponse.right.get();
        assertNotNull(remoteException);
        assertEquals("Execution Cancelled", remoteException.getMessage());
        Throwable cause = remoteException.getCause();
        assertNull(cause);
    }

}
