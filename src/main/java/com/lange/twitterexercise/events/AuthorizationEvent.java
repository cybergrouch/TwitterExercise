package com.lange.twitterexercise.events;

/**
 * Created by lange on 22/1/16.
 */
public interface AuthorizationEvent {

    class AuthorizationSuccessEvent implements AuthorizationEvent {

    }

    class AuthorizationFailedEvent implements AuthorizationEvent {

    }

    static AuthorizationSuccessEvent createSuccess() {
        return new AuthorizationSuccessEvent();
    }

    static AuthorizationFailedEvent createFailed() {
        return new AuthorizationFailedEvent();
    }
}
