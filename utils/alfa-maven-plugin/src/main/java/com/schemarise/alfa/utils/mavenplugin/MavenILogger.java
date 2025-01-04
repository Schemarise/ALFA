package com.schemarise.alfa.utils.mavenplugin;

import com.schemarise.alfa.compiler.ast.model.IResolutionMessage;
import com.schemarise.alfa.compiler.utils.ILogger;
import org.apache.maven.plugin.logging.Log;
import scala.Console;
import scala.collection.Seq;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;

import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;


public class MavenILogger implements ILogger {
    private Log mLogger;

    public MavenILogger(Log log) {
        mLogger = log;
    }

    @Override
    public void debug(String s) {
        mLogger.debug(ansi().fg(BLUE).a(s).reset().toString());
    }

    @Override
    public void trace(String s) {
        mLogger.debug(ansi().fg(CYAN).a(s).reset().toString());
    }

    @Override
    public void info(String s) {
        mLogger.info(s);
    }

    @Override
    public void vitalInfo(String s) {
        mLogger.info(ansi().fg(GREEN).bold().a(s).reset() + Console.RESET());
    }

    @Override
    public void warn(String s) {
        mLogger.warn(ansi().fg(MAGENTA).bold().a(s).reset().toString());
    }

    public void warn(Path srcRootDir, Seq<IResolutionMessage> errs) {
        formatAndLogMessages(srcRootDir, errs, true, false);
    }

    @Override
    public void error(String s) {
        mLogger.error(ansi().fg(RED).a(s).reset().toString());
    }

    public void error(Path srcRootDir, Seq<IResolutionMessage> errs) {
        formatAndLogMessages(srcRootDir, errs, false, false);
    }

    @Override
    public void error(String s, Throwable e) {
        error(s);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        error(sw.toString());
    }

    @Override
    public boolean isTraceEnabled() {
        return mLogger.isDebugEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return mLogger.isDebugEnabled();
    }
}
