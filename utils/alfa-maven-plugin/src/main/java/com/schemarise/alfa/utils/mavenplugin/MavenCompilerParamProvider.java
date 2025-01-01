package com.schemarise.alfa.utils.mavenplugin;

import com.schemarise.alfa.compiler.tools.repo.CompilerParamProvider;

import java.util.List;

public interface MavenCompilerParamProvider extends CompilerParamProvider {
    List<ExportSetting> getExportSettings();

    List<ImportSetting> getImportSettings();
}
