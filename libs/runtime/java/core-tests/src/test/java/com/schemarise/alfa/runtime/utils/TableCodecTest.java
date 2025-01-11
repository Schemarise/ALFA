package com.schemarise.alfa.runtime.utils;

import com.schemarise.alfa.runtime.*;
import schemarise.alfa.runtime.model.ColBasedTable;
import schemarise.alfa.runtime.model.asserts.ValidationReport;
import com.schemarise.alfa.runtime.codec.CodecConfig;
import com.schemarise.alfa.runtime.codec.Converters;
import com.schemarise.alfa.runtime.codec.table.TableCodec;
import schemarise.alfa.runtime.model.ScalarDataType;
import com.schemarise.alfa.runtime_int.table.Table;
import flattentest.*;
import com.schemarise.alfa.utils.testing.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.out;

public class TableCodecTest {

    private Table testSimpleObjTable;
    private Table testSimpleObjTryFailTable;

    @Test
    public void testDecodePlainObjStream() {
        List<String> cols = Arrays.asList(
                "StrVal",
                "IntVal",
                "OptVal",
                "EnVal");

        Stream data = Arrays.asList(Arrays.asList(
                "Bob",
                "200000",
                "2",
                "R")).stream();

        Stream<AlfaObject> stream = TableCodec.importRowBasedObjects(CodecConfig.defaultCodecConfig(), "flattentest.PlainObj", Optional.of(cols),
                data, Collections.emptyMap());

        List<AlfaObject> list = stream.collect(Collectors.toList());

        Assert.assertEquals(list.size(), 1);
    }

    @Test
    public void testReadCsvFile() throws Exception {

        AlfaRandomizer r = new AlfaRandomizer();

        CodecConfig cfg = CodecConfig.builder().setAssertListener(new ValidationCollectingListener()).build();

        Path csvPath = Paths.get(TestUtils.getTestResourcesPath(getClass()) + "plain-obj.csv");

        Stream<AlfaObject> res = TableCodec.importCsv(csvPath, TableCodec.CsvReaderConfig.defaultCsvReaderConfig(),
                cfg, PlainObj.PlainObjDescriptor.TYPE_NAME, Optional.empty(), Collections.emptyMap());

        long d = res.count();

        long count = 0;
        for (int i = 0; i < 5; i++) {
            res = TableCodec.importCsv(csvPath, TableCodec.CsvReaderConfig.defaultCsvReaderConfig(),
                    CodecConfig.defaultCodecConfig(), PlainObj.PlainObjDescriptor.TYPE_NAME,
                    Optional.empty(), Collections.emptyMap());
            count = res.count();
        }

        ValidationReport vr = cfg.getAssertListener().getValidationReport().build();

        // expect errors for the wrong enum
        Assert.assertEquals(5001, vr.getTotalErrors());

        // rest should be processed
        Assert.assertEquals(94999, count);
    }

    @Test
    public void testDecodePlainObj() {
        List data = Arrays.asList(
                "Australia and Oceania",
                "Tuvalu",
                "Food",
                "Offline",
                "H",
                "5/28/2010",
                "1669165933",
                "6/27/2010",
                "9925",
                "255.28",
                "159.42",
                "2533654.00",
                "1582243.50",
                "951410.50"
        );


        for (int i = 0; i < 1000; i++) {
            AlfaObject obj = TableCodec.importRowBasedObject(CodecConfig.defaultCodecConfig(), FlatObj.FlatObjDescriptor.TYPE_NAME, Optional.empty(),
                    data, "Line:" + i, Collections.emptyMap());
        }

        long start = System.currentTimeMillis();

        for (int i = 0; i < 1000000; i++) {
            AlfaObject obj = TableCodec.importRowBasedObject(CodecConfig.defaultCodecConfig(), FlatObj.FlatObjDescriptor.TYPE_NAME, Optional.empty(),
                    data, "Line:" + i, Collections.emptyMap());
        }

        long end = System.currentTimeMillis();

        System.out.println((end - start));
        //        System.out.println(obj);
    }


    @Test
    public void testSimpleObj() {
        SimpleObj.SimpleObjBuilder b = SimpleObj.builder();
        b.setEitherVal(AlfaUtils.createEitherLeft("EitherLeftStr1")).setIntVal(20).setStrVal("RandStr").
                setOptVal(Optional.of((short) 1)).setTryVal(AlfaUtils.createTryValue("TryStr")).setEnVal(SampleEnum.B);

        SimpleObj o = b.build();

        Table t = (Table) TableCodec.toTable(o);

        Assert.assertEquals("[StrVal, IntVal, __OptVal_IsSet, OptVal, __TryVal_IsFailure, TryVal, __EitherVal_IsLeft, EitherVal_Left, EnVal]",
                t.getColumnNames().toString());
        Assert.assertEquals("[RandStr, 20, true, 1, false, TryStr, true, EitherLeftStr1, B]", t.getValues(0).toString());

        out.println(t);
        testSimpleObjTable = t;
    }

