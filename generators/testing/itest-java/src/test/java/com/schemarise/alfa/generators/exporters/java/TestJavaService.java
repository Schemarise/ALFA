package com.schemarise.alfa.generators.exporters.java;

import org.junit.Assert;
import org.junit.Test;
import udts.Data;
import udts.MyService;
import udts.MyTrait;

import java.time.LocalDate;

public class TestJavaService {

    public class FactoryImpl implements MyService.Factory {
        @Override
        public MyService create(String _host, int _port) {
            return new ServiceImpl();
        }
    }

    public class ServiceImpl implements MyService {

        @Override
        public LocalDate getDate() {
            return LocalDate.now();
        }

        @Override
        public void setDate(LocalDate _d) {

        }

        @Override
        public Data getUDT() {
            return Data.builder().setF1(10).setF2("abc").build();
        }

        public void methodWithTrait(MyTrait _d) {
        }
    }

    @Test
    public void testService() {
        MyService srv = new FactoryImpl().create("localhost", 8000);
        LocalDate d = srv.getDate();
        Data udt = srv.getUDT();

        Assert.assertEquals(udt.getF1(), 10);
    }
}
