package com.schemarise.alfa.runtime;

import schemarise.alfa.runtime.model.DecisionTable__HitPolicy;
import com.schemarise.alfa.runtime_int.IntImpl;
import schemarise.alfa.runtime.model.Try;

import java.util.List;

public class DecisionTable {
    public static <T> Try<List<T>> getDecisionTableResults(List<Object> inputs, DecisionTable__HitPolicy hp, List<IDecisionExecTableRow> rules) {
        return IntImpl.getDecisionTableResults(inputs, hp, rules);
    }

    public static <T> Try<T> getDecisionTableResult(List<Object> inputs, DecisionTable__HitPolicy hp, List<IDecisionExecTableRow> rules) {
        return IntImpl.getDecisionTableResult(inputs, hp, rules);
    }
}
