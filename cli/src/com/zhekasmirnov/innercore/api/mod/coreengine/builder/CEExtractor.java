package com.zhekasmirnov.innercore.api.mod.coreengine.builder;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CEExtractor {
    @SuppressWarnings("unused")
    private static final boolean CE_DEBUG = true;
    @SuppressWarnings("unused")
    private static final boolean COMPILE = false;
    @SuppressWarnings("unused")
    private static final String PATH_IN_ASSETS = "innercore/coreengine/";
    private static boolean isExtracted = false;
    private static final String DIR_CORE_ENGINE = String.valueOf(FileTools.DIR_WORK) + "coreengine/";
    private static boolean isExtractionSucceeded = false;

    private static boolean unpackAsset(String name, String dir) {
        String dir2 = String.valueOf(DIR_CORE_ENGINE) + dir;
        FileTools.assureFileDir(new File(dir2));
        try {
            FileTools.unpackAsset("innercore/coreengine/" + name, dir2);
            return true;
        } catch (IOException e) {
            ICLog.e("COREENGINE", "unpacking core engine file failed name=" + name, e);
            return false;
        }
    }

    private static boolean unpackAsset(String name) {
        return unpackAsset(name, name);
    }

    private static void prepareExtraction() {
        FileTools.assureDir(DIR_CORE_ENGINE);
    }

    @SuppressWarnings("unused")
    private static boolean tryToCompile() {
        long start = System.currentTimeMillis();
        ICLog.i("CORE-ENGINE", "starting compilation of Core Engine");
        try {
            FileReader fileReader = new FileReader(new File(String.valueOf(DIR_CORE_ENGINE) + "core-engine.dev.js"));
            Compiler.compileScriptToFile(fileReader, "core-engine", String.valueOf(DIR_CORE_ENGINE) + "core-engine.script");
            long end = System.currentTimeMillis();
            ICLog.i("CORE-ENGINE", "successfully compiled in " + (end - start) + " ms");
            return true;
        } catch (IOException e) {
            ICLog.e("CORE-ENGINE", "compilation failed", e);
            return false;
        }
    }

    @SuppressWarnings("unused")
    private static boolean tryReleaseBuild() {
        return unpackAsset("core-engine.script");
    }

    public static void extractIfNeeded() {
        if (!isExtracted) {
            isExtracted = true;
            prepareExtraction();
            isExtractionSucceeded = true;
        }
    }

    public static boolean isCompiledExecutable() {
        return false;
    }

    public static File getExecutableFile() {
        if (isExtractionSucceeded) {
            return new File(DIR_CORE_ENGINE, "core-engine.dev.js");
        }
        return null;
    }

    public static boolean isExtracted() {
        return isExtracted;
    }
}