    @Test
    public void testSimpleObjTryFail() {
        SimpleObj.SimpleObjBuilder b = SimpleObj.builder();
        b.setEitherVal(AlfaUtils.createEitherRight(123.21)).setIntVal(20).setStrVal("RandStr").
                setOptVal(Optional.of((short) 1)).setTryVal(AlfaUtils.createTryFailure("TryFailure")).setEnVal(SampleEnum.B);

        SimpleObj o = b.build();

        Table t = (Table) TableCodec.toTable(o);

        Assert.assertEquals("[StrVal, IntVal, __OptVal_IsSet, OptVal, __TryVal_IsFailure, TryVal_Message, __EitherVal_IsLeft, EitherVal_Right, EnVal]",
                t.getColumnNames().toString());
        Assert.assertEquals("[RandStr, 20, true, 1, true, TryFailure, false, 123.21, B]", t.getValues(0).toString());
        out.println(t);
        testSimpleObjTryFailTable = t;
    }

    @Test
    public void mergeTables() {
        testSimpleObj();
        testSimpleObjTryFail();

        Table t3 = Table.merge(testSimpleObjTable, testSimpleObjTryFailTable);
        out.println(t3);

        Assert.assertEquals("[StrVal, IntVal, __OptVal_IsSet, OptVal, __TryVal_IsFailure, TryVal, __EitherVal_IsLeft, EitherVal_Left, EnVal, TryVal_Message, EitherVal_Right]",
                t3.getColumnNames().toString());
        Assert.assertEquals(t3.getRowCount(), 2);
    }

    @Test
    public void testWithList() {
        WithList.WithListBuilder b = WithList.builder();
        b.setStrVal("Str1");
        b.addListVal(14);
        WithList o = b.build();

        Table t = (Table) TableCodec.toTable(o);
        out.println(t);
        Assert.assertEquals("[StrVal, __ListVal__Id, ListVal]", t.getColumnNames().toString());
        Assert.assertEquals("[Str1, 1, 14]", t.getValues(0).toString());

        b.addListVal(9121);
        b.addListVal(2152);
        b.addListVal(6533);

        o = b.build();

        t = (Table) TableCodec.toTable(o);
        out.println(t);

        Assert.assertEquals("[StrVal, __ListVal__Id, ListVal]", t.getColumnNames().toString());
        Assert.assertEquals("[Str1, 4, 6533]", t.getValues(0).toString());
        Assert.assertEquals("[Str1, 2, 9121]", t.getValues(2).toString());
        Assert.assertEquals(4, t.getRowCount());
    }

    @Test
    public void testWithSet() {
        WithSet.WithSetBuilder b = WithSet.builder();
        b.setStrVal("Str1");
        b.addAllSetVal(new HashSet<>());
        WithSet o = b.build();

        Table t = (Table) TableCodec.toTable(o);
        out.println(t);
        Assert.assertEquals("[StrVal, __SetVal_IsEmpty]", t.getColumnNames().toString());
        Assert.assertEquals("[Str1, true]", t.getValues(0).toString());

        b.addSetVal(9121);
        b.addSetVal(2152);
        b.addSetVal(6533);

        o = b.build();

        t = (Table) TableCodec.toTable(o);
        out.println(t);

        Assert.assertEquals("[StrVal, __SetVal_IsEmpty, __SetVal__Id, SetVal]", t.getColumnNames().toString());
        Assert.assertEquals("[Str1, false, 3, 9121]", t.getValues(0).toString());
        Assert.assertEquals("[Str1, false, 1, 2152]", t.getValues(2).toString());
        Assert.assertEquals(3, t.getRowCount());
    }

