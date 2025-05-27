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
            List<String> alfaProps = System.getProperties().keySet().stream().
                    filter(p -> p.toString().startsWith("alfa.")).
                    filter(p -> !p.toString().equals("alfa.sourcepath")).
                    map(p -> p.toString().substring(5)).
                    collect(Collectors.toList());

            List<String> cliArgs = new ArrayList<>();

            alfaProps.stream().forEach(k -> {
                cliArgs.add("--" + k);
                Object v = System.getProperty("alfa." + k);
                if ( v != null &&
                        !v.toString().equals("true") &&  // -Dcompile
                        !v.toString().isBlank() ) {
                    cliArgs.add(v.toString());
                }
            });

            String srcpath = System.getProperties().getProperty("alfa.sourcepath");

            if ( srcpath == null || srcpath.isBlank() ) {
                throw new MojoExecutionException("-Dalfa.sourcepath=<path to source files> missing");
            }
            cliArgs.add(srcpath);

            logger.info("Arguments: " + String.join(" ", cliArgs));

            AlfaCli.parseAndRun(cliArgs.toArray(new String[]{}));

        } catch (Exception e) {
            throw new MojoExecutionException("Alfa compiler error.", e);
        }
    }
}