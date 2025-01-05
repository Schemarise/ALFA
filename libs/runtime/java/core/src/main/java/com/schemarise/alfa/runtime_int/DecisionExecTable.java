package com.schemarise.alfa.runtime_int;

import com.schemarise.alfa.runtime.IDecisionExecTable;
import com.schemarise.alfa.runtime.IDecisionExecTableRow;
import schemarise.alfa.runtime.model.DecisionTable__HitPolicy;

import java.util.List;
import java.util.stream.Collectors;

public class DecisionExecTable implements IDecisionExecTable {
    private final List<Object> inputs;
    private final List<IDecisionExecTableRow> rules;
    private final DecisionTable__HitPolicy policy;

    public DecisionExecTable(List<Object> inputs, DecisionTable__HitPolicy hp, List<IDecisionExecTableRow> rules) {
        this.inputs = inputs;
        this.rules = rules;
        this.policy = hp;
    }

    @Override
    public <T> List<T> execute() {

        switch (policy) {
            case first:
            case unique:
            case anyof:
        }

        List<T> res = rules.
                stream().
                parallel().
                map(dr -> dr.evaluate(inputs)).
                filter(e -> e.isPresent()).
                map(e -> (T) e.get()).
                collect(Collectors.toList());

        return res;
    }
}
