package com.schemarise.alfa.utils.mavenplugin;

import com.schemarise.alfa.compiler.utils.ILogger$;
import com.schemarise.alfa.utils.cli.AlfaCli;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mojo(name = "cli",
        requiresDependencyResolution = ResolutionScope.COMPILE,
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresProject = false,
        threadSafe = true)
public class AlfaCliMojo extends AbstractMojo {
    protected MavenILogger logger;

    AlfaCliMojo() {
        ILogger$.MODULE$.setupAnsiConsole();
    }

    @Override
    public void setLog(Log log) {
        logger = new MavenILogger(log);
        super.setLog(log);
    }

    @Override
    public void execute() throws MojoExecutionException {
        try {
            var allSysProps = System.getProperties().keySet().stream().map(Object::toString).sorted().collect(Collectors.toList());
            logger.debug("AlfaCliPlugin all system properties: " + allSysProps);

            List<String> alfaProps = System.getProperties().keySet().stream().
                    filter(p -> p.toString().startsWith("alfa.")).
                    filter(p -> !p.toString().equals("alfa.sourcepath")).
                    map(p -> p.toString().substring(5)).
                    collect(Collectors.toList());

            List<String> cliArgs = new ArrayList<>();

            alfaProps.forEach(k -> {
                cliArgs.add("--" + k);
                Object v = System.getProperty("alfa." + k);
                logger.debug("AlfaCliPlugin alfa system property:" + k + "=" + v);
                if ( v != null &&
                        !v.toString().equals("true") &&  // -Dcompile
                        !v.toString().isBlank() ) {
                    cliArgs.add(v.toString());
                }
            });

            String srcpath = System.getProperties().getProperty("alfa.sourcepath");

            logger.debug("AlfaCliPlugin alfa system property:sourcepath=" + srcpath);

            if ( srcpath == null || srcpath.isBlank() ) {
                throw new MojoExecutionException("-Dalfa.sourcepath=<path to source files> missing.\nAvailable property keys: " +
                        String.join(", ", allSysProps ));
            }
            cliArgs.add(srcpath);

            logger.info("Arguments: " + String.join(" ", cliArgs));

            AlfaCli.parseAndRun(cliArgs.toArray(new String[]{}), logger);

        } catch (Exception e) {
            throw new MojoExecutionException("Alfa compiler error.", e);
        }
    }
}