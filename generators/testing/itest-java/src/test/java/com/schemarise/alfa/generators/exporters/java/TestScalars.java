package com.schemarise.alfa.generators.exporters.java;

import com.schemarise.alfa.runtime.Alfa;
import com.schemarise.alfa.runtime.AlfaObject;
import com.schemarise.alfa.runtime.utils.AlfaRandomizer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestScalars {
    private AlfaRandomizer rand = new AlfaRandomizer();

    @Test
    public void testScalars() {

        List<String> scalars = new ArrayList<>();
        scalars.add("Integer");
        scalars.add("Binary");
        scalars.add("Boolean");
        scalars.add("Date");
        scalars.add("Datetime");
        scalars.add("Decimal");
        scalars.add("Duration");
        scalars.add("Long");
        scalars.add("Short");
        scalars.add("String");
        scalars.add("Time");
        scalars.add("Uuid");

        scalars.forEach(s -> {
            AlfaObject d = rand.random("scalars." + s);
            System.out.println(d);

            try {
                String json = Alfa.jsonCodec().toJsonString(d);
                AlfaObject decoded = Alfa.jsonCodec().fromJsonString(json);

                Assert.assertEquals(d, decoded);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
