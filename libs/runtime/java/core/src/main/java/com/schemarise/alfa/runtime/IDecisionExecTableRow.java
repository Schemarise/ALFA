package com.schemarise.alfa.runtime;

import java.util.List;
import java.util.Optional;

public interface IDecisionExecTableRow {
    Optional<Object> evaluate(List<Object> ruleInputs);
}
