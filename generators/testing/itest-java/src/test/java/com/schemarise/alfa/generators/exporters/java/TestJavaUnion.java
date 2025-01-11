package com.schemarise.alfa.generators.exporters.java;

import com.schemarise.alfa.runtime.*;
import org.junit.Assert;
import org.junit.Test;
import udts.MyUnion;
import udts.MyUnionWithConstraints;
import udts.UnionRefRecord;

public class TestJavaUnion {
    @Test
    public void testUnion() throws Exception {
        MyUnion intCase = MyUnion.builder().setUFInt(10).build();
        MyUnion recCase = MyUnion.builder().setUFRec(UnionRefRecord.builder().setDoubleField(10.11).setIntField(2).setStringField("abc").build()).build();

        MyUnion decodedIntCase = Alfa.jsonCodec().fromJsonString(Alfa.jsonCodec().toJsonString(intCase));
        MyUnion decodedRecCase = Alfa.jsonCodec().fromJsonString(Alfa.jsonCodec().toJsonString(recCase));

        Assert.assertEquals(intCase, decodedIntCase);
        Assert.assertEquals(recCase, decodedRecCase);
    }

    @Test(expected = AlfaRuntimeException.class)
    public void testUnionOneField() throws Exception {
        IBuilderConfig cfg = BuilderConfig.builder().build();
        MyUnion.builder(cfg).setUFInt(10).setUFString("a").build();
    }

    @Test
    public void testUnionFieldConstrait() throws Exception {
        IBuilderConfig cfg = BuilderConfig.builder().setAssertListener(new ValidationCollectingListener()).build();
        MyUnionWithConstraints.builder(cfg).setUFInt(1000).build();
        Assert.assertTrue(cfg.getAssertListener().getValidationReport().getAlerts().size() > 0);
    }

}
