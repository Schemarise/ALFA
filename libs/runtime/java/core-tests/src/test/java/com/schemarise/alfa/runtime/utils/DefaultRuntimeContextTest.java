package com.schemarise.alfa.runtime.utils;

import com.schemarise.alfa.runtime.AlfaObject;
import com.schemarise.alfa.runtime.RuntimeContext;
import com.schemarise.alfa.runtime_int.IntImpl;
import flattentest.EntityObj;
import flattentest.EntityObjKey;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;
import java.util.UUID;

public class DefaultRuntimeContextTest {
    @Test
    public void testKeys() throws Exception {
        RuntimeContext rc = IntImpl.defaultRuntimeContext();

        String str = "hello";
        byte[] enc = rc.encrypt(str.getBytes());
        byte[] dec = rc.decrypt(enc);
        Assert.assertEquals(str, new String(dec));

//        rc.logKeyInfo();
    }


    @Test
    public void testPersistOps() throws Exception {
        RuntimeContext rc = IntImpl.defaultRuntimeContext();

        EntityObj e = EntityObj.builder().
                setStrVal("sd").
                set$key(EntityObjKey.builder().setId(UUID.randomUUID()).build()).
                addListVal("A").
                build();

        rc.save(e, Optional.empty());

        Optional<AlfaObject> lk = rc.lookup(EntityObj.EntityObjDescriptor.TYPE_NAME, e.get$key().get(), Optional.empty());
        Assert.assertTrue(lk.isPresent());
    }
}
