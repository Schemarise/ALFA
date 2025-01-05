package com.schemarise.alfa.runtime;

import java.util.List;

public interface IDecisionExecTable {
    <T> List<T> execute();
}
