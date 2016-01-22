package com.lange.twitterexercise.operations;

import com.google.gson.JsonObject;
import com.lange.common.dbc.DesignByContract;
import com.lange.common.functional.Pair;
import com.lange.common.rest.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.lange.common.dbc.DesignByContract.*;

/**
 * Created by lange on 21/1/16.
 */
public class QueryTweets extends BaseOperation<QueryTweets> implements DesignByContract.Ensurable<QueryTweets> {

    private static final Logger LOGGER = Logger.getLogger(QueryTweets.class.getName());

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final List<String> handles;
    private final LocalDate toDate;

    private QueryTweets(List<String> handles, LocalDate toDate, Optional<String> nextQuery) {
        super(Request.create(IRequest.Method.GET, "https", "api.twitter.com", 443, "/1.1/search/tweets.json"));
        this.handles = handles;
        this.toDate = toDate;

        getRequest().putParameters(
                Pair.create("q", nextQuery.isPresent() ? nextQuery.get() : calculateQuery(handles)),
                Pair.create("until", DATE_FORMAT.format(toDate))
        );
    }

    private QueryTweets(List<String> handles, LocalDate toDate) {
        this(handles, toDate, Optional.empty());
    }

    private String calculateQuery(List<String> handles) {
        String fromHandles = handles.stream().map(x -> String.format("from:%s", x)).collect(Collectors.joining(" OR "));

        return String.format("%s", fromHandles);
    }

    public static QueryTweets create(List<String> handles, LocalDate toDate) {
        requireNotNull(handles);
        requireNotNull(toDate);
        validate(handles, x -> !x.isEmpty() ? Optional.empty() : Optional.of("List of handle is empty"));
        QueryTweets query = new QueryTweets(handles, toDate);
        return ensureContracts(query);
    }

    public List<String> getHandles() {
        return handles;
    }

    @Override
    public QueryTweets ensurePostConditionContracts() {
        requireNotNull(getHandles());
        requireNotNull(toDate);
        validate(getHandles(), x -> !x.isEmpty() ? Optional.empty() : Optional.of("List of handle is empty"));
        return this;
    }

    public QueryTweets cloneOperation(String nextQuery) {
        requireNotEmpty(nextQuery);
        return new QueryTweets(handles, toDate, Optional.of(nextQuery));
    }

    @Override
    public QueryTweets cloneOperation() {
        throw new UnsupportedOperationException("Method not supported. Use QueryTweets.cloneOperation(String).");
    }

    @Override
    public QueryTweets cloneOperation(Pair<String, String>... parameters) {
        throw new UnsupportedOperationException("Method not supported. Use QueryTweets.cloneOperation(String).");
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
