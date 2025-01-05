package com.schemarise.alfa.runtime_int;

import com.schemarise.alfa.runtime.IBuiltinFunctions;
import com.schemarise.alfa.runtime.utils.BuiltinFunctionsImpl;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BuiltinFuncsTest {
    private IBuiltinFunctions f = new BuiltinFunctionsImpl(null);

    @Test
    public void percentileTest() {
        List<Double> l = new ArrayList<>();


        l.add(7708059.27);
        l.add(31878420.73);
        l.add(41336778.21);
        l.add(50799399.10);
        l.add(51056993.34);
        l.add(101650992.46);
        l.add(101650992.46);
        l.add(106771968.45);

        Double r = f.percentile(l, 10);
        System.out.println(r);
    }

}