    @Test
    public void testWithMap() {
        WithMap.WithMapBuilder b = WithMap.builder();
        b.setStrVal("Str1");
        b.putAllMapVal(new HashMap<>());
        WithMap o = b.build();

        Table t = (Table) TableCodec.toTable(o);
        out.println(t);

        Assert.assertEquals("[StrVal, __MapVal_IsEmpty]", t.getColumnNames().toString());
        Assert.assertEquals("[Str1, true]", t.getValues(0).toString());

        b.putMapVal("a", 9121);
        b.putMapVal("b", 2152);
        b.putMapVal("c", 6533);

        o = b.build();

        t = (Table) TableCodec.toTable(o);
        out.println(t);

        Assert.assertEquals("[StrVal, __MapVal_IsEmpty, MapVal_Key, MapVal_Value]", t.getColumnNames().toString());
        Assert.assertEquals("[Str1, false, a, 9121]", t.getValues(0).toString());
        Assert.assertEquals("[Str1, false, c, 6533]", t.getValues(2).toString());
        Assert.assertEquals(3, t.getRowCount());
    }

    @Test
    public void testWithListOfSmallRec() {
        WithListOfSmallRec.WithListOfSmallRecBuilder b = WithListOfSmallRec.builder();
        b.setStrVal("Str1");
        b.addAllListVal(new ArrayList<SmallRec>());
        WithListOfSmallRec o = b.build();

        Table t = (Table) TableCodec.toTable(o);
        out.println(t);
        Assert.assertEquals("[StrVal, __ListVal_IsEmpty]", t.getColumnNames().toString());
        Assert.assertEquals("[Str1, true]", t.getValues(0).toString());

        b.addListVal(SmallRec.builder().setIntVal(10).setStrVal("a").build());
        b.addListVal(SmallRec.builder().setIntVal(20).setStrVal("b").build());
        b.addListVal(SmallRec.builder().setIntVal(30).setStrVal("c").build());

        o = b.build();

        t = (Table) TableCodec.toTable(o);
        out.println(t);

        Assert.assertEquals("[StrVal, __ListVal_IsEmpty, __ListVal__Id, ListVal_StrVal, ListVal_IntVal]", t.getColumnNames().toString());
        Assert.assertEquals("[Str1, false, 3, c, 30]", t.getValues(0).toString());
        Assert.assertEquals("[Str1, false, 1, a, 10]", t.getValues(2).toString());
        Assert.assertEquals(3, t.getRowCount());
    }

    @Test
    public void testWithMapOfRecs() {
        WithMapOfSmallRec.WithMapOfSmallRecBuilder b = WithMapOfSmallRec.builder();
        b.setStrVal("Str1");
        b.putAllRecMapsVal(new HashMap<>());
        WithMapOfSmallRec o = b.build();

        Table t = (Table) TableCodec.toTable(o);
        out.println(t);

        Assert.assertEquals("[StrVal, __RecMapsVal_IsEmpty]", t.getColumnNames().toString());
        Assert.assertEquals("[Str1, true]", t.getValues(0).toString());

        b.putRecMapsVal("ak", SmallRec.builder().setIntVal(10).setStrVal("a").build());
        b.putRecMapsVal("bk", SmallRec.builder().setIntVal(20).setStrVal("b").build());
        b.putRecMapsVal("ck", SmallRec.builder().setIntVal(30).setStrVal("c").build());

        o = b.build();

        t = (Table) TableCodec.toTable(o);
        out.println(t);

        Assert.assertEquals("[StrVal, __RecMapsVal_IsEmpty, RecMapsVal_K, RecMapsVal_V_StrVal, RecMapsVal_V_IntVal]", t.getColumnNames().toString());
        Assert.assertEquals("[Str1, false, ak, a, 10]", t.getValues(0).toString());
        Assert.assertEquals("[Str1, false, ck, c, 30]", t.getValues(2).toString());
        Assert.assertEquals(3, t.getRowCount());
    }


    @Test
    public void testWithMapWithCompositeKey() {
        WithCompositeMapKey.WithCompositeMapKeyBuilder b = WithCompositeMapKey.builder();
        b.setStrVal("Str1");
        b.putAllMoL(new HashMap<>());
        WithCompositeMapKey o = b.build();

        Table t = (Table) TableCodec.toTable(o);
        out.println(t);

        Assert.assertEquals("[StrVal, __MoL_IsEmpty]", t.getColumnNames().toString());
        Assert.assertEquals("[Str1, true]", t.getValues(0).toString());

        b.putMoL(SmallRec.builder().setIntVal(10).setStrVal("a").build(), "x");
        b.putMoL(SmallRec.builder().setIntVal(20).setStrVal("b").build(), "y");
        b.putMoL(SmallRec.builder().setIntVal(30).setStrVal("c").build(), "z");

        o = b.build();

        t = (Table) TableCodec.toTable(o);
        out.println(t);

        Assert.assertEquals("[StrVal, __MoL_IsEmpty, MoL_CK_StrVal, MoL_CK_IntVal, MoL_Value]", t.getColumnNames().toString());
        Assert.assertEquals("[Str1, false, a, 10, x]", t.getValues(0).toString());
        Assert.assertEquals("[Str1, false, c, 30, z]", t.getValues(2).toString());

        Assert.assertEquals(3, t.getRowCount());
    }

