package com.schemarise.alfa.generators.exporters.java;

import Feature.NativeUsage;
import acme.types.Storage;
import com.schemarise.alfa.runtime.Alfa;
import com.schemarise.alfa.runtime.AlfaObject;
import com.schemarise.alfa.runtime.BuilderConfig;
import com.schemarise.alfa.runtime.Encrypted;
import enclosed.CompressedScalarData;
import enclosed.EncryptedData;
import enclosed.SensitiveData;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;

public class FeatureTests {
    @Test
    public void testNative() throws IOException {
        Storage cap = new Storage("5MB");
        NativeUsage nu = NativeUsage.builder().setCapacity(cap).build();
        String json = Alfa.jsonCodec().toJsonString(nu);
        System.out.println(json);
        AlfaObject decoded = Alfa.jsonCodec().fromJsonString(json);

        System.out.println(decoded);

    }

    @Test
    public void testCompress() throws IOException {
        CompressedScalarData.CompressedScalarDataBuilder cb = CompressedScalarData.builder();
        cb.setF1("hello");
        cb.setF2(21.12);
        String j = Alfa.jsonCodec().toJsonString(cb.build());
        System.out.println(j);
    }

    @Test
    public void testEncrypt() throws IOException {
        EncryptedData.EncryptedDataBuilder b = EncryptedData.builder();
        b.setF1("ABC");

        SensitiveData.SensitiveDataBuilder b2 = SensitiveData.builder();
        b2.setCreditCard("1234 5678 1234 5678");
        b2.setDOB(LocalDate.now());
        b.setF2(b2.build());

        EncryptedData ed = b.build();
        String json = Alfa.jsonCodec().toJsonString(ed);

        EncryptedData decoded = Alfa.jsonCodec().fromJsonString(json);

        Encrypted<String> es = decoded.getF1();
        String s = es.getValue(BuilderConfig.getInstance());

        System.out.println(s);

    }
}
