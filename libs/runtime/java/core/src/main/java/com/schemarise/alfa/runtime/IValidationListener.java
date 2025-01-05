package com.schemarise.alfa.runtime;

import schemarise.alfa.runtime.model.asserts.ValidationAlert;
import schemarise.alfa.runtime.model.asserts.ValidationReport;

public interface IValidationListener {
    void enterAlfaObjectContext(AlfaObject ao);

    void exitAlfaObjectContext(AlfaObject ao);

    void addFailure(ValidationAlert.ValidationAlertBuilder va);

    long incrementTotalRecords();

//    void addFailure( AlfaConstraintError ce );
//    void addFailure( String raiseType, String assertName, String message );
//    void addFailure( String sourceName, int sourcePosition, String raiseType, String assertName, String message );

    void clear();

    long getErrorCount();

    long getWarningCount();

    ValidationReport.ValidationReportBuilder getValidationReport();

    void setCurrentSourceInfo(String sourceLineInfo);

    void setCurrentTypeName(String expectedType);
}
