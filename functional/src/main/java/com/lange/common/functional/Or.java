package com.lange.common.functional;

import com.lange.common.dbc.DesignByContract;

import java.util.Optional;

import static com.lange.common.dbc.DesignByContract.*;

/**
 * Created by lange on 20/1/16.
 */
public class Or<T1, T2> implements DesignByContract.Ensurable<Or<T1, T2>> {

    public final Optional<T1> left;
    public final Optional<T2> right;

    public Or(Optional<T1> left, Optional<T2> right) {
        this.left = left;
        this.right = right;
    }

    public static <T1, T2> Or<T1, T2> createLeft(T1 left) {
        requireNotNull(left);
        Or<T1, T2> or = new Or(Optional.of(left), Optional.empty());
        return ensureContracts(or);
    }

    public static <T1, T2> Or<T1, T2> createRight(T2 right) {
        requireNotNull(right);
        Or<T1, T2> or = new Or(Optional.empty(), Optional.of(right));
        return ensureContracts(or);
    }

    @Override
    public Or<T1, T2> ensurePostConditionContracts() {
        validate(Pair.create(left, right),
                x -> (x.left.isPresent()) != (x.right.isPresent())
                        ? Optional.empty()
                        : x.left.isPresent()
                        ? Optional.of("Left and Right values cannot be given at the same time")
                        : Optional.of("Left and Right values cannot be null at the same time"));
        return this;
    }
}
