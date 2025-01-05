package com.schemarise.alfa.runtime.utils;

import com.schemarise.alfa.runtime.*;
import schemarise.alfa.runtime.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class DefaultTypeDescriptor implements TypeDescriptor {

    private Map<String, String> fieldsOfUnionFields = new HashMap<>();
    private Map<String, List<String>> typeNameAssignableToField = null;
    private Set<String> allDescendants = null;
    private List<String> fieldNames;

    protected void init() {
        // union<...> cases
        List<Pair<String, UnionDataType>> unionDts = getAllFieldsMeta().
                entrySet().
                stream().
                map(e -> {
                    IDataType t = e.getValue().getDataType();
                    UnionDataType u = locateNestedUnion(t);
                    if (u == null)
                        return null;
                    else
                        return (Pair<String, UnionDataType>) Pair.builder().setLeft(e.getKey()).setRight(u).build();
                })
                .filter(f -> f != null)
                .collect(Collectors.toList());

        for (Pair<String, UnionDataType> ax : unionDts) {
            UnionDataType u = ax.getRight();

            ClassUtils.ClassMeta cm = ClassUtils.getMeta(u.getSynthFullyQualifiedName());
            DefaultTypeDescriptor dtd = (DefaultTypeDescriptor) cm.getModel();
            fieldsOfUnionFields.putAll(dtd.fieldsOfUnionFields);

            u.getFields().keySet().forEach(k -> {
                fieldsOfUnionFields.put(k, ax.getLeft());
            });
        }

        List<String> l = getAllFieldsMeta().values().stream().map(e -> e.getField().getName()).collect(Collectors.toList());
        fieldNames = new ArrayList<String>(l);
    }

    public List<String> getAllFieldNames() {
        return fieldNames;
    }

    public Optional<Map<String, Expression>> getAnnotations() {
        return Optional.empty();
    }

    private UnionDataType locateNestedUnion(IDataType t) {
        if (t instanceof UnionDataType)
            return (UnionDataType) t;
        else if (t instanceof OptionalDataType)
            return locateNestedUnion(((OptionalDataType) t).getComponentType());
        else if (t instanceof ListDataType)
            return locateNestedUnion(((ListDataType) t).getComponentType());
        else
            return null;
    }

    public Set<String> getAllDescendants() {
        if (allDescendants == null) {
            Set<String> set = new HashSet<>();

            set.addAll(getImmediateDescendants());
            for (String dep : getImmediateDescendants()) {
                if (!set.contains(dep)) {
                    ClassUtils.ClassMeta cm = ClassUtils.getMeta(dep);
                    set.addAll(cm.getModel().getAllDescendants());
                }
            }
            allDescendants = set;
        }

        return allDescendants;
    }

    public Optional<String> getFieldContainingNestedUnionField(String name) {
        String f = fieldsOfUnionFields.get(name);
        return Optional.ofNullable(f);
    }

    @Override
    public <T extends AlfaObject> Optional<BiConsumer<T, DataConsumer>> getFieldSupplier(String fieldName) {
        return Optional.empty();
    }

    @Override
    public Optional<BiConsumer<Builder, DataSupplier>> getFieldConsumer(String fieldName) {
        return Optional.empty();
    }

    @Override
    public <T extends AlfaObject> Map<String, FieldMeta<T>> getAllFieldsMeta() {
        return Collections.emptyMap();
    }

    @Override
    public UdtDataType getUdtDataType() {
        return null;
    }

    @Override
    public Optional<TypeDescriptor> getEntityKeyModel() {
        return Optional.empty();
    }

    @Override
    public boolean hasAbstractTypeFieldsInClosure() {
        return false;
    }

    @Override
    public boolean convertableToBuilder() {
        return false;
    }

    @Override
    public boolean hasBuilder() {
        return false;
    }

    @Override
    public Builder builder() {
        return null;
    }

    @Override
    public Builder builder(IBuilderConfig cc) {
        throw new AlfaRuntimeException("newBuilder not implemented for " + getClass().getName());
    }

    @Override
    public java.util.Set<String> getImmediateDescendants() {
        return Collections.emptySet();
    }

    @Override
    public List<String> getFieldAssignableToTypeName(String fqn) {
        if (typeNameAssignableToField == null) {
            Map<String, List<String>> m = new HashMap<>();
            getAllFieldsMeta().
                    entrySet().
                    stream().
                    forEach(e -> {
                        String fname = e.getKey();
                        IDataType dt = e.getValue().getDataType();
                        List<String> path = new ArrayList<>();
                        path.add(fname);
                        findAssignalbleTypes(m, dt, path);
                    });
            typeNameAssignableToField = m;
        }
        return typeNameAssignableToField.get(fqn);
    }

    private void findAssignalbleTypes(Map<String, List<String>> m, IDataType dt, List<String> path) {
        if (dt instanceof UdtDataType) {
            UdtDataType udt = (UdtDataType) dt;
            m.put(udt.getFullyQualifiedName(), path);
            Set<String> fieldTypeDesc = ClassUtils.getMeta(udt.getFullyQualifiedName()).getModel().getAllDescendants();
            fieldTypeDesc.forEach(a -> {
                m.put(a, path);

                TypeDescriptor cms = ClassUtils.getMeta(a).getModel();
                findAssignalbleTypes(m, cms.getUdtDataType(), path);
            });
        } else if (dt instanceof UnionDataType) {
            UnionDataType ut = (UnionDataType) dt;

            ut.getFields().forEach((k, v) -> {
                List<String> copy = new ArrayList<>(path);
                copy.add(k);
                findAssignalbleTypes(m, v.getDataType(), copy);
            });
        } else if (dt instanceof OptionalDataType) {
            OptionalDataType t = (OptionalDataType) dt;
            findAssignalbleTypes(m, t.getComponentType(), path);
        } else if (dt instanceof ListDataType) {
            ListDataType t = (ListDataType) dt;
            findAssignalbleTypes(m, t.getComponentType(), path);
        }
    }

    public java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Assert> getAsserts() {
        return Collections.emptyMap();
    }

    protected <T extends UdtBaseNode> T loadModel(Class<?> context, UdtDataType udt) {
        var p = "META-INF/alfa-schemas/" + udt.getFullyQualifiedName() + ".json";
        var is = context.getClassLoader().getResourceAsStream(p);

        if (is == null) {
            throw new AlfaRuntimeException("Failed to load schema " + p +
                    " from context class " + context.getName() + " in " + context.getProtectionDomain().getCodeSource().getLocation());
        }

        try {
            T res = Alfa.jsonCodec().uncheckedFromJson(is);
            return res;
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

}
