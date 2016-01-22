package com.lange.twitterexercise.events;

import com.lange.common.rest.RemoteException;
import com.lange.twitterexercise.models.UserInfo;

import java.util.List;

import static com.lange.common.dbc.DesignByContract.requireNotNull;

/**
 * Created by lange on 22/1/16.
 */
public interface QueryFriendsEvent {

    class QueryFriendsFailedEvent implements QueryFriendsEvent {
        final RemoteException exception;

        public QueryFriendsFailedEvent(RemoteException exception) {
            this.exception = exception;
        }

        public RemoteException getException() {
            return this.exception;
        }
    }

    class QueryFriendsCompletedEvent implements QueryFriendsEvent {
        final List<UserInfo> friendsList;

        public QueryFriendsCompletedEvent(List<UserInfo> friendsList) {
            this.friendsList = friendsList;
        }

        public List<UserInfo> getFriendsList() {
            return this.friendsList;
        }
    }

    static QueryFriendsCompletedEvent createCompleted(List<UserInfo> friendsList) {
        requireNotNull(friendsList);
        return new QueryFriendsCompletedEvent(friendsList);
    }

    static QueryFriendsFailedEvent createFailed(RemoteException exception) {
        requireNotNull(exception);
        return new QueryFriendsFailedEvent(exception);
    }

}
