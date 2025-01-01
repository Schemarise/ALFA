/**
 * Copyright 2024 Schemarise Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.schemarise.alfa.compiler.tools.repo;

import com.schemarise.alfa.compiler.Context;
import com.schemarise.alfa.compiler.settings.AllSettings;
import com.schemarise.alfa.compiler.settings.ArtifactReference;
import com.schemarise.alfa.compiler.utils.ILogger;
import scala.Option;
import scala.Some;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MavenBasedRepositoryMgr implements IRepositoryManager {
    private final ILogger logger;
    private final CompilerParamProvider compilerParams;
    private final Map<ArtifactReference, IArtifact> depArts;

    public MavenBasedRepositoryMgr(ILogger logger, CompilerParamProvider compilerParams) {
        this.logger = logger;
        this.compilerParams = compilerParams;

        depArts = new LinkedHashMap<>();
        for (ArtifactReference i : compilerParams.getDependencies()) {
            Path p = i.artifactPath().get();

            try {
                FileSystem zipfs = FileSystems.newFileSystem(p, getClass().getClassLoader());
                depArts.put(i, new Artifact(i, zipfs, p));
            } catch (IOException e) {
                throw new CompilerException("Failed to create file system from " + p, e);
            }
        }
    }

    public List<IArtifact> getArtifacts() {
        return new ArrayList<>(depArts.values());
    }

    @Override
    public Option<IArtifact> getArtifact(ArtifactReference ref) {
        IArtifact g = depArts.get(ref);
        if (g == null)
            return Option.empty();
        else
            return new Some(g);
    }

    @Override
    public IDependencyManager createDependencyManager(AllSettings settings, Context ctx) {
        return new DependencyMgr(this, settings, ctx);
    }

}
