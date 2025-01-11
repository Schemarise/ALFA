package com.schemarise.alfa.runtime;

import schemarise.alfa.runtime.model.asserts.SeverityType;
import schemarise.alfa.runtime.model.asserts.ValidationAlert;
import schemarise.alfa.runtime.model.asserts.ValidationReport;
import schemarise.alfa.runtime.model.asserts.ValidationReportKey;
import com.schemarise.alfa.runtime.utils.Utils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ValidationCollectingListener implements IValidationListener {
    private AlfaObject currentObj;

    private final Collection<ValidationAlert.ValidationAlertBuilder> alerts = new ConcurrentLinkedQueue<>();
    private final AtomicLong errorCount = new AtomicLong();
    private final AtomicLong warningCount = new AtomicLong();
    private final AtomicLong totalRecords = new AtomicLong();

    private final ThreadLocal<String> threadLocalSourceLineInfo = new ThreadLocal<>();
    private final ThreadLocal<String> threadLocalTypeName = new ThreadLocal<>();

    @Override
    public void enterAlfaObjectContext(AlfaObject ao) {
        currentObj = ao;
    }

    public AlfaObject getCurrentObj() {
        return currentObj;
    }

    @Override
    public void exitAlfaObjectContext(AlfaObject ao) {

    }

    // synchronized to avoid contention
    @Override
    public synchronized void addFailure(ValidationAlert.ValidationAlertBuilder va) {

        if (va.getDataQualityCategory().isEmpty() && va.getViolatedConstraint().isPresent())
            va.setDataQualityCategory(Optional.of(Utils.constraintTypeToDqType(va.getViolatedConstraint().get())));

        if (va.getTimestamp() == null)
            va.setTimestamp(LocalDateTime.now());

        if (va.getSeverity() == null)
            va.setSeverity(SeverityType.Error);


        if (va.getTypeName().isEmpty())
            va.setTypeName(Optional.ofNullable(threadLocalTypeName.get()));

        if (va.getSourceInfo().isEmpty())
            va.setSourceInfo(Optional.ofNullable(threadLocalSourceLineInfo.get()));

        alerts.add(va);

        if (va.getSeverity() == SeverityType.Error)
            errorCount.getAndIncrement();
        else
            warningCount.getAndIncrement();
    }

    @Override
    public long incrementTotalRecords() {
        return totalRecords.getAndIncrement();
    }

    @Override
    public void clear() {
        alerts.clear();
    }

    @Override
    public long getErrorCount() {
        return errorCount.get();
    }

    @Override
    public long getWarningCount() {
        return warningCount.get();
    }

    @Override
    public ValidationReport.ValidationReportBuilder getValidationReport() {

        List<ValidationAlert> l = alerts.stream().map(e -> e.build()).collect(Collectors.toList());

        int errors = (int) l.stream().filter(e -> e.getSeverity() == SeverityType.Error).count();
        int warnings = (int) l.stream().filter(e -> e.getSeverity() == SeverityType.Warning).count();

        return ValidationReport.builder().
                addAllAlerts(l).
                set$key(ValidationReportKey.builder().setId(UUID.randomUUID()).build()).
                setTimestamp(LocalDateTime.now()).
                setTotalRecords(totalRecords.get()).
                setTotalErrors(errors).
                setTotalWarnings(warnings);
    }

    @Override
    public void setCurrentSourceInfo(String sourceLineInfo) {
        threadLocalSourceLineInfo.set(sourceLineInfo);
    }

    @Override
    public void setCurrentTypeName(String tn) {
        threadLocalTypeName.set(tn);
    }

}
