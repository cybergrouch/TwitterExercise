package com.lange.common.rest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lange.common.dbc.DesignByContract;
import com.lange.common.functional.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.lange.common.dbc.DesignByContract.*;


/**
 * Created by lange on 19/1/16.
 */
public class Request<T> implements IRequest<T>, DesignByContract.Ensurable<IRequest<T>> {

    private final Method method;
    private final String protocol;
    private final String baseUrl;
    private final int port;
    private final String endPoint;
    private Map<String, String> headers;
    private List<Pair<String, String>> parameters;

    public static <T> IRequest<T> create(Method method, String protocol, String baseUrl, int port, String endPoint) {
        requireNotNull(method);
        requireNotEmpty(protocol);
        requireNotEmpty(baseUrl);
        requireNotEmpty(endPoint);
        validate(port, x -> x > 0 ? Optional.empty() : Optional.of(String.format("Invalid port [%s]", port)));
        IRequest<T> request = new Request<T>(method, protocol, baseUrl, port, endPoint);
        return ensureContracts(request);
    }

    protected Request(Method method, String protocol, String baseUrl, int port, String endPoint) {
        this.method = method;
        this.protocol = protocol;
        this.baseUrl = baseUrl;
        this.port = port;
        this.endPoint = endPoint;
        clearHeaders();
        clearParameters();
    }

    @Override
    public Request ensurePostConditionContracts() {
        requireNotNull(getMethod());
        requireNotEmpty(getProtocol());
        requireNotEmpty(getBaseUrl());
        requireNotEmpty(getEndPoint());
        requireNotNull(getHeaders());
        validate(port, x -> x > 0 ? Optional.empty() : Optional.of(String.format("Invalid port [%s]", port)));
        return this;
    }

    @Override
    public String getBaseUrl() {
        return this.baseUrl;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public String getEndPoint() {
        return this.endPoint;
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }

    @Override
    public Method getMethod() {
        return this.method;
    }

    @Override
    public Map<String, String> getHeaders() {
        return ImmutableMap.copyOf(this.headers);
    }

    @Override
    public void putHeaders(Pair<String, String>... headerKeyValuePairs) {
        for (Pair<String, String> headerKeyValuePair : headerKeyValuePairs) {
            this.headers.put(headerKeyValuePair.left, headerKeyValuePair.right);
        }
    }

    @Override
    public void clearHeaders() {
        this.headers = Maps.newHashMap();
    }

    @Override
    public List<Pair<String, String>> getParameters() {
        return ImmutableList.copyOf(this.parameters);
    }

    @Override
    public void putParameters(Pair<String, String>... parameterKeyValuePairs) {
        requireNotNull(this.parameters);
        requireNotNull(parameterKeyValuePairs);
        for (Pair<String, String> parameterKeyValuePair : parameterKeyValuePairs) {
            requireNotEmpty(parameterKeyValuePair.left);
            Optional<Pair<String, String>> parameterByKey = getParameterByKey(parameterKeyValuePair.left);
            if (parameterByKey.isPresent()) {
                this.parameters.remove(parameterByKey.get());
            }
            this.parameters.add(parameterKeyValuePair);
        }
    }

    @Override
    public void clearParameters() {
        this.parameters = Lists.newArrayList();
    }

    @Override
    public Optional<Pair<String, String>> getParameterByKey(String parameterName) {
        requireNotNull(this.parameters);
        requireNotEmpty(parameterName);
        return this.parameters.stream().filter(x -> parameterName.equals(x.left)).findAny();
    }
}
