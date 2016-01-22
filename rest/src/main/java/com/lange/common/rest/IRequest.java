package com.lange.common.rest;

import com.lange.common.dbc.DesignByContract;
import com.lange.common.functional.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This encapsulates a remote REST request to a server.
 * <p>
 * Created by lange on 19/1/16.
 */
public interface IRequest<T> extends DesignByContract.Ensurable<IRequest<T>> {

    /**
     * @return the base url of the target server where this request will be routed
     */
    String getBaseUrl();

    /**
     * @return the port of the target server where this request will be routed
     */
    int getPort();

    /**
     * @return the REST endpoint of the request in the target server where this request will be routed
     */
    String getEndPoint();

    /**
     * @return the protocol to be used in connecting to the target server
     */
    String getProtocol();

    /**
     * @return the HTTP method of the request
     */
    Method getMethod();

    /**
     * @return a immutable copy of headers of the request
     */
    Map<String, String> getHeaders();

    /**
     * Appends multiple key value pair parameters into the request header. If the header already contains
     * the specified key, the old value will be replaced by the new value specified in the parameter.
     *
     * @param headerKeyValuePairs the key value pairs to be appended into the request header
     */
    void putHeaders(Pair<String, String>... headerKeyValuePairs);

    /**
     * Resets the headers to blank
     */
    void clearHeaders();

    /**
     * @return an immutable copy of parameters of the request
     */
    List<Pair<String, String>> getParameters();

    /**
     * Appends multiple key value pair parameters into the request paramater. If a key already exists, the
     * old value is kicked off in favour of the new value.
     *
     * @param parameterKeyValuePairs
     */
    void putParameters(Pair<String, String>... parameterKeyValuePairs);

    /**
     * Resets the parameters to blank
     */
    void clearParameters();

    /**
     * @param parameterName this corresponds to the key being queried
     * @return an Optional that encapsulates the Key value pair where the key corresponds to the parameter name passed.
     */
    Optional<Pair<String, String>> getParameterByKey(String parameterName);

    /**
     * HTTP methods available for the request abstraction
     */
    enum Method {
        GET, POST;
    }
}
