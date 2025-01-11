package com.schemarise.alfa.runtime;

import schemarise.alfa.runtime.model.asserts.ConstraintType;
import schemarise.alfa.runtime.model.asserts.ValidationAlert;

public class AlfaRuntimeValiationException extends AlfaRuntimeException {
    private final ValidationAlert alert;

    public AlfaRuntimeValiationException(ValidationAlert vr) {
        super(vr.getViolatedConstraint().orElse(ConstraintType.Unknown), vr.getMessage());
        this.alert = vr;
    }

    public ValidationAlert getValidationAlert() {
        return this.alert;
    }
}
