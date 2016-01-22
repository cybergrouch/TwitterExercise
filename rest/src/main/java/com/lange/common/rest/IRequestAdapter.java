package com.lange.common.rest;

import com.lange.common.dbc.DesignByContract;

/**
 * Handles the IRequest object and passes it to the server. It processes the server response
 * and returns an IResponse object back. It is an adapter because the implementation of this process
 * is abstracted.
 * <p>
 * Created by lange on 20/1/16.
 */
public interface IRequestAdapter<T> extends DesignByContract.Ensurable<IRequestAdapter<T>> {

    /**
     * Executes the associated request object. It sends it through the internet and awaits for the response.
     * It then uses the listener object parameter to process the server response.
     *
     * @param listener the callback to process the server response
     */
    void execute(IResponseListener<T> listener);

    /**
     * Abstracts the behaviour for processing the server response.
     *
     * @param <T> the expected type of the response payload from the server
     */
    interface IResponseListener<T> {
        /**
         * Process the success flow where the server returns the expected result object.
         *
         * @param result the expected server result object
         */
        void onSuccess(IResponse<T> result);

        /**
         * Process the failure flow where the server returns an expection instead.
         * The exception can be from client or server.
         *
         * @param exception the exception encapsulating the failure
         */
        void onFailure(RemoteException exception);
    }
}
