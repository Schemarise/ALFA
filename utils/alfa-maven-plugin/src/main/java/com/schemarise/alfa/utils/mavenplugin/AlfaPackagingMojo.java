package com.schemarise.alfa.utils.mavenplugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;

@Mojo(name = "package",
        requiresDependencyResolution = ResolutionScope.COMPILE,
        defaultPhase = LifecyclePhase.PACKAGE,
        threadSafe = true)
public class AlfaPackagingMojo extends AlfaCompileMojo {

    @Component
    private MavenProjectHelper projectHelper;


    @Override
    public void execute() throws MojoExecutionException {
        try {
            init();
            CompilerRunner runner = new CompilerRunner(this, logger);
            File f = runner.getOutputZipPath().toFile();
            logger.info("Prepared maven package " + f.getAbsolutePath());

            projectHelper.attachArtifact(project, "zip", "alfa", f);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to create alfa package", e);
        }
    }
}
