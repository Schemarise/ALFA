package com.schemarise.alfa.runtime;

import schemarise.alfa.runtime.model.DecisionTable__HitPolicy;
import com.schemarise.alfa.runtime_int.IntImpl;
import org.junit.Test;
import schemarise.alfa.runtime.model.Try;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DecisionTableTest {
    @Test
    public void testDecision() {
        java.util.List<Object> inputs = Arrays.asList(new Object[]{1, "A"});

        IDecisionColumnExpr<Integer> r1c1 = o -> o.toString().equals("1");
        IDecisionColumnExpr<String> r1c2 = o -> o.toString().equals("B");
        DecisionExecTableRow r1 = new DecisionExecTableRow(list(r1c1, r1c2), list(10, 20));

        IDecisionColumnExpr<Integer> r2c1 = o -> o.toString().equals("1");
        IDecisionColumnExpr<String> r2c2 = o -> o.toString().equals("A");
        DecisionExecTableRow r2 = new DecisionExecTableRow(list(r2c1, r2c2), list(100, 100));

        List<IDecisionExecTableRow> rules = list(r1, r2);

        Try<List<Object>> output = IntImpl.getDecisionTableResults(inputs, DecisionTable__HitPolicy.unique, rules);
    }

    private <T> List<T> list(T... l) {
        List<T> res = new ArrayList<>();

        for (int i = 0; i < l.length; i++) {
            res.add(l[i]);
        }

        return res;
    }
}
