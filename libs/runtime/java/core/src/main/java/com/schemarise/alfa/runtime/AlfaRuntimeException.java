package com.schemarise.alfa.runtime;

import schemarise.alfa.runtime.model.asserts.*;
import com.schemarise.alfa.runtime.utils.Utils;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Based exception class for any runtime exceptions thrown in Alfa
 */
public class AlfaRuntimeException extends RuntimeException {
    private final ConstraintType constraintType;
    private String appended = "";
    private Optional<String> impactedField = Optional.empty();
    private Optional<String> impactedType = Optional.empty();

    private static boolean ShowStackDump =
            System.getProperty("ALFA.Print.AlfaRuntimeException", "false").toLowerCase().equals("true");

    public AlfaRuntimeException(String s) {
        this(ConstraintType.Unknown, s);
    }

    public AlfaRuntimeException(ConstraintType constraintType, String s) {
        super(s);
        this.constraintType = constraintType;

        if (ShowStackDump) {
            this.printStackTrace();
        }
    }

    @Override
    public String getMessage() {
        return super.getMessage() + appended;
    }

    public AlfaRuntimeException(ConstraintType constraintType, String s, Throwable t) {
        super(s, t);
        this.constraintType = constraintType;
    }

    public AlfaRuntimeException(ConstraintType constraintType, Throwable t) {
        super(t);
        this.constraintType = constraintType;
    }

    public ValidationAlert.ValidationAlertBuilder toValidationAlert(String msgPrefix) {
        String msg = getMessage();

        if (msgPrefix.length() > 0)
            msg = msgPrefix + ". " + msg;

        return ValidationAlert.builder().
                setViolatedConstraint(Optional.of(constraintType)).
                setMessage(msg).
                setSeverity(SeverityType.Error).
                setFieldName(impactedField).
                setTypeName(impactedType).
                setDataQualityCategory(Optional.of(Utils.constraintTypeToDqType(constraintType))).
                setTimestamp(LocalDateTime.now());
    }

    public AlfaRuntimeException appendMessage(String s) {
        appended = s;
        return this;
    }

    public void setValidationErrorField(String name) {
        if (impactedField.isPresent())
            impactedField = Optional.of(name + "/" + impactedField.get());
        else
            impactedField = Optional.of(name);
    }

    public void setValidationErrorTypeName(String name) {
        if (impactedType.isPresent())
            impactedType = Optional.of(name + "/" + impactedType.get());
        else
            impactedType = Optional.of(name);
    }


//    public IValidationError toValidationError(String msgPrefix) {
//        return DataViolation.builder().setMessage(msgPrefix + ". " + getMessage()).build();
//    }
}
