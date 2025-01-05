package com.schemarise.alfa.runtime;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Logger implements ILogger {
    private final boolean debugEnabled;
    private final boolean traceEnabled;

    private static ILogger defaultLogger;

    public static void setDefaultLogger(ILogger l) {
        defaultLogger = l;
    }

    public static synchronized ILogger getOrCreateDefault() {
        if (defaultLogger == null)
            defaultLogger = new Logger();
        return defaultLogger;
    }

    public static synchronized ILogger getOrCreateDefault(boolean debug, boolean trace) {
        if (defaultLogger == null)
            defaultLogger = new Logger(debug, trace);
        return defaultLogger;
    }

    public Logger() {
        this(false, false);
    }

    public Logger(boolean debug, boolean trace) {
        this.debugEnabled = debug;
        this.traceEnabled = trace;
    }

    @Override
    public void info(String msg) {
        log("INFO", msg);
    }

    @Override
    public void debug(String msg) {
        if (isDebugEnabled())
            log("DEBUG", msg);
    }

    @Override
    public void error(String msg) {
        log("ERROR", msg);
    }

    @Override
    public void error(String msg, Throwable e) {
        log("ERROR", msg);
    }

    @Override
    public void warn(String msg) {
        log("WARN", msg);
    }

    @Override
    public void trace(String msg) {
        if (isTraceEnabled())
            log("TRACE", msg);
    }

    @Override
    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    @Override
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private void log(String type, String msg) {
        System.out.println(sdf.format(new Date()) + " " + type + " : " + msg);
    }

    public static String stacktraceToString(Throwable e, int lines) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        e.printStackTrace(pw);

        String[] stackstr = sw.toString().split("\n");

        if (lines > 0 && stackstr.length > lines) {
            return String.join("\n", Arrays.copyOfRange(stackstr, 0, lines));
        } else {
            return String.join("\n", stackstr);
        }
    }
}
