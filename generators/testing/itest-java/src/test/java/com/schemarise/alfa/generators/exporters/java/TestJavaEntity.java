package com.schemarise.alfa.generators.exporters.java;

import com.schemarise.alfa.runtime.Alfa;
import org.junit.Assert;
import org.junit.Test;
import udts.MyEntity1;
import udts.MyEntity1Key;

import java.util.UUID;

public class TestJavaEntity {
    @Test
    public void testEntity() throws Exception {
        MyEntity1Key k = MyEntity1Key.builder().setId(UUID.randomUUID()).build();
        MyEntity1 r = MyEntity1.builder().setF1(239).set$key(k).setF2("abc").build();

        String json = Alfa.jsonCodec().toJsonString(r);

        System.out.println(json);
        MyEntity1 decoded = Alfa.jsonCodec().fromJsonString(json);

        Assert.assertEquals(r, decoded);
    }
}
