package com.lange.common.rest;

import com.lange.common.functional.Or;

/**
 * Encapsulates a remote REST response.
 * <p>
 * Created by lange on 19/1/16.
 */
public interface IResponse<T> {

    /**
     * @return the status code returned by the server. Is optional depending on whether the server is able to respond properly.
     */
    int getStatusCode();

    /**
     * @return the payload of the REST response. Is optional depending on the server response.
     */
    Or<T, RemoteException> getPayload();
}
