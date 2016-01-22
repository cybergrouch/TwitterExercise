package com.lange.common.rest;

import com.google.gson.JsonObject;
import com.lange.common.functional.Pair;

import java.util.Optional;
import java.util.logging.Logger;

import static com.lange.common.dbc.DesignByContract.*;

/**
 * Created by lange on 20/1/16.
 */
public final class TokenStore {

    private static final Logger LOGGER = Logger.getLogger(TokenStore.class.getName());

    private TokenStore() {
        super();
    }

    private static class Holder {
        public static final TokenStore INSTANCE = new TokenStore();
    }

    public static TokenStore getInstance() {
        return Holder.INSTANCE;
    }

    private Optional<JsonObject> bearerToken = Optional.empty();
    private Optional<Pair<String, String>> consumerKeyAndSecret = Optional.empty();

    public Optional<String> getBearerToken() {
        if (!bearerToken.isPresent()) {
            return Optional.empty();
        }
        JsonObject bearerTokenJson = bearerToken.get();
        requireNotNull(bearerTokenJson);
        validate(bearerTokenJson,
                x -> "bearer".equals(x.get("token_type").getAsString()) ? Optional.empty() : Optional.of("Token is not of type: bearer"),
                x -> x.has("access_token") ? Optional.empty() : Optional.of("No access_token"),
                x -> !"".equals(x.get("access_token").getAsString().trim()) ? Optional.empty() : Optional.of("Blank access_token"));

        return Optional.ofNullable(bearerTokenJson.get("access_token").getAsString());
    }

    public void setBearerToken(JsonObject bearerTokenJson) {
        requireNotNull(bearerTokenJson);
        validate(bearerTokenJson,
                x -> x.has("token_type") ? Optional.empty() : Optional.of("JsonObject has no token_type attribute"),
                x -> "bearer".equals(x.get("token_type").getAsString()) ? Optional.empty() : Optional.of("Token is not of type: bearer"),
                x -> x.has("access_token") ? Optional.empty() : Optional.of("JsonObject has no access_token attribute"),
                x -> !"".equals(x.get("access_token").getAsString().trim()) ? Optional.empty() : Optional.of("Blank access_token"));

        bearerToken = Optional.of(bearerTokenJson);
    }

    public void clearBearerToken() {
        bearerToken = Optional.empty();
    }

    public Optional<Pair<String, String>> getConsumerKeyAndSecret() {
        return consumerKeyAndSecret;
    }

    public void setConsumerKeyAndSecret(Pair<String, String> consumerKeyAndSecret) {
        requireNotNull(consumerKeyAndSecret);
        requireNotEmpty(consumerKeyAndSecret.left);
        requireNotEmpty(consumerKeyAndSecret.right);
        this.consumerKeyAndSecret = Optional.of(consumerKeyAndSecret);
    }

    public void setConsumerKeyAndSecretFromSystemProperties() {
        String consumerKey = System.getProperty("consumer.key");
        String consumerSecret = System.getProperty("consumer.secret");
        requireNotEmpty(consumerKey);
        requireNotEmpty(consumerSecret);
        setConsumerKeyAndSecret(Pair.create(consumerKey, consumerSecret));

    }

    public void clearConsumerKeyAndSecret() {
        consumerKeyAndSecret = Optional.empty();
    }


}
