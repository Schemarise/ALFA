package com.schemarise.alfa.runtime;

import com.schemarise.alfa.runtime.IDecisionColumnExpr;
import com.schemarise.alfa.runtime.IDecisionExecTableRow;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class DecisionExecTableRow implements IDecisionExecTableRow {
    private final List<IDecisionColumnExpr> rules;
    private final Object output;

    public DecisionExecTableRow(List<IDecisionColumnExpr> rules, Object output) {
        this.rules = rules;
        this.output = output;
    }

    @Override
    public Optional<Object> evaluate(List<Object> ruleInputs) {

//        Utils.zip(ruleInputs, rules).stream().parallel()

        for (int i = 0; i < ruleInputs.size(); i++) {
            Function<Object, Boolean> rule = rules.get(i);
            Boolean pass = rule.apply(ruleInputs.get(i));
            if (!pass)
                return Optional.empty();
        }

        return Optional.of(output);
    }
}
