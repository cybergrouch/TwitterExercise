package com.lange.twitterexercise.operations;

import com.google.gson.JsonObject;
import com.lange.common.dbc.DesignByContract;
import com.lange.common.functional.Pair;
import com.lange.common.rest.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.lange.common.dbc.DesignByContract.ensureContracts;
import static com.lange.common.dbc.DesignByContract.requireNotEmpty;

/**
 * Created by lange on 21/1/16.
 */
public class QueryFriends extends BaseOperation<QueryFriends> implements DesignByContract.Ensurable<QueryFriends> {

    private static final Logger LOGGER = Logger.getLogger(QueryFriends.class.getName());

    private final String handle;

    private QueryFriends(String handle) {
        super(Request.create(IRequest.Method.GET, "https", "api.twitter.com", 443, "/1.1/friends/list.json"));
        this.handle = handle;
        getRequest().putParameters(
                Pair.create("screen_name", handle)
        );
    }

    public static QueryFriends create() {
        String handle = System.getProperty("twitter.handle");
        requireNotEmpty(handle);
        return create(handle);
    }

    public static QueryFriends create(String handle) {
        requireNotEmpty(handle);
        QueryFriends query = new QueryFriends(handle);
        return ensureContracts(query);
    }

    public String getHandle() {
        return handle;
    }

    @Override
    public QueryFriends ensurePostConditionContracts() {
        requireNotEmpty(getHandle());
        return this;
    }

    @Override
    public QueryFriends cloneOperation() {
        return new QueryFriends(handle);
    }

    @Override
    public IRequestAdapter.IResponseListener<JsonObject> createCustomListener() {
        return new IRequestAdapter.IResponseListener<JsonObject>() {

            @Override
            public void onSuccess(IResponse<JsonObject> result) {
                LOGGER.log(Level.INFO, "onSuccess");
            }

            @Override
            public void onFailure(RemoteException exception) {
                LOGGER.log(Level.INFO, "onFailure");
            }
        };
    }
}
