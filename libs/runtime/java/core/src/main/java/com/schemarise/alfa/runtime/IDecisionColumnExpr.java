package com.schemarise.alfa.runtime;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface IDecisionColumnExpr<T> extends Function<T, Boolean> {

}
