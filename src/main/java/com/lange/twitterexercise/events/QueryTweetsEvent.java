package com.lange.twitterexercise.events;

import com.lange.common.rest.RemoteException;
import com.lange.twitterexercise.models.Tweet;

import java.util.List;

import static com.lange.common.dbc.DesignByContract.requireNotNull;

/**
 * Created by lange on 22/1/16.
 */
public interface QueryTweetsEvent {

    class QueryTweetsFailedEvent implements QueryFriendsEvent {
        final RemoteException exception;

        public QueryTweetsFailedEvent(RemoteException exception) {
            this.exception = exception;
        }

        public RemoteException getException() {
            return this.exception;
        }
    }

    class QueryTweetsCompletedEvent implements QueryTweetsEvent {
        public final List<Tweet> tweetsList;

        public QueryTweetsCompletedEvent(List<Tweet> tweetsList) {
            this.tweetsList = tweetsList;
        }

    }

    static QueryTweetsCompletedEvent createCompleted(List<Tweet> tweetsList) {
        requireNotNull(tweetsList);
        return new QueryTweetsCompletedEvent(tweetsList);
    }

    static QueryTweetsFailedEvent createFailed(RemoteException exception) {
        requireNotNull(exception);
        return new QueryTweetsFailedEvent(exception);
    }
}
