package com.lange.twitterexercise.orchestrators;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lange.common.dbc.DesignByContract;
import com.lange.common.rest.IRequestAdapter;
import com.lange.common.rest.IResponse;
import com.lange.common.rest.RemoteException;
import com.lange.twitterexercise.events.QueryTweetsEvent;
import com.lange.twitterexercise.models.Tweet;
import com.lange.twitterexercise.models.UserInfo;
import com.lange.twitterexercise.operations.OperationQueue;
import com.lange.twitterexercise.operations.QueryTweets;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.lange.common.dbc.DesignByContract.*;

/**
 * Created by lange on 22/1/16.
 */
public class QueryTweetsOrchestrator implements DesignByContract.Ensurable<QueryTweetsOrchestrator> {

    private static final Logger LOGGER = Logger.getLogger(QueryFriendsOrchestrator.class.getName());

    private final EventBus bus;
    private LocalDate toDate;

    private QueryTweetsOrchestrator(EventBus bus, LocalDate toDate) {
        super();
        this.bus = bus;
        this.toDate = toDate;
    }

    public static QueryTweetsOrchestrator create(EventBus bus, LocalDate toDate) {
        requireNotNull(bus);
        requireNotNull(toDate);
        QueryTweetsOrchestrator orchestrator = new QueryTweetsOrchestrator(bus, toDate);
        return ensureContracts(orchestrator);
    }

    public void queryTweets(List<UserInfo> friendsList) {
        validate(friendsList, x -> !x.isEmpty() ? Optional.empty() : Optional.of("Twitter Handles passed is empty"));

        List<List<String>> handlerGroup = groupFriends(friendsList);

        handlerGroup.stream().forEach(x -> {
            QueryTweets queryTweets = QueryTweets.create(x, toDate);
            IRequestAdapter.IResponseListener<JsonObject> listener = new IRequestAdapter.IResponseListener<JsonObject>() {
                @Override
                public void onSuccess(IResponse<JsonObject> result) {
                    LOGGER.log(Level.INFO, "Query Success:\n" + result);
                    JsonObject payload = result.getPayload().left.get();

                    OperationQueue.enqueue(OperationQueue.PoolType.PROCESSING_POOL, () -> {
                        JsonArray users = payload.getAsJsonArray("statuses");

                        GsonBuilder gsonBuilder = new GsonBuilder();
                        gsonBuilder.registerTypeAdapter(Tweet.class, new Tweet.Deserializer());
                        final Gson gson = gsonBuilder.create();

                        List<Tweet> tweetsList = Lists.newArrayList();
                        users.forEach(x -> {
                            tweetsList.add(gson.fromJson(x, Tweet.class));
                        });
                        bus.post(QueryTweetsEvent.createCompleted(tweetsList));
                    });

                    if (payload.has("next_results")) {
                        QueryTweets nextQuery = queryTweets.cloneOperation(payload.get("next_results").getAsString());
                        OperationQueue.enqueue(OperationQueue.PoolType.IO_POOL, nextQuery.getExecutable(this));
                    } else {
                        LOGGER.log(Level.INFO, "All tweets queried from batch");
                    }
                }

                @Override
                public void onFailure(RemoteException exception) {
                    bus.post(QueryTweetsEvent.createFailed(exception));
                }
            };
            OperationQueue.enqueue(OperationQueue.PoolType.IO_POOL, queryTweets.getExecutable(listener));
        });
    }

    private List<List<String>> groupFriends(List<UserInfo> friendsList) {
        List<List<String>> grouped = Lists.newArrayList();

        List<String> group = Lists.newArrayList();
        int size = 0;
        for (UserInfo userInfo : friendsList) {
            if (size >= 475) {
                grouped.add(group);
                group = Lists.newArrayList();
            }
            group.add(userInfo.screenName);
            size += 9 + userInfo.screenName.length();
        }

        if (group.size() > 0) {
            grouped.add(group);
            group = Lists.newArrayList();
            size = 0;
        }

        return grouped;
    }

    @Override
    public QueryTweetsOrchestrator ensurePostConditionContracts() {
        requireNotNull(this.bus);
        requireNotNull(this.toDate);
        return this;
    }
}
