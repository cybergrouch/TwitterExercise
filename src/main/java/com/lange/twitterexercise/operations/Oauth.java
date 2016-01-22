package com.lange.twitterexercise.operations;

import com.google.gson.JsonObject;
import com.lange.common.functional.Pair;
import com.lange.common.rest.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.lange.common.dbc.DesignByContract.*;

/**
 * Created by lange on 21/1/16.
 */
public class Oauth extends BaseOperation<Oauth> implements Ensurable<Oauth> {

    private static final Logger LOGGER = Logger.getLogger(Oauth.class.getName());

    final Pair<String, String> consumerKeyAndSecret;

    private Oauth(Pair<String, String> consumerKeyAndSecret) {
        super(Request.create(IRequest.Method.POST, "https", "api.twitter.com", 443, "/oauth2/token"));
        this.consumerKeyAndSecret = consumerKeyAndSecret;
        getRequest().putHeaders(
                Pair.create("Authorization", String.format("Basic %s", calculateBearerTokenCredentials(consumerKeyAndSecret))),
                Pair.create("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"),
                Pair.create("Connection", "close, TE")
        );
        getRequest().putParameters(
                Pair.create("grant_type", "client_credentials")
        );
    }

    static Optional<String> encodeToUtf8(String rawString) {
        try {
            String encoded = URLEncoder.encode(rawString, "UTF-8");
            requireNotEmpty(encoded);
            return Optional.of(encoded);
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, "Exception during encoding", e);
            requireShouldNotReachHere();
            return Optional.empty();
        }
    }

    static String calculateBearerTokenCredentials(Pair<String, String> consumerKeyAndSecret) {
        requireNotNull(consumerKeyAndSecret);

        // Step 1: Encode consumer key and secret

        // URL encode the consumer key and secret
        Optional<String> urlApiKey = encodeToUtf8(consumerKeyAndSecret.left);
        validate(urlApiKey, x -> x.isPresent() ? Optional.empty() : Optional.of("Encoded urlApiKey is blank or null"));
        Optional<String> urlApiSecret = encodeToUtf8(consumerKeyAndSecret.right);
        validate(urlApiSecret, x -> x.isPresent() ? Optional.empty() : Optional.of("Encoded urlApiKey is blank or null"));

        // Concatenate the encoded consumer key, a colon character, and the
        // encoded consumer secret
        String combined = String.format("%s:%s", urlApiKey.get(), urlApiSecret.get());

        // Base64 encode the string
        String base64Encoded = Base64.getEncoder().encodeToString(combined.getBytes());

        requireNotEmpty(base64Encoded);

        return base64Encoded;
    }

    @Override
    public IRequestAdapter.IResponseListener<JsonObject> createCustomListener() {
        return new IRequestAdapter.IResponseListener<JsonObject>() {
            @Override
            public void onSuccess(IResponse<JsonObject> result) {
                TokenStore.getInstance().setBearerToken(result.getPayload().left.get());
            }

            @Override
            public void onFailure(RemoteException exception) {
                LOGGER.log(Level.SEVERE, "Exception attempting to retrieve OAuth token", exception);
            }
        };
    }

    @Override
    public Oauth cloneOperation() {
        return new Oauth(consumerKeyAndSecret);
    }

    public static Oauth create(Pair<String, String> consumerKeyAndSecret) {
        requireNotNull(consumerKeyAndSecret);
        requireNotEmpty(consumerKeyAndSecret.left);
        requireNotEmpty(consumerKeyAndSecret.right);
        Oauth oauth = new Oauth(consumerKeyAndSecret);
        return ensureContracts(oauth);
    }


    public static Oauth create() {
        Optional<Pair<String, String>> consumerKeyAndSecret = TokenStore.getInstance().getConsumerKeyAndSecret();
        validate(consumerKeyAndSecret, x -> x.isPresent() ? Optional.empty() : Optional.of("Consumer Key and Secret not set in TokenStore"));
        Oauth oauth = new Oauth(consumerKeyAndSecret.get());
        return ensureContracts(oauth);
    }

    @Override
    public Oauth ensurePostConditionContracts() {
        requireNotNull(getRequest());
        requireNotNull(consumerKeyAndSecret);
        requireNotNull(getRequest().getHeaders());
        validate(getRequest().getHeaders(), x -> x.containsKey("Authorization") ? Optional.empty() : Optional.of("Authrization header not set"));
        return this;
    }

}
