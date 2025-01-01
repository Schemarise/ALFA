package com.schemarise.alfa.runtime;

/**
 * ALFA will use this logger for all runtimes.
 * Implementations can implement and redirect to existing frameworks.
 */
public interface ILogger {
    void info(String msg);

    void debug(String msg);

    void error(String msg);

    void error(String msg, Throwable e);

    void warn(String msg);

    void trace(String msg);

    boolean isTraceEnabled();

    boolean isDebugEnabled();
}
