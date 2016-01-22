package com.lange.common.rest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lange.common.functional.Or;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.lange.common.dbc.DesignByContract.*;

/**
 * Created by lange on 20/1/16.
 */
public class HttpClientRequestAdapter implements IRequestAdapter<JsonObject> {

    private final static Logger LOGGER = Logger.getLogger(HttpClientRequestAdapter.class.getName());

    public final IRequest<JsonObject> request;
    public final CloseableHttpAsyncClient client;

    public HttpClientRequestAdapter(IRequest<JsonObject> request, CloseableHttpAsyncClient client) {
        this.request = request;
        this.client = client;
    }

    public static IRequestAdapter<JsonObject> create(IRequest<JsonObject> request, CloseableHttpAsyncClient client) {
        requireNotNull(request);
        requireNotNull(client);
        IRequestAdapter<JsonObject> handler = new HttpClientRequestAdapter(request, client);
        return ensureContracts(handler);
    }

    @Override
    public IRequestAdapter ensurePostConditionContracts() {
        requireNotNull(request);
        requireNotNull(client);
        return this;
    }

    HttpUriRequest generateHttpUriRequest() {
        HttpUriRequest uriRequest;

        switch (request.getMethod()) {
            case GET:
                uriRequest = new HttpGet(getUri());
                break;
            default:
                uriRequest = new HttpPost(getUri());
                break;
        }
        requireNotNull(uriRequest);

        uriRequest.setHeader("User-Agent", "Lange Client");
        if (TokenStore.getInstance().getBearerToken().isPresent()) {
            uriRequest.setHeader("Authorization", String.format("Bearer %s", TokenStore.getInstance().getBearerToken().get()));
        }
        request.getHeaders()
                .entrySet()
                .stream()
                .forEach(entry -> uriRequest.setHeader(entry.getKey(), entry.getValue()));

        if (request.getMethod() == IRequest.Method.POST) {
            List<NameValuePair> urlParameters =
                    request.getParameters()
                            .stream()
                            .map(x -> new BasicNameValuePair(x.left, x.right))
                            .collect(Collectors.toList());
            try {
                HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);
                ((HttpPost) uriRequest).setEntity(postParams);
            } catch (UnsupportedEncodingException e) {
                LOGGER.log(Level.SEVERE, "Unable to encode POST Entity", e);
                requireShouldNotReachHere();
            }
        }

        return uriRequest;
    }

    IResponse<JsonObject> convertToIResponse(HttpResponse response) {
        Or<JsonObject, RemoteException> payload = readJsonPayload(response);
        requireNotNull(payload);

        IResponse<JsonObject> convertedResponse = payload.right.isPresent()
                ? Response.create(400, payload.right.get())
                : Response.create(response.getStatusLine().getStatusCode(), payload.left.get());
        requireNotNull(convertedResponse);

        return convertedResponse;
    }

    Or<JsonObject, RemoteException> readJsonPayload(HttpResponse response) {
        requireNotNull(response);

        try {
            String json = EntityUtils.toString(response.getEntity(), "UTF-8");
            requireNotEmpty(json);
            JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
            requireNotNull(jsonObject);
            return Or.createLeft(jsonObject);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading HttpResponse payload", e);
            return Or.createRight(RemoteException.create("Error in parsing the JSON payload", e));
        }
    }

    CloseableHttpAsyncClient getHttpClient() {
        return client;
    }

    @Override
    public void execute(IResponseListener<JsonObject> listener) {

        HttpUriRequest request = generateHttpUriRequest();
        getHttpClient().execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.log(Level.INFO, String.format("Response: [%s]", result.getStatusLine()));
                }
                IResponse<JsonObject> jsonObjectIResponse = convertToIResponse(result);
                if (jsonObjectIResponse.getPayload().right.isPresent()) {
                    listener.onFailure(jsonObjectIResponse.getPayload().right.get());
                } else if (jsonObjectIResponse.getStatusCode() != 200) {
                    listener.onFailure(RemoteException.create(String.format("Server returned [%s]", jsonObjectIResponse.getStatusCode())));
                } else {
                    listener.onSuccess(jsonObjectIResponse);
                }
            }

            @Override
            public void failed(Exception ex) {
                listener.onFailure(RemoteException.create("Execution Failure", ex));
            }

            @Override
            public void cancelled() {
                listener.onFailure(RemoteException.create("Execution Cancelled"));
            }
        });
    }

    String getUri() {

        String basicUri = String.format("%s://%s:%s%s", request.getProtocol(), request.getBaseUrl(), request.getPort(), request.getEndPoint());

        if (IRequest.Method.POST == request.getMethod()) {
            return basicUri;
        }

        if (IRequest.Method.GET == request.getMethod()) {
            List<NameValuePair> urlParameters =
                    request.getParameters()
                            .stream()
                            .map(x -> new BasicNameValuePair(x.left, x.right))
                            .collect(Collectors.toList());

            String paramString = URLEncodedUtils.format(urlParameters, "utf-8");
            requireNotNull(paramString);
            if ("".equals(paramString)) {
                return basicUri;
            }
            return String.format("%s?%s", basicUri, paramString);
        }

        requireShouldNotReachHere();
        return null;
    }
}
