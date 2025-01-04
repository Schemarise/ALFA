package com.schemarise.alfa.runtime.codec;

import com.schemarise.alfa.runtime.ServiceFactory;

public class ServiceFactoryDefinition {
    private Class<? extends com.schemarise.alfa.runtime.ServiceFactory> ifc;
    private ServiceFactory sfImpl;

    public ServiceFactoryDefinition(Class<? extends ServiceFactory> ifc, ServiceFactory sfImpl) {
        this.ifc = ifc;
        this.sfImpl = sfImpl;
    }

    public Class<? extends ServiceFactory> getIfc() {
        return ifc;
    }

    public ServiceFactory getSfImpl() {
        return sfImpl;
    }
}
