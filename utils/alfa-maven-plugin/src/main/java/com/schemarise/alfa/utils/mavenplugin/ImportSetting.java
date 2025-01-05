package com.schemarise.alfa.utils.mavenplugin;

import com.schemarise.alfa.compiler.tools.repo.CompilerException;
import com.schemarise.alfa.compiler.utils.ILogger;
import com.schemarise.alfa.utils.cli.Importers;
import org.apache.maven.plugins.annotations.Parameter;
import scala.Option;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

public class ImportSetting {
    /**
     * importClassName or type needs to be set
     */
    @Parameter(name = "importClassName", required = false)
    private String importClassName;

    /**
     * importClassName or type needs to be set
     */
    @Parameter(name = "importType", required = false)
    private String importType;

    @Parameter(name = "outputDir", required = false)
    private File outputDir;

    @Parameter(name = "config", required = false)
    private Map<String, Object> config = Collections.emptyMap();

    public void setImportClassName(String c) {
        importClassName = c;
    }

    public String getImportClass() {
        return importClassName;
    }

    public void setImportType(String c) {
        importType = c;
    }

    public String getImportType() {
        return importType;
    }

    public void setOutputDir(File c) {
        outputDir = c;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setConfig(Map<String, Object> c) {
        config = c;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    private void validate() {
        if (importClassName == null && importType == null)
            throw new CompilerException("One of importClassName or importType needs to be set");

        if (importClassName != null && importType != null)
            throw new CompilerException("Both importClassName and importType cannot be set");
    }

    public void inferDefaults(ILogger logger, String outputDirectory) {

        validate();

        if (importClassName == null) {
            Option<String> expClass = new Importers(logger).getImportClassName(importType);
            if (expClass.isEmpty())
                throw new CompilerException("Unknown importer type " + importType);

            importClassName = expClass.get();
        }

        if (outputDir == null) {
            if (importType == null)
                throw new CompilerException("outputDir required when importClass is specified" + importType);

            outputDir = Paths.get(outputDirectory, "..", "generated-sources", importType).toAbsolutePath().normalize().toFile();
        }
    }

    @Override
    public String toString() {
        return "ImportSetting{" +
                "importClassName='" + importClassName + '\'' +
                ", importType='" + importType + '\'' +
                ", outputDir=" + outputDir +
                ", config=" + config +
                '}';
    }
}
