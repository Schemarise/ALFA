package com.schemarise.alfa.runtime.utils;

import com.schemarise.alfa.runtime.*;
import schemarise.alfa.runtime.model.Testcase;
import schemarise.alfa.runtime.model.asserts.ConstraintType;

import java.lang.Enum;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClassUtils {
    private static Map<String, ClassMeta> classCache = new ConcurrentHashMap<>();

    private static InheritableThreadLocal<ClassLoader> classLoader = new InheritableThreadLocal<>();

    public static void setContextClassLoader(ClassLoader cl) {
        Thread.currentThread().setContextClassLoader(cl);
        classLoader.set(cl);
    }

    public static class ClassMeta {
        private Map<String, Optional<Enum>> enumFields = new HashMap<>();
        private Map<String, AlfaRuntimeException> enumUnknowns = new HashMap<>();
        private Class<?> typeClass;
        private TypeDescriptor model;

        private ClassMeta() {
        }

        public Builder getNewBuilder(IBuilderConfig cc) {
            try {
                return model.builder(cc);
            } catch (Exception e) {
                if (e instanceof AlfaRuntimeException)
                    throw e;
                else
                    throw new AlfaRuntimeException(ConstraintType.Unknown, "Failed to create newBuilder. ", e);
            }
        }

        public Class<?> getTypeClass() {
            return typeClass;
        }

        public TypeDescriptor getModel() {
            return model;
        }

        public boolean hasBuilderClass() {
            return model != null &&  // when service
                    model.hasBuilder();
        }

        public String toString() {
            return "ClassMeta:" + typeClass.getName() + ";";
        }

        public boolean isTypeAssignable(ClassMeta other) {
            return typeClass.isAssignableFrom(other.typeClass);
        }

        public void assertCompatible(ClassMeta other) {
            if (!typeClass.isAssignableFrom(other.typeClass)) {
                throw new AlfaRuntimeException("Types incompatible - " + typeClass.getName() + " and " + other.typeClass.getName());
            }
        }
    }

    public static Enum getByEnumConst(String fullyQualifiedName) {
        int l = fullyQualifiedName.lastIndexOf(".");
        return getByEnumConst(fullyQualifiedName.substring(0, l), fullyQualifiedName.substring(l + 1), false);
    }


    public static Enum getByEnumConst(String enumType, String constField) {
        return getByEnumConst(enumType, constField, false);
    }

    public static Enum getByEnumConst(String enumType, String constField, boolean nullOnNotFound) {
        ClassUtils.ClassMeta cm = ClassUtils.getMeta(enumType);

        Optional<Enum> en = cm.enumFields.get(constField);

        if (en != null) {
            if (en.isPresent())
                return en.get();
            else {
                if (nullOnNotFound)
                    return null;

                throw cm.enumUnknowns.get(constField);
            }
        }

        Field[] flds = cm.getTypeClass().getDeclaredFields();
        for (Field f : flds) {
            if (Enum.class.isAssignableFrom(f.getType())) {
                java.lang.Enum v = java.lang.Enum.valueOf((Class<java.lang.Enum>) f.getType(), f.getName());
                cm.enumFields.put(f.getName(), Optional.of(v));

                com.schemarise.alfa.runtime.Enum v2 = (com.schemarise.alfa.runtime.Enum) v;
                if (v2.getLexicalValue().isPresent()) {
                    cm.enumFields.put(v2.getLexicalValue().get(), Optional.of(v));
                }
            }
        }

        Optional<Enum> c = cm.enumFields.get(constField);

        if (c == null) {
            cm.enumFields.put(constField, Optional.empty());

            AlfaRuntimeException excp = new AlfaRuntimeException(ConstraintType.InvalidConstant,
                    "Invalid value '" + constField + "' for type '" + enumType + "'");

            cm.enumUnknowns.put(constField, excp);

            if (nullOnNotFound)
                return null;

            throw excp;
        } else
            return c.get();
    }

//    public static Enum getByEnumValue(String enumType, String constField) {
//        ClassUtils.ClassMeta cm = ClassUtils.getMeta(enumType);
//
//        try {
//            Method m = cm.getTypeClass().getMethod("fromValue", new Class[]{String.class});
//            Object enumVal = m.invoke(null, constField);
//            if ( enumVal == null )
//                throw new Exception("Value not found");
//
//            return (Enum) enumVal;
//        } catch (Exception e) {
//            throw new AlfaRuntimeException( ConstraintType.InvalidConstant, "Field '" + constField + "' is invalid constant for enum '" + enumType + "'", e);
//        }
//    }

    private static Class forName(String n) throws ClassNotFoundException {
        if (classLoader.get() == null) {
            classLoader.set(Thread.currentThread().getContextClassLoader());
        }
        return classLoader.get().loadClass(n);
    }

    private static Map<String, Boolean> definedClasses = new HashMap<>();

    public static boolean isDefined(String type) {
        Boolean existing = definedClasses.get(type);

        if (existing != null)
            return existing;
        else {
            try {
                forName(type);
                definedClasses.put(type, true);
                return true;
            } catch (ClassNotFoundException e) {
                definedClasses.put(type, false);
                return false;
            }
        }
    }

    public static ClassMeta getMeta(String type) {
        ClassMeta existing = classCache.get(type);
        if (existing != null)
            return existing;
        else {
            try {
                Class<?> clz = forName(type);
                ClassMeta cm = new ClassMeta();
                cm.typeClass = clz;

                if ( !Service.class.isAssignableFrom(clz) &&
                     !Library.class.isAssignableFrom(clz)  &&
                     !Testcase.class.isAssignableFrom(clz)  ) {
                    String[] split = type.split("\\.");
                    Class<?> descclz = forName(type + "$" + split[split.length - 1] + "Descriptor");
                    cm.model = (TypeDescriptor) descclz.getDeclaredField("INSTANCE").get(null);

                    if (cm.model == null)
                        throw new AlfaRuntimeException("Failed to load model for " + type);
                }
                classCache.put(type, cm);

                return cm;
            } catch (Exception e) {
                if (e instanceof AlfaRuntimeException)
                    throw (AlfaRuntimeException) e;
                else
                    throw new AlfaRuntimeException(ConstraintType.Unknown, e);
            }
        }
    }

    private static Map<String, Field> classFieldMap = new HashMap<>();

    public static <T> T getDeclaredFieldValue(Object obj, String fieldName) {
        try {
            return (T) getField(obj, fieldName).get(obj);
        } catch (Exception e) {
            if (e instanceof AlfaRuntimeException)
                throw (AlfaRuntimeException) e;
            else
                throw new AlfaRuntimeException(ConstraintType.Unknown, e);
        }
    }

    public static void setDeclaredFieldValue(Object obj, String fieldName, Object o) {
        try {
            getField(obj, fieldName).set(obj, o);
        } catch (Exception e) {
            if (e instanceof AlfaRuntimeException)
                throw (AlfaRuntimeException) e;
            else
                throw new AlfaRuntimeException(ConstraintType.Unknown, e);
        }
    }

    public static Field getField(Object obj, String fieldName) {
        Class<?> cls = obj.getClass();

        String idx = cls.getTypeName() + ":" + fieldName;

        Field f = classFieldMap.get(idx);
        if (f == null) {
            f = getDeclaredField(cls, fieldName);
            classFieldMap.put(idx, f);
        }

        if (f == null) {
            throw new AlfaRuntimeException(ConstraintType.UnknownField,
                    "Failed to find field " + fieldName + " in " + obj.getClass().getName());
        }
        return f;
    }

    private static Field getDeclaredField(Class<?> cls, String fieldName) {
        try {
            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException nsf) {
            if (cls.getSuperclass() != null)
                return getDeclaredField(cls.getSuperclass(), fieldName);
            else
                return null;
        }
    }

}
