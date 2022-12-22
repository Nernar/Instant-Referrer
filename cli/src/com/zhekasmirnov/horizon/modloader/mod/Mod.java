package com.zhekasmirnov.horizon.modloader.mod;

import com.zhekasmirnov.horizon.modloader.ExecutionDirectory;
import com.zhekasmirnov.horizon.modloader.ModContext;
import com.zhekasmirnov.horizon.modloader.configuration.Configuration;
import com.zhekasmirnov.horizon.modloader.configuration.ConfigurationFile;
import com.zhekasmirnov.horizon.modloader.java.JavaDirectory;
import com.zhekasmirnov.horizon.modloader.library.Library;
import com.zhekasmirnov.horizon.modloader.library.LibraryDirectory;
import com.zhekasmirnov.horizon.modloader.repo.location.ModLocation;
import com.zhekasmirnov.horizon.modloader.resource.ResourceManager;
import com.zhekasmirnov.horizon.modloader.resource.directory.ResourceDirectory;
import com.zhekasmirnov.horizon.runtime.logger.EventLogger;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;

public class Mod {
    private ConfigurationInterface configInterface;
    private final ModContext context;
    private DeveloperInterface devInterface;
    public final File directory;
    private final ExecutionDirectory executionDirectory;
    private Object graphics;
    public final ModManifest manifest;
    private final ResourceManager resourceManager;
    private SafetyInterface safetyInterface;
    public final List<LibraryDirectory> libraries = new ArrayList<>();
    public final List<ResourceDirectory> resources = new ArrayList<>();
    public final List<JavaDirectory> java = new ArrayList<>();
    public final List<Module> modules = new ArrayList<>();
    public final List<ModInstance> modInstances = new ArrayList<>();
    public final List<ModLocation> subModLocations = new ArrayList<>();

