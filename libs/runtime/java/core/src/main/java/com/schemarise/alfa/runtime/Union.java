package com.schemarise.alfa.runtime;

/**
 * Base interface of all generated Union definitions.
 */
public interface Union extends AlfaObject {
    /**
     * Name of the union case/field that has been assigned for this union
     *
     * @return Name of the assigned union field. If untagged union null is returned
     */
    String caseName();

    /**
     * Value for the case/field that has been assigned a value
     *
     * @return The value assigned to the field
     */
    Object caseValue();

    default boolean isTagged() {
        return true;
    }

//    /**
//     * Is this an Alfa Union object
//     * @return true to indicate this is a union
//     */
//    default boolean isUnion() {
//        return true;
//    }
}
