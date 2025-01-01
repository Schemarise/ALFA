package com.schemarise.alfa.runtime.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface BindClass {
    String jaxb();
}