    @Test
    public void testWithMapOfLists() {
        WithMapOfLists.WithMapOfListsBuilder b = WithMapOfLists.builder();
        b.setStrVal("Str1");
        b.putAllMoL(new HashMap<>());
        WithMapOfLists o = b.build();

        Table t = (Table) TableCodec.toTable(o);
        out.println(t);

        Assert.assertEquals("[StrVal, __MoL_IsEmpty]", t.getColumnNames().toString());
        Assert.assertEquals("[Str1, true]", t.getValues(0).toString());

        b.putMoL(100, Arrays.asList("p", "q", "r"));
        b.putMoL(200, Arrays.asList("a", "b"));
        b.putMoL(300, Arrays.asList("w", "x", "y", "z"));

        o = b.build();

        t = (Table) TableCodec.toTable(o);
        out.println(t);

        Assert.assertEquals("[StrVal, __MoL_IsEmpty, MoL_Key, __MoL_Value_IsEmpty_L1, __MoL_Value__Id_L1, MoL_Value]", t.getColumnNames().toString());
        Assert.assertEquals("[Str1, false, 100, false, 3, r]", t.getValues(0).toString());
        Assert.assertEquals("[Str1, false, 300, false, 1, w]", t.getValues(8).toString());

        Assert.assertEquals(9, t.getRowCount());
    }

    @Test
    public void testWithMapAndList() {
        WithMapAndList.WithMapAndListBuilder b = WithMapAndList.builder();
        b.setStrVal("Str1");
        b.setIntVal(99);
        b.putAllMapVal(new HashMap<>());
        b.addAllListVal(new ArrayList<>());
        WithMapAndList o = b.build();

        Table t = (Table) TableCodec.toTable(o);
        out.println(t);

        Assert.assertEquals(2, t.getRowCount());
        Assert.assertEquals("[StrVal, IntVal, __Dimension, __MapVal_IsEmpty, __ListVal_IsEmpty]", t.getColumnNames().toString());
        Assert.assertEquals("[Str1, 99, MapVal, true]", t.getValues(0).toString());
        Assert.assertEquals("[Str1, 99, ListVal, true]", t.getValues(1).toString());

        b.putMapVal("x", 10);
        b.putMapVal("y", 11);
        b.putMapVal("z", 12);
        b.addListVal("a");
        b.addListVal("b");
        b.addListVal("c");

        o = b.build();

        t = (Table) TableCodec.toTable(o);
        out.println(t);

        Assert.assertEquals("[StrVal, IntVal, __Dimension, __MapVal_IsEmpty, MapVal_Key, MapVal_Value, __ListVal_IsEmpty, __ListVal__Id, ListVal]", t.getColumnNames().toString());
        Assert.assertEquals("[Str1, 99, MapVal, false, x, 10]", t.getValues(0).toString());
        Assert.assertEquals("[Str1, 99, false, ListVal, 1, a]", t.getValues(5).toString());

        Assert.assertEquals(6, t.getRowCount());
    }

    @Test
    public void testWithRandom() {
        AlfaRandomizer r = new AlfaRandomizer();
        AlfaObject o = r.random("flattentest.WithMapAndList");
        Table t = (Table) TableCodec.toTable(o);
        ScalarDataType ct = t.getColumnType("MapVal_Value");
        Assert.assertEquals(Converters.DataTypeInt, ct);
        out.println(t);
    }

    @Test
    public void testWithEntity() {
        EntityObj.EntityObjBuilder b = EntityObj.builder();
        b.setStrVal("Str1");
        b.addAllListVal(new ArrayList<>());
        b.set$key(EntityObjKey.builder().setId(UUID.fromString("378eaf7c-24dc-482c-af9e-e59ea81c9591")).build());
        EntityObj o = b.build();

        Table t = (Table) TableCodec.toTable(o);
        out.println(t);

        Assert.assertEquals(1, t.getRowCount());
        Assert.assertEquals("[id, StrVal, __ListVal_IsEmpty]", t.getColumnNames().toString());
        Assert.assertEquals("[378eaf7c-24dc-482c-af9e-e59ea81c9591, Str1, true]", t.getValues(0).toString());

        b.addListVal("a");
        b.addListVal("b");
        b.addListVal("c");

        o = b.build();

        t = (Table) TableCodec.toTable(o);
        out.println(t);

        Assert.assertEquals("[id, StrVal, __ListVal_IsEmpty, __ListVal__Id, ListVal]", t.getColumnNames().toString());
        Assert.assertEquals("[378eaf7c-24dc-482c-af9e-e59ea81c9591, Str1, false, 3, c]", t.getValues(0).toString());
        Assert.assertEquals("[378eaf7c-24dc-482c-af9e-e59ea81c9591, Str1, false, 1, a]", t.getValues(2).toString());

        Assert.assertEquals(3, t.getRowCount());
    }

