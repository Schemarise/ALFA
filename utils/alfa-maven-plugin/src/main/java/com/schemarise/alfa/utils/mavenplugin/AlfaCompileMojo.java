package com.schemarise.alfa.utils.mavenplugin;

import com.schemarise.alfa.compiler.ast.model.ICompilationUnitArtifact;
import com.schemarise.alfa.compiler.ast.model.graph.GraphReachabilityScopeType;
import com.schemarise.alfa.compiler.settings.AllSettings;
import com.schemarise.alfa.compiler.settings.ArtifactReference;
import com.schemarise.alfa.compiler.tools.AlfaPath;
import com.schemarise.alfa.compiler.tools.AllSettingsFactory;
import com.schemarise.alfa.compiler.tools.repo.CompilerException;
import com.schemarise.alfa.compiler.utils.ILogger$;
import com.schemarise.alfa.compiler.utils.VFS;
import com.schemarise.alfa.generators.common.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import scala.Option;
import scala.Some;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Mojo(name = "compile",
        requiresDependencyResolution = ResolutionScope.COMPILE,
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        threadSafe = true)
public class AlfaCompileMojo extends AbstractMojo implements MavenCompilerParamProvider {
    protected MavenILogger logger;

    @Parameter(name = "project", defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter(name = "srcPath")
    private Path srcPath;

    @Parameter(name = "srcDependency")
    private String srcDependency;

    @Parameter(name = "exportSettings")
    private List<ExportSetting> exportSettings = Collections.emptyList();

    @Parameter(name = "importSettings")
    private List<ImportSetting> importSettings = Collections.emptyList();

    private List<ArtifactReference> dependencies;
    private ArtifactReference artifactReference;

    AlfaCompileMojo() {
        ILogger$.MODULE$.setupAnsiConsole();
    }

    // ------------ Property Bean methods ------------

    public MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public File getSrcPath() {
        return srcPath.toFile();
    }

    @Override
    public Path getSourcePath() {
        return srcPath;
    }

    public void setSrcPath(File f) {
        srcPath = f.toPath();
    }

    @Override
    public List<ExportSetting> getExportSettings() {
        return exportSettings;
    }

    @Override
    public List<ImportSetting> getImportSettings() {
        return importSettings;
    }

    @Override
    public Path getOutputRootDir() {
        return Paths.get(getProject().getBuild().getOutputDirectory()).getParent();
    }

    public void setExportSettings(List<ExportSetting> d) {
        d.forEach(e -> logger.debug("Exporter: " + e));
        exportSettings = d;
    }

    public void setImportSettings(List<ImportSetting> d) {
        d.forEach(e -> logger.debug("Importer: " + e));
        importSettings = d;
    }

    protected void init() throws Exception {
        readDependencies();
        readProjectArtifactDef();

        List<String> srcPaths = getProject().getCompileSourceRoots();

        if (srcDependency != null) {
            if (srcPath != null)
                throw new Exception("Both srcDependency and srcPath should not be specified");

            String[] ds = srcDependency.split(":");
            if (ds.length != 3)
                throw new Exception("srcDependency should be in the format <group>:<artifact>:<version>");

            String g = ds[0];
            String a = ds[1];
            String v = ds[2];

            // Exclude the srcDependency from main dependencies
            dependencies = dependencies.stream().filter(d -> {
                boolean match = d.group().equals(g) && d.name().equals(a) && d.version().get().equals(v);
                if (match) {
                    srcPath = d.artifactPath().get();
                    logger.info("Using source from artifact " + d + ". Path " + srcPath.toAbsolutePath());
                }
                return !match;
            }).collect(Collectors.toList());

            if (srcPath == null)
                throw new Exception("Failed to find srcDependency " + srcDependency + " in dependencies " + dependencies);
        } else {
            List<Path> pathsWithAlfaFiles = srcPaths.stream().
                    map(r -> new File(r).toPath()).
                    filter(p -> AlfaPath.containsAlfaResources(p)).
                    collect(Collectors.toList());

            if (srcPath == null && importSettings.size() == 0) {
                if (pathsWithAlfaFiles.size() > 1) {
                    throw new Exception("ALFA source found in multiple source-file paths. Please specify <srcPath> or <sourceDirectory>. Only 1 source path is currently supported - " + srcPaths);
                } else if (pathsWithAlfaFiles.size() == 0) {
                    logger.info("No ALFA source files were found in " + srcPaths);
                    logger.info("Use <sourceDirectory>src/main/alfa</sourceDirectory> for the sole source path, or configure using build-helper-maven-plugin. Current paths:");

                    srcPaths.forEach(p -> logger.info("\t - " + p));

                    FileSystem emptyFs = VFS.create();
                    srcPath = emptyFs.getPath("/");
                } else {
                    srcPath = pathsWithAlfaFiles.get(0);
                    logger.info("Using source path " + srcPath);
                }
            }
        }
    }

    @Override
    public ArtifactReference getProjectArtifactDef() {
        return artifactReference;
    }

    private void readProjectArtifactDef() {
        artifactReference = new ArtifactReference(project.getGroupId(), project.getArtifactId(),
                new Some<String>(project.getVersion()), Option.<Path>apply(null));
        logger.info("Using project artifact reference " + artifactReference);
    }

    @Override
    public List<ArtifactReference> getDependencies() {
        return dependencies;
    }


    private void readDependencies() {
        String FILE = "file:" + (SystemUtils.IS_OS_WINDOWS ? "/" : "");
        String JAR = ".zip!/";

        List<URL> allResources = new ArrayList<URL>();

        dependencies = new ArrayList<>();

        List zipFiles = new ArrayList<>();

        project.getArtifacts().stream().
                filter(e -> e.hasClassifier() && e.getClassifier().equals("alfa")).
                filter(e -> e.getType() != null && e.getType().equals("zip")).
                forEach(e -> {
                    String path = e.getFile().getPath();

                    zipFiles.add(e.getFile().getAbsoluteFile().toString());
                    try {
                        URLClassLoader child = new URLClassLoader(
                                new URL[]{new File(path).toURI().toURL()},
                                this.getClass().getClassLoader());

                        Enumeration<URL> yml = child.getResources(".alfa-meta/settings.alfa-proj.yaml");
                        Enumeration<URL> json = child.getResources(".alfa-meta/settings.alfa-proj.json");

                        allResources.addAll(Collections.list(yml));
                        allResources.addAll(Collections.list(json));
                    } catch (IOException ex) {
                        throw new CompilerException("Failed to find dependencies", ex);
                    }
                });

        if (zipFiles.size() > 0)
            logger.info("Found dependencies: " + zipFiles);


        for (URL fileUrl : allResources) {
            String n = fileUrl.getFile();
            int i = n.indexOf(JAR);
            Path p = Paths.get(n.substring(FILE.length(), i + JAR.length() - 2));

            String contents;
            try {
                contents = IOUtils.toString(fileUrl.openStream(), Charset.defaultCharset().name());
            } catch (IOException e1) {
                throw new CompilerException("Failed to read settings file from zip", e1);
            }

            String file = fileUrl.getFile();
            String ext = file.substring(file.length() - 4);
            AllSettings settings = AllSettingsFactory.fromJsonOrYamlString(Option.<Path>apply(null), contents, ext);

            ArtifactReference aref = settings.projectId().get();

            ArtifactReference r = new ArtifactReference(aref.group(), aref.name(), aref.version(), new Some<Path>(p));

            if (!dependencies.contains(r)) {
                dependencies.add(r);
            } else {
                logger.info("\tArtifact is already a dependency, skipping " + r);
            }
        }

        logger.info("Using ALFA project dependencies " + dependencies);
    }

    @Override
    public void setLog(Log log) {
        logger = new MavenILogger(log);
        super.setLog(log);
    }

    @Override
    public void execute() throws MojoExecutionException {
        try {
            init();

            if (!importSettings.isEmpty()) {
                logger.debug("Executing " + importSettings.size() + " importers ... ");
                importSettings.forEach(e -> runImport(e, this.getSourcePath()));
            } else {
                if (srcPath != null) {
                    CompilerRunner runner = new CompilerRunner(this, logger);
                    ICompilationUnitArtifact cua = runner.execute();

                    if (exportSettings.isEmpty())
                        logger.info("No ALFA exporters configured.");


                    for (ExportSetting s : exportSettings) {
                        runExport(s, cua);
                    }
                } else
                    logger.info("Not running ALFA compiler, no source files found.");
            }

        } catch (Exception e) {
            throw new MojoExecutionException("ALFA compiler error.", e);
        }

        if ( importSettings.isEmpty() || exportSettings.isEmpty() ) {
            logger.debug("Generation completed");
        }
    }

    private void runExport(ExportSetting e, ICompilationUnitArtifact cua) throws MojoExecutionException {

        e.inferDefaults(logger, getProject().getBuild().getOutputDirectory(), getProject().getBuild().getTestOutputDirectory());

        GraphReachabilityScopeType et = GraphReachabilityScopeType.all;

        if (e.getExportScope() != null) {
            try {
                et = GraphReachabilityScopeType.valueOf(e.getExportScope());
            } catch (Exception ex) {
                getLog().error("Unknown <exportScope> '" + e.getExportScope() +
                        "'. Expected one of " +
                        Arrays.asList(GraphReachabilityScopeType.values()).stream().map(x -> x.toString() + " ").collect(Collectors.joining()));
                throw new MojoExecutionException("Invalid <exportScope>");
            }
        }

        AlfaExporterParams exporterParams = new AlfaExporterParams(logger, e.getOutputDir().toPath(), cua, e.getConfig(), et);

        logger.info("Running exporter: " + e.getExportType());

        logger.debug("Exporter: " + e.getExportType() + " Config " +
                exporterParams.exportConfig() + " Scope " + exporterParams.exportScopeType());

        e.getOutputDir().mkdirs();

        // TODO Use Exporter

        try {
            Class.forName(e.getExportClass());
        } catch (Exception ex) {
            throw new CompilerException("The specified exportClass '" + e.getExportType() + "' is not found");
        }

        AlfaExporter exporter;

        try {
            Constructor<?> ctor = Class.forName(e.getExportClass()).
                    getDeclaredConstructor(AlfaExporterParams.class);
            exporter = (AlfaExporter) ctor.newInstance(exporterParams);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            logger.debug(sw.toString());
            throw new CompilerException("Unable to create exporter " + e.getExportType() + ". Use -X for trace");
        }

        List<String> supported = Arrays.asList(exporter.supportedConfig());
        for (String s : e.getConfig().keySet()) {
            if (!supported.contains(s)) {
                throw new CompilerException("The exportClass for '" + e.getExportType() + "' does not support <" + s +
                        "> as a exportSetting config entry. Use one of " + supported);
            }
        }

        String[] reqd = exporter.requiredConfig();
        for (String s : reqd) {
            if (! e.getConfig().containsKey(s)) {
                throw new CompilerException("The export type " + e.getExportType() + " class '" + e.getExportClass() + "' requires <config><" + s +
                        ">... as an exportSetting config entry");
            }
        }

        try {
            exporter.exportSchema();
        } catch (GeneratorException e1) {
            throw new CompilerException("Exporter '" + e.getExportClass() + "' failed.", e1);
        }
        logger.info("Exporter " + e.getExportType() + " completed, to " + e.getOutputDir());
    }


    private void runImport(ImportSetting e, Path sourcePath) {
        e.inferDefaults(logger, getProject().getBuild().getOutputDirectory());

        if (!Files.exists( sourcePath )) {
            throw new CompilerException("The specified path does not exist '" + sourcePath + "'");
        }

        logger.info("Running importer: " + e.getImportType());

        e.getOutputDir().mkdirs();

        AlfaImporterParams importerParams = new AlfaImporterParams(logger, sourcePath, e.getOutputDir().toPath(), e.getConfig());

        logger.debug("Importer: " + e.getImportType() + " Config " +
                importerParams.importConfig() );

        // TODO Use Importer
        try {
            Class.forName(e.getImportClass());
        } catch (Exception ex) {
            throw new CompilerException("The specified importClass '" + e.getImportClass() + "' is not found");
        }

        AlfaImporter importer = null;
        try {
            Constructor<?> ctor = Class.forName(e.getImportClass()).getDeclaredConstructor(AlfaImporterParams.class);
            importer = (AlfaImporter) ctor.newInstance(importerParams);
        } catch (Exception ex) {
            throw new CompilerException("Failed to create instance of importClass '" + e.getImportClass() + ex.getMessage(), ex);
        }

        List<String> supported = Arrays.asList(importer.supportedConfig());
        for (String s : e.getConfig().keySet()) {
            if (!supported.contains(s)) {
                throw new CompilerException("The importClass '" + e.getImportClass() + "' does not support <" + s +
                        "> as an importSetting config entry. Use one of " + supported);
            }
        }

        List<String> reqd = Arrays.asList(importer.requiredConfig());
        for (String s : reqd) {
            if (!e.getConfig().keySet().contains(s)) {
                throw new CompilerException("The importClass '" + e.getImportClass() + "' requires <" + s +
                        "> as an importSetting config entry");
            }
        }

        try {
            importer.importSchema();
        } catch (GeneratorException e1) {
            throw new CompilerException("Exporter '" + e.getImportClass() + "' failed.", e1);
        }

        logger.info("Importer " + e.getImportType() + " completed, to " + e.getOutputDir());
    }

}