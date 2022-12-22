package com.zhekasmirnov.innercore.api.mod.adaptedscript;

import com.zhekasmirnov.horizon.activity.util.DialogHelper;
import com.zhekasmirnov.horizon.compiler.holder.CompilerInstaller;
import com.zhekasmirnov.innercore.api.InnerCoreConfig;
import com.zhekasmirnov.innercore.api.annotations.APIStaticModule;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.build.ExtractionHelper;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.utils.FileTools;
import com.zhekasmirnov.innercore.utils.IMessageReceiver;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSStaticFunction;

public class PreferencesWindowAPI extends AdaptedScriptAPI {
    @Override
    public String getName() {
        return "PrefsWinAPI";
    }

    @JSStaticFunction
    public static void log(String str) {
        ICLog.d("PREFS", str);
    }

    @APIStaticModule
    public static class Prefs {
        @JSStaticFunction
        public static ArrayList<Mod> getModList() {
            return ModLoader.instance.modsList;
        }

        @JSStaticFunction
        public static boolean compileMod(Object mod, Object logger) {
            return Compiler.compileMod((Mod) Context.jsToJava(mod, Mod.class), (IMessageReceiver) Context.jsToJava(logger, IMessageReceiver.class));
        }

        @JSStaticFunction
        public static AdaptedScriptAPI.Config getGlobalConfig() {
            return (AdaptedScriptAPI.Config) InnerCoreConfig.config;
        }

        @JSStaticFunction
        public static ArrayList<String> installModFile(String path, Object _log) {
            IMessageReceiver log = (IMessageReceiver) Context.jsToJava(_log, IMessageReceiver.class);
            return ExtractionHelper.extractICModFile(new File(path), log, null);
        }
    }

    @APIStaticModule
    public static class Network {

        interface IDownloadHandler {
            boolean isCancelled();

            void message(String str);

            void progress(float f);
        }

        @JSStaticFunction
        public static String getURLContents(String sURL) {
            try {
                URL url = new URL(sURL);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                StringBuilder result = new StringBuilder();
                while (true) {
                    String inputLine = in.readLine();
                    if (inputLine != null) {
                        result.append(inputLine);
                    } else {
                        in.close();
                        return result.toString();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @JSStaticFunction
        public static String downloadIcon(String sURL) {
            return "missing_texture";
        }

        @JSStaticFunction
        public static String downloadFile(String spec, Object ex) {
            final IDownloadHandler downloadHandler = (IDownloadHandler) Context.jsToJava(ex, IDownloadHandler.class);
            final String dir_WORK = FileTools.DIR_WORK;
            final StringBuilder sb = new StringBuilder();
            sb.append("temp/download/");
            sb.append(spec.replaceAll("[/\\\\ :.]", "_"));
            sb.append(".icmod");
            final File file = new File(dir_WORK, sb.toString());
            FileTools.assureFileDir(file);
            if (file.exists()) {
                file.delete();
            }
            CompilerInstaller.downloadFile(spec, file, new DialogHelper.ProgressInterface() {
                @Override
                public boolean isTerminated() {
                    if (downloadHandler != null) {
                        return downloadHandler.isCancelled();
                    }
                    return false;
                }

                @Override
                public void onProgress(double d) {
                    if (downloadHandler != null) {
                        downloadHandler.progress((float) d);
                    }
                }
            });
            return file.exists() ? file.getAbsolutePath() : null;
        }
    }

    public static class WorkbenchRecipeListBuilder {
        public WorkbenchRecipeListBuilder(long player, AdaptedScriptAPI.ItemContainer container) {
        }
    }

    public static class WorkbenchRecipeListProcessor {
        public WorkbenchRecipeListProcessor(ScriptableObject target) {
        }
    }
}