    @Test
    public void testUnionObj() {
        UnionObj.UnionObjBuilder b = UnionObj.builder();
        b.setUdtVal(SmallRec.builder().setIntVal(10).setStrVal("x").build());
        UnionObj o = b.build();

        Table t = (Table) TableCodec.toTable(o);
        out.println(t);

        Assert.assertEquals(1, t.getRowCount());
        Assert.assertEquals("[__Case, UdtVal_StrVal, UdtVal_IntVal]", t.getColumnNames().toString());
        Assert.assertEquals("[UdtVal, x, 10]", t.getValues(0).toString());
    }

    @Test
    public void testRecOfUnions() throws IOException, SQLException {
        RecOfUnions.RecOfUnionsBuilder b = RecOfUnions.builder();
        b.setStrVal("abc");
        b.addUnis(UnionObj.builder().setStrVal("us1").build());
        b.addUnis(UnionObj.builder().setStrVal("us2").build());
        b.addUnis(UnionObj.builder().setUdtVal(SmallRec.builder().setIntVal(10).setStrVal("so1").build()).build());
        b.addUnis(UnionObj.builder().setUdtVal(SmallRec.builder().setIntVal(15).setStrVal("so2").build()).build());
        RecOfUnions o = b.build();

        Table t = (Table) TableCodec.toTable(o);
        out.println(t);

        Assert.assertEquals(4, t.getRowCount());
        Assert.assertEquals("[StrVal, __Unis_IsEmpty, __Unis__Id, Unis__Case, Unis_UdtVal_StrVal, Unis_UdtVal_IntVal, Unis_StrVal]", t.getColumnNames().toString());
        Assert.assertEquals("[abc, false, 4, UdtVal, so2, 15]", t.getValues(0).toString());


        ColBasedTable cbt = t.getColBasedTable();
        String json = Alfa.jsonCodec().toFormattedJson(cbt);

        ResultSet rs = t.getResultSet();
        ResultSetMetaData md = rs.getMetaData();

        Assert.assertEquals(7, md.getColumnCount());

        while (rs.next()) {
            Assert.assertEquals(rs.getString("StrVal"), "abc");
        }

//        out.println(json);


//        AlfaRandomizer r = new AlfaRandomizer();
//        AlfaObject ro = r.random("flattentest.RecOfUnions");
//        ITable t1 = f1.flatten();
//        out.println(t1);
    }

    @Test
    public void testWithTrait() throws IOException {
        WithTraitRec.WithTraitRecBuilder b = WithTraitRec.builder();

        b.addTVal(SampleTraitImplA.builder().setF1("a").setF2(20).build());
        b.addTVal(SampleTraitImplB.builder().setF1("a").setF3(30).build());

        WithTraitRec o = b.build();

        Table t = (Table) TableCodec.toTable(o);
        out.println(t);

        Assert.assertEquals(2, t.getRowCount());
        Assert.assertEquals("[__TVal_IsEmpty, __TVal__Id, TVal__Type, TVal_F1, TVal_F3, TVal_F2]", t.getColumnNames().toString());
        Assert.assertEquals("[false, 2, flattentest.SampleTraitImplB, a, 30]", t.getValues(0).toString());
        Assert.assertEquals("[false, 1, flattentest.SampleTraitImplA, a, 20]", t.getValues(1).toString());
    }

    @Test
    public void testNested() throws IOException {
        DeeplyNestedVectors.DeeplyNestedVectorsBuilder b = DeeplyNestedVectors.builder();

        b.addF2(Arrays.asList(1, 2, 3));
        b.addF2(Arrays.asList(4, 5, 6));
        b.addF2(Arrays.asList(7, 8, 9));

        DeeplyNestedVectors o = b.build();

        Table t = (Table) TableCodec.toTable(o);
        out.println(t);

    }
}
