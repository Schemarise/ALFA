package com.schemarise.alfa.generators.exporters.java;

import com.schemarise.alfa.runtime.Alfa;
import org.junit.Assert;
import org.junit.Test;
import udts.AnotherRecord;
import udts.MyRecord;

import java.time.LocalDate;
import java.util.Optional;

public class TestJavaUDT {
    @Test
    public void testRecord() throws Exception {
        AnotherRecord or = AnotherRecord.builder().setDoubleField(129341.234).setIntField(42).setStringField("efg").build();

        MyRecord r = MyRecord.builder().setStringField("abc").setDoubleField(23.543).setIntField(200).setOptDateField(Optional.of(LocalDate.now())).setRecordField(or).build();

        String json = Alfa.jsonCodec().toJsonString(r);

        MyRecord decoded = Alfa.jsonCodec().fromJsonString(json);

        Assert.assertEquals(r, decoded);

    }
}
