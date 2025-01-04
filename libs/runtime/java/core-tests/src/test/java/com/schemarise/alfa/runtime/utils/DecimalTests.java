package com.schemarise.alfa.runtime.utils;

import com.schemarise.alfa.runtime.Alfa;
import com.schemarise.alfa.runtime.BuilderConfig;
import com.schemarise.alfa.runtime.IBuilderConfig;
import com.schemarise.alfa.runtime.JsonCodec;
import flattentest.DecimalTestObj;
import org.junit.Test;

import java.math.BigDecimal;

public class DecimalTests {
    @Test
    public void testDecimalOverflow() throws Exception {
        BigDecimal d1 = new BigDecimal("234908.22343");
        BigDecimal d2 = new BigDecimal("20890.2342");
        BigDecimal d3 = new BigDecimal("20890.2342");


        IBuilderConfig cfg = BuilderConfig.builder().build();

        DecimalTestObj o = DecimalTestObj.builder(cfg).setDec1Val(d1).setDec2Val(d2).setDec3Val(d3).build();

        System.out.println(cfg.getAssertListener().getValidationReport());

        String json = Alfa.jsonCodec().toJsonString(o);

        System.out.println(json);

        DecimalTestObj decoded = Alfa.jsonCodec().fromJsonString(json);

    }
}
