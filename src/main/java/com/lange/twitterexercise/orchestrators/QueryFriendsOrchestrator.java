package com.lange.twitterexercise.orchestrators;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lange.common.dbc.DesignByContract;
import com.lange.common.functional.Pair;
import com.lange.common.rest.IRequestAdapter;
import com.lange.common.rest.IResponse;
import com.lange.common.rest.RemoteException;
import com.lange.twitterexercise.events.QueryFriendsEvent;
import com.lange.twitterexercise.models.UserInfo;
import com.lange.twitterexercise.operations.OperationQueue;
import com.lange.twitterexercise.operations.QueryFriends;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.lange.common.dbc.DesignByContract.*;

/**
 * Created by lange on 22/1/16.
 */
public class QueryFriendsOrchestrator implements DesignByContract.Ensurable<QueryFriendsOrchestrator> {

    private static final Logger LOGGER = Logger.getLogger(QueryFriendsOrchestrator.class.getName());

    private final EventBus bus;
    private final String twitterHandle;

    private QueryFriendsOrchestrator(EventBus bus, String twitterHandle) {
        super();
        this.bus = bus;
        this.twitterHandle = twitterHandle;
    }

    public static QueryFriendsOrchestrator create(EventBus bus, String twitterHandle) {
        requireNotNull(bus);
        requireNotEmpty(twitterHandle);
        QueryFriendsOrchestrator orchestrator = new QueryFriendsOrchestrator(bus, twitterHandle);
        return ensureContracts(orchestrator);
    }

    public void queryFriends() {
        QueryFriends queryFriends = QueryFriends.create(twitterHandle);

        IRequestAdapter.IResponseListener<JsonObject> listener = new IRequestAdapter.IResponseListener<JsonObject>() {
            @Override
            public void onSuccess(IResponse<JsonObject> result) {
                LOGGER.log(Level.INFO, "Query Success:\n" + result);
                JsonObject payload = result.getPayload().left.get();

                OperationQueue.enqueue(OperationQueue.PoolType.PROCESSING_POOL, () -> {
                    JsonArray users = payload.getAsJsonArray("users");

                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(UserInfo.class, new UserInfo.Deserializer());
                    final Gson gson = gsonBuilder.create();

                    List<UserInfo> usersList = Lists.newArrayList();
                    users.forEach(x -> {
                        usersList.add(gson.fromJson(x, UserInfo.class));
                    });
                    bus.post(QueryFriendsEvent.createCompleted(usersList));
                });

                String nextCursor = payload.get("next_cursor_str").getAsString();
                if (!"0".equals(nextCursor)) {
                    QueryFriends nextQuery = queryFriends.cloneOperation(Pair.create("cursor", nextCursor));
                    OperationQueue.enqueue(OperationQueue.PoolType.IO_POOL, nextQuery.getExecutable(this));
                } else {
                    LOGGER.log(Level.INFO, "All friends queried");
                }
            }

            @Override
            public void onFailure(RemoteException exception) {
                bus.post(QueryFriendsEvent.createFailed(exception));
            }
        };
        OperationQueue.enqueue(OperationQueue.PoolType.IO_POOL, queryFriends.getExecutable(listener));
    }

    @Override
    public QueryFriendsOrchestrator ensurePostConditionContracts() {
        requireNotEmpty(this.twitterHandle);
        return this;
    }
}
