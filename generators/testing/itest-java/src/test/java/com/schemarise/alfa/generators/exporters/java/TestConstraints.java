package com.schemarise.alfa.generators.exporters.java;

import com.schemarise.alfa.runtime.AlfaObject;
import com.schemarise.alfa.runtime.BuilderConfig;
import com.schemarise.alfa.runtime.IBuilderConfig;
import com.schemarise.alfa.runtime.utils.AlfaRandomizer;
import org.junit.Assert;
import org.junit.Test;
import scalars.constraints.lower.Date;
import scalars.constraints.range.Integer;
import vectors.list.ListOfLists;
import vectors.map.MapOfLists;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestConstraints {
    private AlfaRandomizer rand = new AlfaRandomizer();

    @Test
    public void testIntConstraints() {
        IBuilderConfig cfg = BuilderConfig.builder().build();

        Integer.IntegerBuilder b = Integer.builder(cfg).setFRange(2000);
        b.build();

        Assert.assertTrue(cfg.getAssertListener().getValidationReport().getAlerts().size() > 0);
    }

    @Test
    public void testDateConstraints() {
        IBuilderConfig cfg = BuilderConfig.builder().build();
        Date.DateBuilder b = Date.builder(cfg).setFLowerBound(LocalDate.of(1200, 1, 1));
        b.build();
        Assert.assertTrue(cfg.getAssertListener().getValidationReport().getAlerts().size() > 0);
    }

    @Test
    public void testFixedSizeConstraints() {
        AlfaObject d = rand.random("vectors.list.constraints.range.List");
        System.out.println(d);
    }


    @Test(expected = UnsupportedOperationException.class)
    public void testImmutableMap() {

        Map<String, List<java.lang.Integer>> m = new HashMap<>();
        List<java.lang.Integer> l = new ArrayList<java.lang.Integer>();
        m.put("a", l);
        l.add(10);
        MapOfLists lom = MapOfLists.builder().putAllF1(m).build();

        l.add(200);

        // add 200 is not in the list
        Assert.assertEquals(1, lom.getF1().get("a").size());

        // this will throw as the list is now immutable
        lom.getF1().get("a").add(22);
    }


    @Test
    public void testImmutableList() {
        List<List<java.lang.Integer>> l = new ArrayList();

        List<java.lang.Integer> a1 = new ArrayList();
        List<java.lang.Integer> a2 = new ArrayList();
        List<java.lang.Integer> a3 = new ArrayList();

        a1.add(10);
        a1.add(12);
        a1.add(12);

        a2.add(20);
        a2.add(20);
        a2.add(50);

        a3.add(33);

        l.add(a1);
        l.add(a2);
        l.add(a3);

        ListOfLists.ListOfListsBuilder v = ListOfLists.builder().addAllF1(l);
        ListOfLists d = v.build();

        a3.add(999);

        // list in object should have been cloned, therefore a3 still has only 1 element
        Assert.assertEquals(d.getF1().get(2).size(), 1);

    }
}
