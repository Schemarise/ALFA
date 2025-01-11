package com.schemarise.alfa.runtime;

import schemarise.alfa.runtime.model.asserts.ConstraintType;
import schemarise.alfa.runtime.model.asserts.ValidationAlert;
import schemarise.alfa.runtime.model.asserts.ValidationReport;

public class DefaultValidationListener implements IValidationListener {
    @Override
    public void enterAlfaObjectContext(AlfaObject ao) {

    }

    @Override
    public void exitAlfaObjectContext(AlfaObject ao) {

    }

    @Override
    public void addFailure(ValidationAlert.ValidationAlertBuilder va) {
        throw new AlfaRuntimeValiationException( va.build() );
    }

    @Override
    public long incrementTotalRecords() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public long getErrorCount() {
        return 0;
    }

    @Override
    public long getWarningCount() {
        return 0;
    }

    @Override
    public ValidationReport.ValidationReportBuilder getValidationReport() {
        return null;
    }

    @Override
    public void setCurrentSourceInfo(String sourceLineInfo) {

    }

    @Override
    public void setCurrentTypeName(String expectedType) {

    }
}
