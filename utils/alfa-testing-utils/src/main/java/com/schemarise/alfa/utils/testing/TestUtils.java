package com.schemarise.alfa.utils.testing;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class TestUtils {
    public static String getLocalProjectRootPath(Class c) {
        try {
            return new File(c.getResource("/").getPath() + "/../../").getCanonicalPath() + "/";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getTestResourcesPath(Class c) {
        try {
            return new File(c.getResource("/").getPath() + "/../../src/test/resources/").getCanonicalPath() + "/";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
