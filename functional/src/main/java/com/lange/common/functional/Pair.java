package com.lange.common.functional;

import com.lange.common.dbc.DesignByContract;

import static com.lange.common.dbc.DesignByContract.ensureContracts;
import static com.lange.common.dbc.DesignByContract.requireNotNull;

/**
 * A simple 2-tuple implementation.
 * <p>
 * Created by lange on 19/1/16.
 */
public class Pair<L, R> implements DesignByContract.Ensurable<Pair<L, R>> {
    public final L left;
    public final R right;

    private Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Pair<L, R> create(L left, R right) {
        requireNotNull(left);
        requireNotNull(right);
        Pair<L, R> pair = new Pair<>(left, right);
        return ensureContracts(pair);
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (!left.equals(pair.left)) return false;
        return right.equals(pair.right);

    }

    @Override
    public int hashCode() {
        int result = left.hashCode();
        result = 31 * result + right.hashCode();
        return result;
    }

    @Override
    public Pair<L, R> ensurePostConditionContracts() {
        requireNotNull(left);
        requireNotNull(right);
        return this;
    }
}
