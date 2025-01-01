package com.schemarise.alfa.generators.exporters.java;

import com.schemarise.alfa.runtime.Alfa;
import com.schemarise.alfa.runtime.AlfaObject;
import com.schemarise.alfa.runtime.Union;
import com.schemarise.alfa.runtime.codec.table.TableCodec;
import com.schemarise.alfa.runtime.utils.AlfaRandomizer;
import com.schemarise.alfa.runtime_int.table.Table;
import org.apache.commons.io.IOUtils;
import schemarise.alfa.runtime.model.ColBasedTable;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * To be used for generating test data
 */
public class TestJsonGenerator {
    static String path = "generators/testing/test-material/src/jsons/default/";
    static String tblpath = "generators/testing/test-material/src/table-jsons/default/";

    public static void main(String[] args) throws Exception {

        List<String> allTypes = getTypes();

        AlfaRandomizer r = new AlfaRandomizer(allTypes);


        allTypes.forEach(t -> {

            if (!t.startsWith("generics") &&
                    !t.trim().startsWith("AllBuiltins") &&
                    !t.startsWith("LibTest")) {
                genSample(r, t);

            } else {
                System.out.println("  Skipping " + t);
            }
        });

    }

    private static void genSample(AlfaRandomizer r, String t) {
        try {
            System.out.println("Randomising " + t);

            if ( t.equals("Feature.DeeplyNestedTrait.A")) {
                int s = 2;
            }
            AlfaObject obj = r.random(t);
            if (obj instanceof com.schemarise.alfa.runtime.Enum || (obj instanceof Union) && !((Union) obj).isTagged()) {
                System.out.println("Skip " + obj.descriptor().getUdtDataType() + " " + t);
            } else {
                String json = Alfa.jsonCodec().toFormattedJson(obj);
                System.out.println("  Generated " + t);

                IOUtils.write(json, new FileOutputStream(path + t + ".json"), Charset.defaultCharset());

                Table tbl = (Table) TableCodec.toTable(obj);

                ColBasedTable cbt = tbl.getColBasedTable();
                String tblJson = Alfa.jsonCodec().toFormattedJson(cbt);
                IOUtils.write(tblJson, new FileOutputStream(tblpath + t + ".json"), Charset.defaultCharset());
            }

        } catch (UnsupportedOperationException | IOException e) {
            System.out.println("Skipping - Failed to generate " + t);
        } catch (Throwable e) {
            System.out.println("Fatal error " + t);
        }
    }

    public static List<String> getTypes() throws Exception {
        String fileToBeExtracted = ".alfa-meta/zip-index.txt";
        String zipPackage = "generators/testing/itest-java/target/alfa/alfa-gen-exp-itest-java.zip";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream fileInputStream = new FileInputStream(zipPackage);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ZipInputStream zin = new ZipInputStream(bufferedInputStream);

        ZipEntry ze;

        while ((ze = zin.getNextEntry()) != null) {
            if (ze.getName().equals(fileToBeExtracted)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = zin.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.close();
                break;
            }
        }
        zin.close();

        return Arrays.asList(new String(out.toByteArray()).split("\n"));

    }

}
