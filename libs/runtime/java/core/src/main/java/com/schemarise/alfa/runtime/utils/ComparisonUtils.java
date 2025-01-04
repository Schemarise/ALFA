package com.schemarise.alfa.runtime.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComparisonUtils {
//    public static void main(String... args) {
//        csharp();
//    }


    private static void csharp() {
//        String[] boxed = new String[]{"Integer", "Long", "Short", "Double", "Float"};
        String[] unboxed = new String[]{"int", "long", "double"};

        for (int i = 0; i < unboxed.length; i++) {
            p(
                    "    public static bool lessThan(" + unboxed[i] + " l, " + unboxed[i] + " r) {\n" +
                            "        return l < r;\n" +
                            "    }\n");
            p(
                    "    public static bool lessThanEqualTo(" + unboxed[i] + " l, " + unboxed[i] + " r) {\n" +
                            "        return l <= r;\n" +
                            "    }\n");

            p(
                    "    public static bool greaterThan(" + unboxed[i] + " l, " + unboxed[i] + " r) {\n" +
                            "        return l > r;\n" +
                            "    }\n");

            p(
                    "    public static bool greaterThanEqualTo(" + unboxed[i] + " l, " + unboxed[i] + " r) {\n" +
                            "        return l >= r;\n" +
                            "    }\n");

            p("    public static int compareTo(" + unboxed[i] + " l, " + unboxed[i] + " r) {\n" +
                    "        return l > r ? 1 : l < r ? -1 : 0;\n" +
                    "    }\n");
        }

        String[] objected = new String[]{"NodaTime.LocalDate", "NodaTime.LocalDateTime", "NodaTime.LocalTime"};

        for (int i = 0; i < objected.length; i++) {
            p("// ------------------------------ " + objected[i]);

            p("    public static bool lessThan(" + objected[i] + " l, " + objected[i] + " r) {\n" +
                    "        int c = l.CompareTo( r );\n" +
                    "        return c < 0;\n" +
                    "    }\n");
            p("    public static bool lessThanEqualTo(" + objected[i] + " l, " + objected[i] + " r) {\n" +
                    "        int c = l.CompareTo( r );\n" +
                    "        return c <= 0;\n" +
                    "    }\n");

            p("    public static bool greaterThan(" + objected[i] + " l, " + objected[i] + " r) {\n" +
                    "        int c = l.CompareTo( r );\n" +
                    "        return c > 0;\n" +
                    "    }\n");

            p("    public static bool greaterThanEqualTo(" + objected[i] + " l, " + objected[i] + " r) {\n" +
                    "        int c = l.CompareTo( r );\n" +
                    "        return c >= 0;\n" +
                    "    }\n");

            p("    public static int compareTo(" + objected[i] + " l, " + objected[i] + " r) {\n" +
                    "        int c = l.CompareTo( r );\n" +
                    "        return c;\n" +
                    "    }\n");

        }
    }

    private static void java() {
        String[] boxed = new String[]{"Integer", "Long", "Short", "Double", "Float"};
        String[] unboxed = new String[]{"int", "long", "short", "double", "float"};

        for (int i = 0; i < boxed.length; i++) {
            p("    public static boolean lessThan(" + boxed[i] + " l, " + boxed[i] + " r) {\n" +
                    "        return lessThan(l." + unboxed[i] + "Value(), r." + unboxed[i] + "Value());\n" +
                    "    }\n" +
                    "\n" +
                    "    private static boolean lessThan(" + unboxed[i] + " l, " + unboxed[i] + " r) {\n" +
                    "        return l < r;\n" +
                    "    }\n");
            p("    public static boolean lessThanEqualTo(" + boxed[i] + " l, " + boxed[i] + " r) {\n" +
                    "        return lessThanEqualTo(l." + unboxed[i] + "Value(), r." + unboxed[i] + "Value());\n" +
                    "    }\n" +
                    "\n" +
                    "    private static boolean lessThanEqualTo(" + unboxed[i] + " l, " + unboxed[i] + " r) {\n" +
                    "        return l <= r;\n" +
                    "    }\n");

            p("    public static boolean greaterThan(" + boxed[i] + " l, " + boxed[i] + " r) {\n" +
                    "        return greaterThan(l." + unboxed[i] + "Value(), r." + unboxed[i] + "Value());\n" +
                    "    }\n" +
                    "\n" +
                    "    private static boolean greaterThan(" + unboxed[i] + " l, " + unboxed[i] + " r) {\n" +
                    "        return l > r;\n" +
                    "    }\n");

            p("    public static boolean greaterThanEqualTo(" + boxed[i] + " l, " + boxed[i] + " r) {\n" +
                    "        return greaterThanEqualTo(l." + unboxed[i] + "Value(), r." + unboxed[i] + "Value());\n" +
                    "    }\n" +
                    "\n" +
                    "    private static boolean greaterThanEqualTo(" + unboxed[i] + " l, " + unboxed[i] + " r) {\n" +
                    "        return l >= r;\n" +
                    "    }\n");

            p("    public static int compareTo(" + unboxed[i] + " l, " + unboxed[i] + " r) {\n" +
                    "        return l > r ? 1 : l < r ? -1 : 0;\n" +
                    "    }\n");
        }

        String[] objected = new String[]{"java.time.LocalDate", "java.time.LocalTime", "java.time.LocalDateTime"};

        for (int i = 0; i < objected.length; i++) {
            p("// ------------------------------ " + objected[i]);

            p("    public static boolean lessThan(" + objected[i] + " l, " + objected[i] + " r) {\n" +
                    "        int c = l.compareTo( r );\n" +
                    "        return c < 0;\n" +
                    "    }\n");
            p("    public static boolean lessThanEqualTo(" + objected[i] + " l, " + objected[i] + " r) {\n" +
                    "        int c = l.compareTo( r );\n" +
                    "        return c <= 0;\n" +
                    "    }\n");

            p("    public static boolean greaterThan(" + objected[i] + " l, " + objected[i] + " r) {\n" +
                    "        int c = l.compareTo( r );\n" +
                    "        return c > 0;\n" +
                    "    }\n");

            p("    public static boolean greaterThanEqualTo(" + objected[i] + " l, " + objected[i] + " r) {\n" +
                    "        int c = l.compareTo( r );\n" +
                    "        return c >= 0;\n" +
                    "    }\n");

            p("    public static int compareTo(" + objected[i] + " l, " + objected[i] + " r) {\n" +
                    "        int c = l.compareTo( r );\n" +
                    "        return c;\n" +
                    "    }\n");

        }
    }

    private static void p(String s) {
        System.out.println(s);
    }

    public static int compareTo(Comparable l, Comparable r) {
        return l.compareTo(r);
    }

    public static boolean lessThan(Number l, Number r) {
        return lessThan(l.doubleValue(), r.doubleValue());
    }

    public static boolean lessThanEqualTo(Number l, Number r) {
        return lessThanEqualTo(l.doubleValue(), r.doubleValue());
    }

    public static boolean greaterThan(Number l, Number r) {
        return greaterThan(l.doubleValue(), r.doubleValue());
    }

    public static boolean greaterThanEqualTo(Number l, Number r) {
        return greaterThanEqualTo(l.doubleValue(), r.doubleValue());
    }

    // ---------------

    public static boolean lessThan(Integer l, Integer r) {
        return lessThan(l.intValue(), r.intValue());
    }

    private static boolean lessThan(int l, int r) {
        return l < r;
    }

    public static boolean lessThanEqualTo(Integer l, Integer r) {
        return lessThanEqualTo(l.intValue(), r.intValue());
    }

    private static boolean lessThanEqualTo(int l, int r) {
        return l <= r;
    }

    public static boolean greaterThan(Integer l, Integer r) {
        return greaterThan(l.intValue(), r.intValue());
    }

    private static boolean greaterThan(int l, int r) {
        return l > r;
    }

    public static boolean greaterThanEqualTo(Integer l, Integer r) {
        return greaterThanEqualTo(l.intValue(), r.intValue());
    }

    private static boolean greaterThanEqualTo(int l, int r) {
        return l >= r;
    }

    public static int compareTo(int l, int r) {
        return l > r ? 1 : l < r ? -1 : 0;
    }

    public static boolean lessThan(Long l, Long r) {
        return lessThan(l.longValue(), r.longValue());
    }

    private static boolean lessThan(long l, long r) {
        return l < r;
    }

    public static boolean lessThanEqualTo(Long l, Long r) {
        return lessThanEqualTo(l.longValue(), r.longValue());
    }

    private static boolean lessThanEqualTo(long l, long r) {
        return l <= r;
    }

    public static boolean greaterThan(Long l, Long r) {
        return greaterThan(l.longValue(), r.longValue());
    }

    private static boolean greaterThan(long l, long r) {
        return l > r;
    }

    public static boolean greaterThanEqualTo(Long l, Long r) {
        return greaterThanEqualTo(l.longValue(), r.longValue());
    }

    private static boolean greaterThanEqualTo(long l, long r) {
        return l >= r;
    }

    public static int compareTo(long l, long r) {
        return l > r ? 1 : l < r ? -1 : 0;
    }

    public static boolean lessThan(Short l, Short r) {
        return lessThan(l.shortValue(), r.shortValue());
    }

    private static boolean lessThan(short l, short r) {
        return l < r;
    }

    public static boolean lessThanEqualTo(Short l, Short r) {
        return lessThanEqualTo(l.shortValue(), r.shortValue());
    }

    private static boolean lessThanEqualTo(short l, short r) {
        return l <= r;
    }

    public static boolean greaterThan(Short l, Short r) {
        return greaterThan(l.shortValue(), r.shortValue());
    }

    private static boolean greaterThan(short l, short r) {
        return l > r;
    }

    public static boolean greaterThanEqualTo(Short l, Short r) {
        return greaterThanEqualTo(l.shortValue(), r.shortValue());
    }

    private static boolean greaterThanEqualTo(short l, short r) {
        return l >= r;
    }

    public static int compareTo(short l, short r) {
        return l > r ? 1 : l < r ? -1 : 0;
    }

    public static boolean lessThan(Double l, Double r) {
        return lessThan(l.doubleValue(), r.doubleValue());
    }

    private static boolean lessThan(double l, double r) {
        return l < r;
    }

    public static boolean lessThanEqualTo(Double l, Double r) {
        return lessThanEqualTo(l.doubleValue(), r.doubleValue());
    }

    private static boolean lessThanEqualTo(double l, double r) {
        return l <= r;
    }

    public static boolean greaterThan(Double l, Double r) {
        return greaterThan(l.doubleValue(), r.doubleValue());
    }

    private static boolean greaterThan(double l, double r) {
        return l > r;
    }

    public static boolean greaterThanEqualTo(Double l, Double r) {
        return greaterThanEqualTo(l.doubleValue(), r.doubleValue());
    }

    private static boolean greaterThanEqualTo(double l, double r) {
        return l >= r;
    }

    public static int compareTo(double l, double r) {
        return l > r ? 1 : l < r ? -1 : 0;
    }

    public static boolean lessThan(Float l, Float r) {
        return lessThan(l.floatValue(), r.floatValue());
    }

    private static boolean lessThan(float l, float r) {
        return l < r;
    }

    public static boolean lessThanEqualTo(Float l, Float r) {
        return lessThanEqualTo(l.floatValue(), r.floatValue());
    }

    private static boolean lessThanEqualTo(float l, float r) {
        return l <= r;
    }

    public static boolean greaterThan(Float l, Float r) {
        return greaterThan(l.floatValue(), r.floatValue());
    }

    private static boolean greaterThan(float l, float r) {
        return l > r;
    }

    public static boolean greaterThanEqualTo(Float l, Float r) {
        return greaterThanEqualTo(l.floatValue(), r.floatValue());
    }

    private static boolean greaterThanEqualTo(float l, float r) {
        return l >= r;
    }

    public static int compareTo(float l, float r) {
        return l > r ? 1 : l < r ? -1 : 0;
    }


    // ------------------------------ java.time.LocalDate
    public static boolean lessThan(java.time.LocalDate l, java.time.LocalDate r) {
        int c = l.compareTo(r);
        return c < 0;
    }

    public static boolean lessThanEqualTo(java.time.LocalDate l, java.time.LocalDate r) {
        int c = l.compareTo(r);
        return c <= 0;
    }

    public static boolean greaterThan(java.time.LocalDate l, java.time.LocalDate r) {
        int c = l.compareTo(r);
        return c > 0;
    }

    public static boolean greaterThanEqualTo(java.time.LocalDate l, java.time.LocalDate r) {
        int c = l.compareTo(r);
        return c >= 0;
    }

    public static int compareTo(java.time.LocalDate l, java.time.LocalDate r) {
        int c = l.compareTo(r);
        return c;
    }

    // ------------------------------ java.time.LocalTime
    public static boolean lessThan(java.time.LocalTime l, java.time.LocalTime r) {
        int c = l.compareTo(r);
        return c < 0;
    }

    public static boolean lessThanEqualTo(java.time.LocalTime l, java.time.LocalTime r) {
        int c = l.compareTo(r);
        return c <= 0;
    }

    public static boolean greaterThan(java.time.LocalTime l, java.time.LocalTime r) {
        int c = l.compareTo(r);
        return c > 0;
    }

    public static boolean greaterThanEqualTo(java.time.LocalTime l, java.time.LocalTime r) {
        int c = l.compareTo(r);
        return c >= 0;
    }

    public static int compareTo(java.time.LocalTime l, java.time.LocalTime r) {
        int c = l.compareTo(r);
        return c;
    }

    // ------------------------------ java.time.LocalDateTime
    public static boolean lessThan(java.time.LocalDateTime l, java.time.LocalDateTime r) {
        int c = l.compareTo(r);
        return c < 0;
    }

    public static boolean lessThanEqualTo(java.time.LocalDateTime l, java.time.LocalDateTime r) {
        int c = l.compareTo(r);
        return c <= 0;
    }

    public static boolean greaterThan(java.time.LocalDateTime l, java.time.LocalDateTime r) {
        int c = l.compareTo(r);
        return c > 0;
    }

    public static boolean greaterThanEqualTo(java.time.LocalDateTime l, java.time.LocalDateTime r) {
        int c = l.compareTo(r);
        return c >= 0;
    }

    public static int compareTo(java.time.LocalDateTime l, java.time.LocalDateTime r) {
        int c = l.compareTo(r);
        return c;
    }

    public static <T> List<T> sort(List<T> l) {
        List nl = new ArrayList(l);
        Collections.sort(nl);
        return nl;
    }
}
