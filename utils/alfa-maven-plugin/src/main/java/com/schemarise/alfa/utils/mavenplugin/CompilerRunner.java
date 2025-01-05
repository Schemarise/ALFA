package com.schemarise.alfa.utils.mavenplugin;

import com.google.common.collect.Sets;
import com.schemarise.alfa.compiler.AlfaCompiler;
import com.schemarise.alfa.compiler.CompilationUnitArtifact;
import com.schemarise.alfa.compiler.ast.model.ICompilationUnitArtifact;
import com.schemarise.alfa.compiler.ast.model.IResolutionMessage;
import com.schemarise.alfa.compiler.settings.AllSettings;
import com.schemarise.alfa.compiler.settings.ArtifactReference;
import com.schemarise.alfa.compiler.tools.AlfaPath;
import com.schemarise.alfa.compiler.tools.repo.CompilerException;
import com.schemarise.alfa.compiler.tools.repo.CompilerParamProvider;
import com.schemarise.alfa.compiler.tools.repo.MavenBasedRepositoryMgr;
import scala.Option;
import scala.collection.Seq;

import java.nio.file.Path;
import java.util.List;

public class CompilerRunner {
    private final AllSettings compilerSettings;
    private final MavenILogger logger;
    private final CompilerParamProvider compilerParams;
    private final Path srcRootDir;
    private final Path outputDir;

    public CompilerRunner(CompilerParamProvider params, MavenILogger l) throws CompilerException {
        srcRootDir = params.getSourcePath();
        outputDir = params.getOutputRootDir();
        compilerParams = params;
        logger = l;
        compilerSettings = getCompilerSettings(params);
    }

    public Path getOutputZipPath() {
        String n = "alfa/" + compilerSettings.projectId().get().name() + ".zip";
        return outputDir.resolve(n);
    }

    public ICompilationUnitArtifact execute() {
        MavenBasedRepositoryMgr repoMan = new MavenBasedRepositoryMgr(logger, compilerParams);


        AlfaCompiler c = new AlfaCompiler(logger, repoMan);
        ICompilationUnitArtifact cua = c.compile(srcRootDir, compilerSettings, Option.empty());

        Seq<IResolutionMessage> warnings = cua.getWarnings();
        if (warnings.size() > 0)
            logger.warn(srcRootDir, warnings);

//        while ( warnings.hasNext() ) {
//            IResolutionMessage v = warnings.next();
//            logger.warn( v.location().toString() + Console.BOLD() + v.formattedMessage() );
//        }

        Seq<IResolutionMessage> errors = cua.getErrors();
        if (errors.size() > 0)
            logger.error(srcRootDir, errors);

//        while ( errors.hasNext() ) {
//            IResolutionMessage v = errors.next();
//            logger.error( v.location().toString() + " " + Console.BOLD() + v.formattedMessage() );
//        }

        if (cua.getErrors().size() > 0)
            throw new CompilerException("See compiler output.");
        else {
            CompilationUnitArtifact art = (CompilationUnitArtifact) cua;

            art.writeAsZipModule(logger, getOutputZipPath());
            // art.writeAsFileSystemModule( this.outputDir.resolve("generated-sources/syn") );
        }

        return cua;
    }

    private AllSettings getCompilerSettings(CompilerParamProvider params) {
        ArtifactReference proj = params.getProjectArtifactDef();
        List<ArtifactReference> deps = params.getDependencies();

        Sets d;
        // TODO read compiler settings
        Option<Path> sProjFile = AlfaPath.locateProjectFileInPath(srcRootDir);

        return AllSettings.create(proj, deps.toArray(new ArtifactReference[0]));
    }
}
