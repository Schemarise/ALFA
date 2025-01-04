package com.schemarise.alfa.runtime;

public interface Visitor {
    boolean visit(AlfaObject o);

    RuntimeContext getContext();
}
