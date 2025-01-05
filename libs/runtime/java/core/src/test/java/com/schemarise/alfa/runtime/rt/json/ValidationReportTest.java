package com.schemarise.alfa.runtime.json;

import schemarise.alfa.runtime.model.asserts.*;
import com.schemarise.alfa.runtime.codec.json.JsonCodecConfig;
import com.schemarise.alfa.runtime.codec.json.JsonTypeWriteMode;
import com.schemarise.alfa.runtime.utils.Utils;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ValidationReportTest {
    private LocalDateTime ts = LocalDateTime.of(LocalDate.of(2022, 8, 9), LocalTime.of(13, 33));

    @Test
    public void testReport() throws IOException {

        ValidationReport.ValidationReportBuilder vr = ValidationReport.builder();

        vr.set$key(ValidationReportKey.builder().setId(UUID.randomUUID()).build());
        vr.setTimestamp(ts);
        vr.setDataFormat(Optional.of("csv"));
        vr.setDataFormatInfo(Optional.of("cob-trades.csv"));
        vr.setSourceSystem(Optional.of("TradeDataSys"));
        vr.setSourceSubsystem(Optional.of("TradePublisher"));
        vr.setSourceFeed(Optional.of("CobTradesFeed"));
        vr.setSourceSubfeed(Optional.of("CobTradesFeed2"));
        vr.setSourceInfo(Optional.of("Verson1"));

        vr.addAlerts(err(ConstraintType.UserDefinedAssert, "", "ALFABank.TradeDataSys.Trade", "Maturity date '2022-08-01' should be after Trade date '2022-08-08'", "Line:6905", "ValidMaturityDate", DataQualityType.Consistency, SeverityType.Error));
        vr.addAlerts(err(ConstraintType.UserDefinedAssert, "", "ALFABank.TradeDataSys.Trade", "Found 4 duplicate TradeIds", "", "DuplicateTradeId", DataQualityType.Uniqueness, SeverityType.Warning));
        vr.addAlerts(err(ConstraintType.UserDefinedAssert, "", "ALFABank.TradeDataSys.Trade", "Expected SLA 11:00 breached", "", "BatchRecvTime", DataQualityType.Timeliness, SeverityType.Warning));

        vr.addAlerts(err(ConstraintType.MandatoryFieldNotSet, "PartyId", "ALFABank.TradeDataSys.Trade", "Mandatory field 'PartyId' not specified", "Line:2309", null, null, SeverityType.Error));
        vr.addAlerts(err(ConstraintType.MandatoryFieldNotSet, "PartyId", "ALFABank.TradeDataSys.Trade", "Mandatory field 'PartyId' not specified", "Line:2311", null, null, SeverityType.Error));
        vr.addAlerts(err(ConstraintType.UnknownField, "ContryCode", "ALFABank.TradeDataSys.Trade", "Unknown field 'ContryCode'", "Line:5350", null, null, SeverityType.Error));
        vr.addAlerts(err(ConstraintType.UnknownField, "ContryCode", "ALFABank.TradeDataSys.Trade", "Unknown field 'ContryCode'", "Line:5351", null, null, SeverityType.Error));
        vr.addAlerts(err(ConstraintType.UnknownField, "ContryCode", "ALFABank.TradeDataSys.Trade", "Unknown field 'ContryCode'", "Line:5352", null, null, SeverityType.Error));
        vr.addAlerts(err(ConstraintType.UnknownField, "ContryCode", "ALFABank.TradeDataSys.Trade", "Unknown field 'ContryCode'", "Line:5353", null, null, SeverityType.Error));
        vr.addAlerts(err(ConstraintType.UnknownField, "ContryCode", "ALFABank.TradeDataSys.Trade", "Unknown field 'ContryCode'", "Line:5354", null, null, SeverityType.Error));
        vr.addAlerts(err(ConstraintType.UnknownField, "ContryCode", "ALFABank.TradeDataSys.Trade", "Unknown field 'ContryCode'", "Line:5355", null, null, SeverityType.Error));
        vr.addAlerts(err(ConstraintType.UnknownField, "ContryCode", "ALFABank.TradeDataSys.Trade", "Unknown field 'ContryCode'", "Line:5356", null, null, SeverityType.Error));
        vr.addAlerts(err(ConstraintType.UnknownField, "ContryCode", "ALFABank.TradeDataSys.Trade", "Unknown field 'ContryCode'", "Line:5357", null, null, SeverityType.Error));

        vr.setTotalRecords(200);
        vr.setTotalErrors((int) vr.getAlerts().stream().filter(e -> e.getSeverity() == SeverityType.Error).count());
        vr.setTotalWarnings((int) vr.getAlerts().stream().filter(e -> e.getSeverity() == SeverityType.Warning).count());
        ValidationReport v = vr.build();
        List<ValidationAlert> alerts = v.getAlerts();

        JsonCodecConfig cc = JsonCodecConfig.builder().setWriteTypeMode(JsonTypeWriteMode.NeverWriteType).build();

//        System.out.println(JsonCodec.toJsonString(cc, v));

//        alerts.forEach( e -> {
//            try {
//                System.out.println(JsonCodec.toJsonString(cc, e) + ",");
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
//        });

//        System.out.println(JsonCodec.toFormattedJson(cc, vr.build()));

    }

    private ValidationAlert err(ConstraintType constraintType, String fieldName, String type, String msg, String srcInfo,
                                String assertName, DataQualityType dq, SeverityType st) {
        ValidationAlert.ValidationAlertBuilder va = ValidationAlert.builder();
        va.setSeverity(st);
        va.setViolatedConstraint(Optional.of(constraintType));
        va.setFieldName(Optional.of(fieldName));
        va.setTypeName(Optional.of(type));
        va.setMessage(msg);
        va.setTimestamp(ts);

        if (assertName != null)
            va.setAssertName(Optional.of(assertName));

        if (dq == null)
            va.setDataQualityCategory(Optional.of(Utils.constraintTypeToDqType(constraintType)));
        else
            va.setDataQualityCategory(Optional.of(dq));

        va.setSourceInfo(Optional.of(srcInfo));
        return va.build();
    }
}
