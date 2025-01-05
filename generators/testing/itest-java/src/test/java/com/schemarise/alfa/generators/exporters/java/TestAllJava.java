package com.schemarise.alfa.generators.exporters.java;

import com.schemarise.alfa.compiler.AlfaCompiler;
import com.schemarise.alfa.compiler.CompilationUnitArtifact;
import com.schemarise.alfa.compiler.ast.model.ICompilationUnitArtifact;
import com.schemarise.alfa.compiler.utils.PathUtils;
import com.schemarise.alfa.compiler.utils.StdoutLogger;
import com.schemarise.alfa.runtime.*;
import com.schemarise.alfa.runtime.codec.json.JsonCodecConfig;
import com.schemarise.alfa.runtime.utils.AlfaRandomizer;
import com.schemarise.alfa.runtime.utils.AlfaUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TestAllJava {
    Path itestFiles = Paths.get(PathUtils.ResourceDirAsUnixPath(getClass()) + "/../../../test-material/src/").normalize();
    private AlfaRandomizer rand = new AlfaRandomizer();

    private Set<String> skipList = new HashSet<String>(
            Arrays.asList(new String[]{
                    "vectors.map.MapOfTraits",
                    "vectors.sets.SetOfTraits",
                    "vectors.list.ListOfTraits",
                    "enclosed.TableData",

//                    "generics.useof.SomeKeyFromEntity",
//                    "generics.InplaceInstantiate",
//
//                    "generics.ConcretedOptional",
//                    "generics.ConcretedMapList",
//                    "generics.ConcretedMap",
//                    "generics.TemplatedMapList",
//                    "generics.TemplatedOptional",
//                    "generics.AbstractRecordOneParam",
//                    "generics.useof.SomeKey",
                    "scalars.constraints.range.Date"
            }));

    @Test
    public void run() throws Exception {
        AlfaCompiler sc = new AlfaCompiler(new StdoutLogger());
        ICompilationUnitArtifact s = sc.compile(itestFiles);

        if (s instanceof AlfaCompiler.ErrCompilationUnitArtifact) {
            AlfaCompiler.ErrCompilationUnitArtifact err = (AlfaCompiler.ErrCompilationUnitArtifact) s;
            throw new RuntimeException(err.stacktrace());
        } else {
            CompilationUnitArtifact cua = (CompilationUnitArtifact) s;
            String[] allDefs = cua.context().registry().allUserDeclarations();

            for (String ds : allDefs) {
                if (skipList.contains(ds))
                    continue;

                if (ds.startsWith("generics."))
                    continue;

                if (!rand.randomizable(ds))
                    continue;

                Class<?> c = Class.forName(ds);
                if (Library.class.isAssignableFrom(c)) {
                    continue;
                }

                if (ds.equals("scalars.constraints.range.Decimal"))
                    test(ds);
            }
        }
    }

    private void test(String t) throws IOException {
        if (!rand.randomizable(t))
            return;

        System.out.println("-------------- Randomising " + t + " --------------");
        AlfaObject obj = rand.random(t);

        String json = Alfa.jsonCodec().toJsonString(obj);

        System.out.println(json);
        JsonCodecConfig jc = JsonCodecConfig.builder().setRuntimeContext(rand.codecConfig().getRuntimeContext()).build();
        AlfaObject decoded = Alfa.jsonCodec().importObj(jc, new ByteArrayInputStream(json.getBytes()));

        Assert.assertEquals(obj, decoded);

        if (decoded.descriptor().convertableToBuilder()) {
            Builder b = AlfaUtils.toBuilder(BuilderConfig.getInstance(), decoded);
            AlfaObject built = b.build();
            Assert.assertEquals(obj, decoded);
        }
    }
}
