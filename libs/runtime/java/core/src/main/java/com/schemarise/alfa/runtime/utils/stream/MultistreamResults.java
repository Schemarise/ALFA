package com.schemarise.alfa.runtime.utils.stream;

import java.util.List;

public class MultistreamResults<T> {
    private final long realCount;
    private List<T> results;

    // results may be given as empty collection for cases like DQ - no need to really collect
    // but real count is a counter
    public MultistreamResults(long realCount, List<T> results) {
        this.results = results;
        this.realCount = realCount;
    }

    public long size() {
        return realCount;
    }

    public List<T> getResults() {
        return results;
    }


    @Override
    public String toString() {
        return "MultistreamResults [ " +
                "results=" + results +
                '}';
    }
}
