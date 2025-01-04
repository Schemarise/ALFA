package com.schemarise.alfa.runtime.json;


//import Demo.League;

import com.schemarise.alfa.runtime.Alfa;
import com.schemarise.alfa.runtime.AlfaObject;
import com.schemarise.alfa.runtime.codec.json.JsonCodecConfig;
import schemarise.alfa.runtime.model.ScalarDataType;
import schemarise.alfa.runtime.model.ScalarType;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * For AVRO use https://github.com/FasterXML/jackson-dataformats-binary/tree/master/avro
 */
public class JsonTest {

    @Test
    public void dateFormatTest() {
        String fmt = "M/dd/yyyy";
        DateTimeFormatter df = DateTimeFormatter.ofPattern(fmt);
        LocalDate ld = LocalDate.parse("3/26/2012", df);
    }

    @Test
    public void testJsonBinSmile() throws IOException {
        SmileFactory f = new SmileFactory();
        JsonCodecConfig cfg = JsonCodecConfig.builder().setJsonFactory(f).build();
        ScalarDataType alfaObj = ScalarDataType.builder().setScalarType(ScalarType.intType).build();

        OutputStream encoded = Alfa.jsonCodec().exportObj(cfg, alfaObj);

        InputStream is = new ByteArrayInputStream(((ByteArrayOutputStream) encoded).toByteArray());
        JsonCodecConfig icfg = JsonCodecConfig.builder().setJsonFactory(f).build();

        AlfaObject smileDecoded = Alfa.jsonCodec().importObj(icfg, is);

        Assert.assertEquals(alfaObj, smileDecoded);

    }

//    UUID id1 = UUID.randomUUID();
//    UUID id2 = UUID.randomUUID();
//
//    @Test
//    @Ignore
//    public void testSCalar() throws IOException {
//////        ScalarDataType sdt = ScalarDataType.newBuilder().setScalarType(ScalarType.stringType).
//////                setValueRange(Optional.of(ValueRange.newBuilder().setIntRange(Range.newBuilder().setFrom(10).setTo(3299).build()).build())).build();
////
////        ValueRange sdt = ValueRange.newBuilder().setIntRange(Range.newBuilder().setFrom(10).setTo(3299).build()).build();
////
////        String json = JsonCodec.exportObj(sdt);
////        System.out.println(json);
//
//        RecOfBox rob = RecOfBox.newBuilder().setBoxed(Boxed.newBuilder().setValue(10).build()).build();
//        String json = JsonCodec.exportObj(rob);
//        System.out.println(json);
//    }
//
//    @Test
//    @Ignore
//    public void randomTest() {
//        AlfaRandomizer r = new AlfaRandomizer();
//        AlfaObject o = r.random("alfa.rt.model.ScalarDataType");
//        System.out.println(o);
//    }
//
//    @Test
//    public void basicJsonTests() throws Exception {
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < 10_000; i++) {
//            basicJsonTest();
//        }
//        long end = System.currentTimeMillis();
//
//        System.out.println("time:" + ( end - start ));
//    }
//
//    @Test
//    public void basicJsonTest() throws Exception {
//
//        ArrayList<Double> l = new ArrayList<Double>(3);
//        l.add(1.1);
//        l.add(1.2);
//        l.add(1.3);
//
//        Demo.SampleRecord v = Demo.SampleRecord.newBuilder(BuilderConfig.getInstance()).
//                setIntField(123).
//                setCheckField(Try.newBuilder().setResult((34.32)).build()).
//                setEncryptedField(390.123).
//                setCompressedField(DefaultCompressed.fromValue( SampleRecord.descriptor.compressedFieldMeta, 190238109.123)).
//                setStringField(Optional.of("abc")).
//                putMapField(LocalDate.MIN, 123920348139L).
//                putMapField(LocalDate.now(), 23434234L).
//                addSetField(id1).
//                addSetField(id2).
//                addMatrix( l ).
//                build();
//
//
//        String json = JsonCodec.exportObj( v );
//
////        System.out.println(json);
//
//        JsonCodecConfig cc = JsonCodecConfig.newBuilder().setAssignableToClass(SampleRecord.class).build();
//        SampleRecord decoded = JsonCodec.importObj(cc, json);
//    }
//
//
//    @Test
//    public void toBuilderTest() throws Exception {
//        AlfaRandomizer r = new AlfaRandomizer();
//        SimpleRecord o = r.random("Demo.SimpleRecord");
//        SimpleRecord.$Builder b = o.toBuilder();
//
//        b.setIntField(555);
//        SimpleRecord o2 = b.build();
//
//        System.out.println(o);
//        System.out.println(o2);
//    }
//
//    @Test
//    public void testNoDollarType() throws Exception {
//
//        String json = "{\n";
//        json += "\"F1\" : \"ABC\"\n";
//        json += "}\n";
//
//        JsonCodecConfig cc = JsonCodecConfig.newBuilder().setAssignableToClass(SampleNested.class).build();
//
//        SampleNested sn = JsonCodec.importObj(cc, json);
//
//        System.out.println(sn);
//
//
//        AlfaRandomizer r = new AlfaRandomizer();
//        AlfaObject nsted = r.random(SampleTopLevel.class.getName());
//        System.out.println( JsonCodec.exportObj(nsted));
//
////        json = "{\"@type\":\"Demo.SampleTopLevel\",\"F1\":{\"@type\":\"Demo.SampleExtendedRecord\",\"F1\":\"olqqt\",\"F2\":123},\"F2\":[]}";
////        SampleTopLevel tl = JsonCodec.importObj(json);
////        System.out.println(tl);
//    }
}

