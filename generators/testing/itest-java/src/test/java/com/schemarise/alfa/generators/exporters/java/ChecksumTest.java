package com.schemarise.alfa.generators.exporters.java;

import Feature.DeeplyNestedTrait.*;
import org.junit.Assert;
import org.junit.Test;

public class ChecksumTest {
    @Test
    public void testNestedTraitChecksum() {
        A.ADescriptor desc = A.ADescriptor.INSTANCE;
        String cs = desc.getChecksum();

        Assert.assertTrue(desc.hasAbstractTypeFieldsInClosure());

    }
}
