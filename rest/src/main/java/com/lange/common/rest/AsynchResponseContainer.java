package com.lange.common.rest;

import com.lange.common.dbc.DesignByContract;
import com.lange.common.functional.Or;

import java.util.Optional;

import static com.lange.common.dbc.DesignByContract.ensureContracts;
import static com.lange.common.dbc.DesignByContract.validate;

/**
 * Created by lange on 21/1/16.
 */
public class AsynchResponseContainer<S, F> implements DesignByContract.Ensurable<AsynchResponseContainer<S, F>> {

    Optional<Or<S, F>> container = Optional.empty();

    public static final <S, F> AsynchResponseContainer<S, F> create() {
        AsynchResponseContainer<S, F> container = new AsynchResponseContainer<>();
        return ensureContracts(container);
    }

    private AsynchResponseContainer() {
        super();
    }

    public void setSuccessResult(S contained) {
        validate(container, x -> !container.isPresent() ? Optional.empty() : Optional.of("There is a previously contained value"));
        this.container = Optional.of(Or.createLeft(contained));
    }

    public void setFailureArtifact(F contained) {
        validate(container, x -> !container.isPresent() ? Optional.empty() : Optional.of("There is a previously contained value"));
        this.container = Optional.of(Or.createRight(contained));
    }

    public void reset() {
        container = Optional.empty();
    }

    public Optional<Or<S, F>> getContained() {
        return this.container;
    }

    @Override
    public AsynchResponseContainer<S, F> ensurePostConditionContracts() {
        return this;
    }
}
