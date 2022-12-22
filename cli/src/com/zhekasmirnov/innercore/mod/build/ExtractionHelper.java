package com.zhekasmirnov.innercore.mod.build;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.api.runtime.other.PrintStacking;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.mod.executable.CompilerConfig;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import com.zhekasmirnov.innercore.modpack.DirectorySetRequestHandler;
import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import com.zhekasmirnov.innercore.utils.FileTools;
import com.zhekasmirnov.innercore.utils.IMessageReceiver;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class ExtractionHelper {
    public static final String TEMP_DIR = String.valueOf(FileTools.DIR_WORK) + "temp/extract/";
    private static ArrayList<String> extractionPathList;
    private static String lastLocation;

    static {
        FileTools.assureDir(TEMP_DIR);
        extractionPathList = new ArrayList<>();
    }

    static String searchForSubPath(ZipFile modArchiveFile, String searchFor) {
        ZipEntry entry;
        Enumeration<? extends ZipEntry> entries = modArchiveFile.entries();
        while (entries.hasMoreElements() && (entry = entries.nextElement()) != null) {
            String name = entry.getName();
            Logger.debug("DEBUG", "searching: " + name);
            if (name.endsWith(searchFor)) {
                return name.substring(0, name.length() - searchFor.length());
            }
        }
        return null;
    }

    private static String getFreeLocation(String defaultLocation) {
        ModPack modPack = ModPackContext.getInstance().getCurrentModPack();
        DirectorySetRequestHandler handler = modPack.getRequestHandler(ModPackDirectory.DirectoryType.MODS);
        File directory = handler.get(defaultLocation);
        int suffix = 0;
        while (directory.exists()) {
            StringBuilder sb = new StringBuilder();
            sb.append(defaultLocation);
            sb.append("-");
            suffix++;
            sb.append(suffix);
            directory = handler.get(sb.toString());
        }
        if (suffix == 0) {
            return defaultLocation;
        }
        return String.valueOf(defaultLocation) + "-" + suffix;
    }

    private static File getInstallationPath(String locationName) {
        ModPack modPack = ModPackContext.getInstance().getCurrentModPack();
        return modPack.getRequestHandler(ModPackDirectory.DirectoryType.MODS).get(locationName);
    }

    static String extractAs(ZipFile modArchiveFile, String subPath, String dirName) throws IOException {
        ZipEntry entry = null;
        if (dirName == null || dirName.length() == 0 || dirName.indexOf(92) != -1 || dirName.indexOf(47) != -1) {
            throw new IllegalArgumentException("invalid directory name passed to the method extractAs: '" + dirName + "', it must be not empty and must not contain '\\' or '/' symbols");
        }
        String path = getInstallationPath(dirName).getAbsolutePath();
        byte[] buffer = new byte[1024];
        Enumeration<? extends ZipEntry> entries = modArchiveFile.entries();
        while (true) {
            try {
                entry = entries.nextElement();
            } catch (NoSuchElementException e) {
            }
            if (entry != null) {
                String name = entry.getName();
                if (name.startsWith(subPath) && !name.contains(".setup/")) {
                    String name2 = name.substring(subPath.length());
                    if (!entry.isDirectory()) {
                        File out = new File(path, name2);
                        FileTools.assureFileDir(out);
                        InputStream inStream = modArchiveFile.getInputStream(entry);
                        FileOutputStream outStream = new FileOutputStream(out);
                        while (true) {
                            int count = inStream.read(buffer);
                            if (count == -1) {
                                break;
                            }
                            outStream.write(buffer, 0, count);
                        }
                        outStream.close();
                        inStream.close();
                    }
                }
            } else {
                extractionPathList.add(path);
                return path;
            }
        }
    }

    static void extractEntry(ZipFile modArchiveFile, String subPath, String entryName, String target) throws IOException {
        ZipEntry entry = modArchiveFile.getEntry(String.valueOf(subPath) + entryName);
        if (entry == null) {
            throw new IllegalArgumentException("entry " + subPath + entryName + " does not exist for file " + modArchiveFile);
        }
        FileTools.assureFileDir(new File(target));
        Logger.debug("DEBUG", "started entry extraction " + subPath + entryName);
        byte[] buffer = new byte[1024];
        InputStream inStream = modArchiveFile.getInputStream(entry);
        FileOutputStream outStream = new FileOutputStream(target);
        while (true) {
            int count = inStream.read(buffer);
            if (count != -1) {
                outStream.write(buffer, 0, count);
            } else {
                outStream.close();
                inStream.close();
                return;
            }
        }
    }

    static void runSetupScript(final ZipFile modArchiveFile, final String subPath, File setupScriptFile, final String defaultDir, final IMessageReceiver logger) throws Exception {
        FileReader reader = new FileReader(setupScriptFile);
        CompilerConfig config = new CompilerConfig(null);
        Executable setupScript = Compiler.compileReader(reader, config);
        ScriptableObject scope = setupScript.getScope();
        scope.put("extractAs", scope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] args) {
                String dir;
                String name = args.length > 0 ? (String) args[0] : null;
                if (name != null) {
                    dir = name;
                } else {
                    dir = defaultDir;
                }
                IMessageReceiver iMessageReceiver = logger;
                iMessageReceiver.message("extracting mod to ...mods/" + dir);
                try {
                    return ExtractionHelper.extractAs(modArchiveFile, subPath, dir);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        scope.put("unpack", scope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] args) {
                try {
                    ExtractionHelper.extractEntry(modArchiveFile, subPath, String.valueOf(args[0]), String.valueOf(args[1]));
                    return null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        scope.put("log", scope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                StringBuilder output = new StringBuilder();
                for (Object obj : objects) {
                    output.append(obj);
                    output.append(" ");
                }
                logger.message(output.toString());
                return null;
            }
        });
        scope.put("print", scope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                for (Object obj : objects) {
                    PrintStacking.print(new StringBuilder().append(obj).toString());
                }
                return null;
            }
        });
        scope.put("__modsdir__", scope, String.valueOf(FileTools.DIR_WORK) + "mods/");
        scope.put("__subpath__", scope, subPath);
        setupScript.run();
        Throwable throwable = setupScript.getLastRunException();
        if (throwable != null) {
            throw new RuntimeException(throwable);
        }
    }

    public static synchronized ArrayList<String> extractICModFile(File file, IMessageReceiver logger, Runnable readyToInstallCallback) {
        String defaultDir;
        logger.message("preparing to install " + file.getName());
        extractionPathList.clear();
        try {
            try {
                ZipFile modArchiveFile = new ZipFile(file, Charset.forName("UTF-8"));
                String subPath = searchForSubPath(modArchiveFile, "build.config");
                if (subPath == null) {
                    logger.message("mod archive has incorrect structure: build.config file was not found anywhere");
                    return null;
                }
                logger.message("mod installation dir was found at path '/" + subPath + "'");
                char c = 1;
                String[][] files = {new String[]{"cfg", "build.config"}, new String[]{"icon", "mod_icon.png"}, new String[]{"info", "mod.info"}};
                logger.message("extracting installation files");
                int length = files.length;
                int i = 0;
                while (i < length) {
                    String[] next = files[i];
                    File tmp = new File(TEMP_DIR, (String) next[0]);
                    if (tmp.exists()) {
                        tmp.delete();
                    }
                    try {
                        extractEntry(modArchiveFile, subPath, next[c == 1 ? 1 : 0], String.valueOf(TEMP_DIR) + next[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    i++;
                    c = 1;
                }
                BuildConfig buildConfig = new BuildConfig(new File(TEMP_DIR, "cfg"));
                if (!buildConfig.read()) {
                    logger.message("build config cannot be loaded correctly, it failed to extract or was corrupted");
                    return null;
                }
                logger.message("we are ready to install");
                if (readyToInstallCallback != null) {
                    readyToInstallCallback.run();
                }
                String setupScriptDir = buildConfig.defaultConfig.setupScriptDir;
                if (subPath.length() > 0) {
                    int slashIndex = Math.max(subPath.indexOf(47), subPath.indexOf(92));
                    defaultDir = subPath.substring(0, slashIndex != -1 ? slashIndex : subPath.length());
                } else {
                    String defaultDir2 = file.getName();
                    if (defaultDir2.endsWith(".icmod")) {
                        defaultDir = defaultDir2.substring(0, defaultDir2.length() - 6);
                    } else {
                        defaultDir = defaultDir2;
                    }
                }
                String defaultDir3 = getFreeLocation(defaultDir);
                logger.message("installing mod (default directory name is '" + defaultDir3 + "', but it probably will change).");
                if (setupScriptDir != null) {
                    try {
                        extractEntry(modArchiveFile, subPath, setupScriptDir, String.valueOf(TEMP_DIR) + "setup");
                        logger.message("running setup script");
                        runSetupScript(modArchiveFile, subPath, new File(TEMP_DIR, "setup"), defaultDir3, logger);
                        lastLocation = defaultDir3;
                        return extractionPathList;
                    } catch (Exception e2) {
                        logger.message("failed to extract setup script: " + e2);
                        return null;
                    }
                }
                try {
                    logger.message("extracting mod to ...mods/" + defaultDir3);
                    extractAs(modArchiveFile, subPath, defaultDir3);
                    lastLocation = defaultDir3;
                    return extractionPathList;
                } catch (IOException e3) {
                    logger.message("failed to extract mod archive: " + e3);
                    return null;
                }
            } catch (IOException e4) {
                logger.message("io exception occurred: " + e4);
                e4.printStackTrace();
                return null;
            }
        } catch (Exception e5) {
            logger.message("mod archive is corrupt: " + e5);
            e5.printStackTrace();
            return null;
        }
    }

    public static String getLastLocation() {
        return lastLocation;
    }
}
