package com.lange.twitterexercise.models;

import com.google.gson.*;
import com.lange.common.dbc.DesignByContract;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.lange.common.dbc.DesignByContract.*;

/**
 * Created by lange on 22/1/16.
 */
public class Tweet implements DesignByContract.Ensurable<Tweet> {
    public final LocalDate date;
    public final String userId;
    public final long count;

    public static final DateTimeFormatter TWITTER_TWEET_DATE_FORMAT = DateTimeFormatter.ofPattern("EEE MMM dd kk:mm:ss Z yyyy");

    private Tweet(LocalDate date, String userId, long count) {
        this.date = date;
        this.userId = userId;
        this.count = count;
    }

    public static Tweet create(LocalDate date, String userId, long count) {
        requireNotNull(date);
        requireNotEmpty(userId);
        validate(count, x -> x > 0 ? Optional.empty() : Optional.of("Count is not positive."));
        return ensureContracts(new Tweet(date, userId, count));
    }

    public LocalDate getDate() {
        return date;
    }

    public String getUserId() {
        return userId;
    }

    public long getCount() {
        return count;
    }

    @Override
    public Tweet ensurePostConditionContracts() {
        requireNotNull(getDate());
        requireNotEmpty(getUserId());
        validate(getCount(), x -> x > 0 ? Optional.empty() : Optional.of("Count is not positive."));
        return this;
    }

    public static class Deserializer implements JsonDeserializer<Tweet> {
        @Override
        public Tweet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            requireNotNull(json);
            JsonObject jsonObject = json.getAsJsonObject();
            requireNotNull(jsonObject);

            String jsonDate = jsonObject.get("created_at").getAsString();
            LocalDate date = LocalDate.parse(jsonDate, TWITTER_TWEET_DATE_FORMAT);
            requireNotNull(date);

            String userId = jsonObject.get("user").getAsJsonObject().get("id_str").getAsString();

            return Tweet.create(date, userId, 1);
        }
    }
}
