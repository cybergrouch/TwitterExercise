package com.lange.common.rest;


import com.lange.common.dbc.DesignByContract.*;
import com.lange.common.functional.Or;

import java.util.Optional;

import static com.lange.common.dbc.DesignByContract.*;

/**
 * Created by lange on 19/1/16.
 */
public class Response<T> implements IResponse<T>, Ensurable<IResponse<T>> {

    public final int statusCode;
    public final Or<T, RemoteException> payload;

    public Response(int statusCode, T payload) {
        this.statusCode = statusCode;
        this.payload = Or.createLeft(payload);
    }

    public Response(int statusCode, RemoteException remoteException) {
        this.statusCode = statusCode;
        this.payload = Or.createRight(remoteException);
    }

    public static <T> IResponse<T> create(int statusCode, T responsePayload) {
        requireNotNull(responsePayload);
        validate(statusCode,
                x -> x >= 100 && x < 600
                        ? Optional.empty()
                        : Optional.of(String.format("Invalid status code [%s]", statusCode)));
        Response<T> response = new Response<>(statusCode, responsePayload);
        return ensureContracts(response);
    }

    public static <T> IResponse<T> create(int statusCode, RemoteException remoteException) {
        requireNotNull(remoteException);
        validate(statusCode,
                x -> x >= 100 && x < 600
                        ? Optional.empty()
                        : Optional.of(String.format("Invalid status code [%s]", statusCode)));
        Response<T> response = new Response<>(statusCode, remoteException);
        return ensureContracts(response);
    }

    @Override
    public IResponse<T> ensurePostConditionContracts() {
        requireNotNull(getPayload());
        validate(getStatusCode(),
                x -> x >= 100 && x < 600
                        ? Optional.empty()
                        : Optional.of(String.format("Invalid status code [%s]", getStatusCode())));
        return this;
    }

    @Override
    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public Or<T, RemoteException> getPayload() {
        return this.payload;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Response{").append("\n");
        buffer.append("\tstatusCode: ").append(statusCode).append("\n");
        buffer.append("\tpayload: ");
        if (payload.right.isPresent()) {
            buffer.append(payload.right.get());
        } else {
            buffer.append(payload.left.get()).append("\n");
        }
        buffer.append("}");
        return buffer.toString();
    }
}
