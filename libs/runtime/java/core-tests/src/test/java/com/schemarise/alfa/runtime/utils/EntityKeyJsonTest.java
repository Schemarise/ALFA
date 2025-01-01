package com.schemarise.alfa.runtime.utils;

import com.schemarise.alfa.runtime.Alfa;
import com.schemarise.alfa.runtime.AlfaObject;
import com.schemarise.alfa.runtime.JsonCodec;
import com.schemarise.alfa.runtime.codec.json.JsonCodecConfig;
import flattentest.EntityObj;
import flattentest.EntityObjKey;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class EntityKeyJsonTest {
    @Test
    public void EntityKeyTest() throws Exception {
        EntityObjKey k = EntityObjKey.builder().setId(UUID.randomUUID()).build();
        EntityObj e = EntityObj.builder().set$key(k).setStrVal("Hello").addListVal("e").build();

        String j1 = Alfa.jsonCodec().toFormattedJson(e);
        System.out.println(j1);

        JsonCodecConfig cc = JsonCodecConfig.builder().setWriteEntityKeyAsObject(false).build();
        String j2 = Alfa.jsonCodec().toFormattedJson(cc, e);
        System.out.println(j2);


        AlfaObject v1 = Alfa.jsonCodec().fromJsonString(j1);
        AlfaObject v2 = Alfa.jsonCodec().fromJsonString(j2);

        Assert.assertEquals(v1, v2);
    }
}
