package com.zhekasmirnov.innercore.api.mod.coreengine.builder;

import com.zhekasmirnov.apparatus.minecraft.enums.EnumsJsInjector;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.coreengine.CEHandler;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.mod.executable.CompilerConfig;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.mozilla.javascript.ScriptableObject;

public class CELoader {
    public static Executable loadCoreEngine() throws IOException {
        Executable executable;
        CEExtractor.extractIfNeeded();
        boolean isCompiled = CEExtractor.isCompiledExecutable();
        File execFile = CEExtractor.getExecutableFile();
        if (execFile != null) {
            CompilerConfig config = new CompilerConfig(API.getInstanceByName("AdaptedScript"));
            config.setName("Core Engine");
            if (isCompiled) {
                executable = Compiler.loadDex(execFile, config);
            } else {
                executable = Compiler.compileReader(new FileReader(execFile), config);
            }
            if (executable != null) {
                setupExecutable(executable, execFile.getParentFile());
            }
            return executable;
        }
        return null;
    }

    public static CEHandler loadAndCreateHandler() {
        try {
            Executable exec = loadCoreEngine();
            if (exec != null) {
                return new CEHandler(exec);
            }
            ICLog.e("COREENGINE", "failed to create handler, core engine executable is null", new RuntimeException());
            return null;
        } catch (Exception e) {
            ICLog.e("COREENGINE", "failed to create handler, compilation of IO error occurred", e);
            return null;
        }
    }

    private static void setupExecutable(Executable executable, File directory) {
        ScriptableObject additionalScope = ScriptableObjectHelper.createEmpty();
        additionalScope.put("__version__", additionalScope, Integer.valueOf(MinecraftVersions.getCurrent().getCode()));
        additionalScope.put("__name__", additionalScope, "core-engine");
        additionalScope.put("__dir__", additionalScope, directory.getAbsolutePath());
        new EnumsJsInjector(additionalScope, true).injectAllEnumScopes("E");
        executable.addToScope(additionalScope);
    }
}
