package com.lange.twitterexercise.models;

import com.google.gson.*;
import com.lange.common.dbc.DesignByContract;

import java.lang.reflect.Type;
import java.util.Optional;

import static com.lange.common.dbc.DesignByContract.*;

public class UserInfo implements DesignByContract.Ensurable<UserInfo> {
    public final String userId;
    public final String screenName;
    public final long tweetCount;

    public static UserInfo create(String userId, String screenName, long tweetCount) {
        requireNotEmpty(userId);
        requireNotEmpty(screenName);
        validate(tweetCount, x -> x >= 0 ? Optional.empty() : Optional.of("Negative tweet count"));
        UserInfo info = new UserInfo(userId, screenName, tweetCount);
        return ensureContracts(info);
    }

    private UserInfo(String userId, String screenName, long tweetCount) {
        this.userId = userId;
        this.screenName = screenName;
        this.tweetCount = tweetCount;
    }

    @Override
    public String toString() {
        return String.format("UserInfo{\n\tuserId: %s,\n\tscreenName: %s,\n\ttweetCount: %s\n}", userId, screenName, tweetCount);
    }

    @Override
    public UserInfo ensurePostConditionContracts() {
        requireNotEmpty(userId);
        requireNotEmpty(screenName);
        validate(tweetCount, x -> x >= 0 ? Optional.empty() : Optional.of("Negative tweet count"));
        return this;
    }

    public static class Deserializer implements JsonDeserializer<UserInfo> {
        @Override
        public UserInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            requireNotNull(json);
            JsonObject jsonObject = json.getAsJsonObject();
            requireNotNull(jsonObject);
            String userId = jsonObject.get("id_str").getAsString();
            String screenName = jsonObject.get("screen_name").getAsString();
            long tweetCount = jsonObject.get("statuses_count").getAsLong();
            return UserInfo.create(userId, screenName, tweetCount);
        }
    }

}