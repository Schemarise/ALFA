package com.schemarise.alfa.runtime.utils;

import com.schemarise.alfa.runtime.*;
import com.schemarise.alfa.runtime_int.IntImpl;

import java.util.*;

public class SizeEstimator extends NoOpDataConsumer {
    private IntImpl.SizeEstimatorIfc d;

    public SizeEstimator(Set<String> strings, AlfaObject so) {
        d = IntImpl.sizeEstimateGenerator(strings, so);
        d.consume(so.descriptor().getUdtDataType(), so);
    }

    public SizeEstimator(AlfaObject so) {
        d = IntImpl.sizeEstimateGenerator(so);
        d.consume(so.descriptor().getUdtDataType(), so);
    }

    public long getEstimatedSize() {
        return d.getEstimatedSize();
    }
}
