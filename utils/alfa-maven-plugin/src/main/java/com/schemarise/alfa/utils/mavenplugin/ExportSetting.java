package com.schemarise.alfa.utils.mavenplugin;

import com.schemarise.alfa.compiler.tools.repo.CompilerException;
import com.schemarise.alfa.compiler.utils.ILogger;
import com.schemarise.alfa.generators.exporters.java.JavaExporter$;
import com.schemarise.alfa.utils.cli.Exporters;
import org.apache.maven.plugins.annotations.Parameter;
import scala.Option;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ExportSetting {
    /**
     * exportClassName or type needs to be set
     * TODO Remove and make exportType mandatory
     */
    @Parameter(name = "exportClassName", required = false)
    private String exportClassName;

    /**
     * exportClassName or type needs to be set
     */
    @Parameter(name = "exportType", required = false)
    private String exportType;

    /**
     * all, local, localandreachable
     * Values from GraphReachabilityScopeType
     */
    @Parameter(name = "exportScope", required = false)
    private String exportScope;


    @Parameter(name = "outputDir", required = false)
    private File outputDir;

    @Parameter(name = "config", required = false)
    private Map<String, Object> config = new HashMap<>();

    public void setExportClassName(String c) {
        exportClassName = c;
    }

    public String getExportClass() {
        return exportClassName;
    }

    public void setExportType(String c) {
        exportType = c;
    }

    public String getExportType() {
        return exportType;
    }

    public String getExportScope() {
        return exportScope;
    }

    public void setExportScope(String exportScope) {
        this.exportScope = exportScope;
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
        if (exportClassName == null && exportType == null)
            throw new CompilerException("One of exportClassName or type needs to be set");

        if (exportClassName != null && exportType != null)
            throw new CompilerException("Both exportClassName and type cannot be set");
    }

    public void inferDefaults(ILogger logger, String outputDirectory, String testOutputDirectory) {
        validate();

        if (exportClassName == null) {
            Option<String> expClass = new Exporters(logger).getExportClassName(exportType);
            if (expClass.isEmpty())
                throw new CompilerException("Unknown exporter type " + exportType);

            exportClassName = expClass.get();
        }

        if (exportType != null && exportType.equals("java") && !config.containsKey(JavaExporter$.MODULE$.ResourcesPath())) {
            var dir = outputDir != null && outputDir.getAbsolutePath().contains("generated-test") ? testOutputDirectory : outputDirectory;
            var rpath = Paths.get(dir); // resources written to classes dir
            config.put(JavaExporter$.MODULE$.ResourcesPath(), rpath);
        }

        if (outputDir == null) {
            String subPath = exportType == null ? "" : new Exporters(logger).exportSubDirectory(exportType);
            outputDir = Paths.get(outputDirectory, subPath).toAbsolutePath().normalize().toFile();
        }
    }

    @Override
    public String toString() {
        return "ExportSetting{" +
                "exportClassName='" + exportClassName + '\'' +
                ", exportType='" + exportType + '\'' +
                ", outputDir=" + outputDir +
                ", config=" + config +
                '}';
    }
}
