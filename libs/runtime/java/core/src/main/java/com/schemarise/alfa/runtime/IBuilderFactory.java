package com.schemarise.alfa.runtime;

/**
 * An interface that is used by IBuilderConfig to construct instances of the final target object
 * Implementations can plug custom implementation in order the create custom concrete classes
 * but adhereing to the generated ALFA type based interfaces.
 */
public interface IBuilderFactory {
    <T extends Builder> T builder(com.schemarise.alfa.runtime.IBuilderConfig bc, TypeDescriptor descriptor, Object... objs);

    <T extends AlfaObject> T create(com.schemarise.alfa.runtime.IBuilderConfig bc, TypeDescriptor descriptor, Object... objs);

    @Deprecated
    default <T extends Builder> T create(TypeDescriptor descriptor, Object... objs) {
        return builder(null, descriptor, objs);
    }
}