    public Mod(ModContext modContext, File file) {
        try {
            this.manifest = new ModManifest(new File(file, "manifest"));
            this.directory = file;
            this.context = modContext;
            this.executionDirectory = modContext.executionDirectory;
            this.resourceManager = modContext.resourceManager;
            this.devInterface = new DeveloperInterface();
            this.configInterface = new ConfigurationInterface();
            this.safetyInterface = new SafetyInterface();
            for (ModManifest.Directory directory : this.manifest.getDirectories()) {
                int i = AnonymousClass1.$SwitchMap$com$zhekasmirnov$horizon$modloader$mod$ModManifest$DirectoryType[directory.type.ordinal()];
                if (i == 1) {
                    this.libraries.add(new LibraryDirectory(this, directory.file));
                } else if (i == 2) {
                    this.java.add(new JavaDirectory(this, directory.file));
                } else if (i == 3) {
                    this.resources.add(new ResourceDirectory(this.resourceManager, this, directory.file));
                } else if (i == 4) {
                    this.subModLocations.add(directory.asModLocation());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("failed to read mod manifest for " + file, e);
        } catch (JSONException e2) {
            throw new RuntimeException("failed to read mod manifest for " + file, e2);
        }
    }

    static class AnonymousClass1 {
        static final int[] $SwitchMap$com$zhekasmirnov$horizon$modloader$mod$ModManifest$DirectoryType;

        AnonymousClass1() {
        }

        static {
            int[] iArr = new int[ModManifest.DirectoryType.valuesCustom().length];
            $SwitchMap$com$zhekasmirnov$horizon$modloader$mod$ModManifest$DirectoryType = iArr;
            try {
                iArr[ModManifest.DirectoryType.LIBRARY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$zhekasmirnov$horizon$modloader$mod$ModManifest$DirectoryType[ModManifest.DirectoryType.JAVA.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$zhekasmirnov$horizon$modloader$mod$ModManifest$DirectoryType[ModManifest.DirectoryType.RESOURCE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$zhekasmirnov$horizon$modloader$mod$ModManifest$DirectoryType[ModManifest.DirectoryType.SUBMOD.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public void inject() {
        for (LibraryDirectory libraryDirectory : this.libraries) {
            this.executionDirectory.addLibraryDirectory(libraryDirectory);
        }
        for (JavaDirectory javaDirectory : this.java) {
            this.executionDirectory.addJavaDirectory(javaDirectory);
        }
        for (ResourceDirectory resourceDirectory : this.resources) {
            this.resourceManager.addResourceDirectory(resourceDirectory);
        }
    }

    public void initialize() {
        this.modules.clear();
        this.modInstances.clear();
        for (LibraryDirectory libraryDirectory : this.libraries) {
            Library library = libraryDirectory.getLibrary();
            if (library != null) {
                for (Module module : library.getModules()) {
                    this.modules.add(module);
                    if (module.isMod()) {
                        this.modInstances.add(new ModInstance(module));
                    }
                }
            }
        }
    }

    public String toString() {
        return "[Mod name=" + getDisplayedName() + " version=" + this.manifest.getMainModule().versionName + " dir=" + this.directory + "]";
    }

    public String getDisplayedName() {
        return this.manifest.getName();
    }

    public class ConfigurationInterface {
        public Configuration configuration;

        public ConfigurationInterface() {
            ConfigurationFile configurationFile = new ConfigurationFile(new File(Mod.this.directory, "config.json"), false);
            this.configuration = configurationFile;
            configurationFile.refresh();
            this.configuration.checkAndRestore("\n            {\n                \"enabled\": true\n            }");
            this.configuration.save();
        }

        public boolean isActive() {
            return this.configuration.getBoolean("enabled");
        }

        public void setActive(boolean z) {
            this.configuration.set("enabled", Boolean.valueOf(z));
            this.configuration.save();
        }
    }

    public ConfigurationInterface getConfigurationInterface() {
        return this.configInterface;
    }

    public class DeveloperInterface {
        public DeveloperInterface() {
        }

        public void toProductionMode(EventLogger eventLogger) {
            for (LibraryDirectory next : Mod.this.libraries) {
                next.compileToTargetFile(Mod.this.executionDirectory, Mod.this.context.getActivityContext(), next.soFile);
                next.setPreCompiled(false);
            }
            for (JavaDirectory next2 : Mod.this.java) {
                next2.compileToClassesFile(Mod.this.context.getActivityContext());
                next2.setPreCompiled(false);
            }
        }

        public void toDeveloperMode() {
            for (LibraryDirectory libraryDirectory : Mod.this.libraries) {
                if (!libraryDirectory.isPreCompiled() && libraryDirectory.soFile.exists()) {
                    libraryDirectory.soFile.delete();
                }
            }
            for (JavaDirectory javaDirectory : Mod.this.java) {
                if (!javaDirectory.isPreCompiled()) {
                    for (File file : javaDirectory.getCompiledClassesFiles()) {
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }
            }
        }

        public boolean toProductModeUiProtocol() {
            toProductionMode(Mod.this.context.getEventLogger());
            List<EventLogger.Message> messages = Mod.this.context.getEventLogger().getMessages(new EventLogger.Filter() {
                @Override
                public boolean filter(EventLogger.Message message) {
                    return message.type == EventLogger.MessageType.EXCEPTION || message.type == EventLogger.MessageType.FAULT;
                }
            });
            Mod.this.context.getEventLogger().clear();
            boolean hasAnyException = false;
            for (EventLogger.Message message : messages) {
                hasAnyException = true;
                Logger.error(message.tag, message.message);
            }
            return !hasAnyException;
        }

        public boolean anyForDeveloperModeTransfer() {
            for (LibraryDirectory libraryDirectory : Mod.this.libraries) {
                if (!libraryDirectory.isPreCompiled() && libraryDirectory.soFile.exists()) {
                    return true;
                }
            }
            for (JavaDirectory javaDirectory : Mod.this.java) {
                File compiledClassesFile = javaDirectory.getCompiledClassesFile();
                if (!javaDirectory.isPreCompiled() && compiledClassesFile.exists()) {
                    return true;
                }
            }
            return false;
        }

        public boolean anyForProductionModeTransfer() {
            for (LibraryDirectory libraryDirectory : Mod.this.libraries) {
                if (!libraryDirectory.soFile.exists()) {
                    return true;
                }
            }
            for (JavaDirectory javaDirectory : Mod.this.java) {
                if (!javaDirectory.getCompiledClassesFile().exists()) {
                    return true;
                }
            }
            return false;
        }
    }

    public DeveloperInterface getDeveloperInterface() {
        return this.devInterface;
    }

    public class SafetyInterface {
        public static final String CRASH_DISABLED_LOCK = ".crash-disabled-lock";
        public static final String CRASH_LOCK = ".crash-lock";
        private boolean isInUnsafeSection = false;

        public SafetyInterface() {
        }

        public boolean getLock(String str) {
            return new File(Mod.this.directory, str).exists();
        }

        public boolean setLock(String str, boolean z) {
            File file = new File(Mod.this.directory, str);
            if (z && !file.exists()) {
                try {
                    return file.createNewFile();
                } catch (IOException e) {
                    return false;
                }
            } else if (file.exists()) {
                return file.delete();
            } else {
                return true;
            }
        }

        public boolean beginUnsafeSection() {
            if (this.isInUnsafeSection) {
                return true;
            }
            if (getLock(".crash-lock")) {
                return false;
            }
            this.isInUnsafeSection = true;
            return setLock(".crash-lock", true);
        }

        public boolean endUnsafeSection() {
            if (this.isInUnsafeSection) {
                if (getLock(".crash-lock")) {
                    this.isInUnsafeSection = false;
                    return setLock(".crash-lock", false);
                }
                return false;
            }
            return true;
        }

        public boolean isInUnsafeSection() {
            return this.isInUnsafeSection;
        }

        public boolean isCrashRegistered() {
            return !this.isInUnsafeSection && getLock(".crash-lock");
        }

        public boolean removeCrashLock() {
            if (isCrashRegistered()) {
                return setLock(".crash-lock", false);
            }
            return false;
        }

        public boolean isDisabledDueToCrash() {
            return getLock(".crash-disabled-lock");
        }

        public boolean setDisabledDueToCrash(boolean z) {
            return setLock(".crash-disabled-lock", z);
        }
    }

    public SafetyInterface getSafetyInterface() {
        return this.safetyInterface;
    }

    public Object getGraphics() {
        return this.graphics;
    }
}
