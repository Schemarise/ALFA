package com.schemarise.alfa.runtime;

import java.util.Map;

import schemarise.alfa.runtime.model.Pair;

class MappablePair<L, R> implements Map.Entry<L, R> {
    private L l;
    private R r;

    public static <L, R> MappablePair<L, R> fromPair(Pair<L, R> p) {
        return new MappablePair<L, R>(p.getLeft(), p.getRight());
    }

    public Pair<L, R> toPair() {
        return Pair.builder().setLeft(l).setRight(r).build();
    }

    public MappablePair(L l, R r) {
        this.l = l;
        this.r = r;
    }

    public L getL() {
        return l;
    }

    public R getR() {
        return r;
    }

    @Override
    public L getKey() {
        return l;
    }

    @Override
    public R getValue() {
        return r;
    }

    @Override
    public R setValue(R value) {
        r = value;
        return null;
    }
}
