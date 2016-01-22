package com.lange.common.rest;

import com.lange.common.dbc.DesignByContract;

import static com.lange.common.dbc.DesignByContract.*;


/**
 * Created by lange on 20/1/16.
 */
public class RemoteException extends RuntimeException implements DesignByContract.Ensurable<RemoteException> {
    private RemoteException(String message, Throwable exception) {
        super(message, exception);
    }

    private RemoteException(String message) {
        super(message);
    }

    public static RemoteException create(String message, Throwable throwable) {
        requireNotEmpty(message);
        requireNotNull(throwable);
        RemoteException remoteException = new RemoteException(message, throwable);
        return ensureContracts(remoteException);
    }

    public static RemoteException create(String message) {
        requireNotEmpty(message);
        RemoteException remoteException = new RemoteException(message);
        return ensureContracts(remoteException);
    }

    @Override
    public RemoteException ensurePostConditionContracts() {
        requireNotEmpty(getMessage());
        return this;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("RemoteException{ [").append(getMessage()).append("] }\n");
        return buffer.toString();
    }

}
