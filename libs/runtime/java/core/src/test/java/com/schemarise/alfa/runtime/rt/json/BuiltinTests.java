package com.schemarise.alfa.runtime.json;

import com.schemarise.alfa.runtime.RuntimeContext;
import com.schemarise.alfa.runtime.utils.BuiltinFunctionsImpl;
import org.junit.Test;

import java.util.*;

public class BuiltinTests {
    BuiltinFunctionsImpl n = new BuiltinFunctionsImpl(RuntimeContext.getDefaultRuntimeContext());

    @Test
    public void toStringTest() {
        List<String> l = Arrays.asList(new String[]{"A", "B", "C"});


        System.out.println(n.toString(l));
    }

    @Test
    public void toNestedStringTest() {
        List<List<String>> l = Arrays.asList(new List[]{Arrays.asList(new String[]{"A", "B", "C"}), Arrays.asList(new String[]{"A", "B", "C"}), Arrays.asList(new String[]{"A", "B", "C"})});

        BuiltinFunctionsImpl n = new BuiltinFunctionsImpl(RuntimeContext.getDefaultRuntimeContext());

        System.out.println(n.toString(l));
    }

    @Test
    public void toStringSet() {
        Set<String> l = new HashSet<>();
        l.add("A");
        l.add("B");
        l.add("C");

        BuiltinFunctionsImpl n = new BuiltinFunctionsImpl(RuntimeContext.getDefaultRuntimeContext());

        System.out.println(n.toString(l));
    }

    @Test
    public void toStringMap() {
        Map<String, Integer> l = new HashMap<>();
        l.put("A", 10);
        l.put("B", 20);
        l.put("C", 30);

        BuiltinFunctionsImpl n = new BuiltinFunctionsImpl(RuntimeContext.getDefaultRuntimeContext());

        System.out.println(n.toString(l));
    }
}
