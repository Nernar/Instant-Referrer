package com.zhekasmirnov.mcpe161;

import com.zhekasmirnov.horizon.launcher.pack.Pack;
import com.zhekasmirnov.horizon.modloader.java.JavaDirectory;
import com.zhekasmirnov.horizon.modloader.library.LibraryDirectory;
import com.zhekasmirnov.horizon.modloader.resource.ResourceManager;
import com.zhekasmirnov.horizon.modloader.resource.directory.ResourceDirectory;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.resource.types.enums.TextureType;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import org.mineprogramming.horizon.innercore.util.DencityConverter;

public class EnvironmentSetup {
    private static TextureAtlas blockTextureAtlas;
    private static InnerCore innerCore;
    private static TextureAtlas itemTextureAtlas;

    public static void abortLaunchIfRequired(Pack pack) {
        boolean aborted = !InnerCore.checkLicence(null);
        if (aborted) {
            pack.abortLaunch();
        }
    }

    private static void disableSslCertificateChecking() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            ICLog.e("ERROR", "failed to disable certificate check", e);
        }
    }

    public static void prepareForInjection(Pack pack) {
        innerCore = new InnerCore(null, pack);
        innerCore.load();
    }

    public static void prepareForLaunch(Pack pack) {
        File installationPackage = new File(pack.directory, ".installation_package");
        if (installationPackage.isFile()) {
            installationPackage.delete();
        }
        File noMediaFile = new File(pack.directory, ".nomedia");
        if (!noMediaFile.exists()) {
            try {
                noMediaFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File logsDir = new File(System.getProperty("user.dir"), "games/horizon/logs");
        if (!logsDir.isDirectory()) {
            logsDir.delete();
            logsDir.mkdirs();
        }
        File[] logFiles = logsDir.listFiles();
        if (logFiles != null) {
            for (File file : logsDir.listFiles()) {
                if (file.getName().startsWith("crash.txt")) {
                    try {
                        int hash = FileUtils.readFileText(file).hashCode();
                        file.renameTo(new File(file.getParent(), "archived-crash-" + hash + ".txt"));
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        file.delete();
                    }
                }
            }
        }
    }

    public static void instantLaunch(Pack pack) {
        NativeCallback.onFinalInitStarted();
        NativeCallback.onFinalInitComplete();
    }

    public static Object getCurrentActivity() {
        return null;
    }

    private static void reportResourceSetupError(String message, Throwable err) {
        ICLog.e("Resource-Setup-Error", message, err);
        err.printStackTrace();
    }

    public static void setupResourceManager(ResourceManager manager) {
        blockTextureAtlas = new TextureAtlas(TextureType.BLOCK, "textures/terrain_texture.json", "block-atlas-descriptor", "terrain-atlas");
        manager.addResourceProcessor(blockTextureAtlas);
        manager.addRuntimeResourceHandler(blockTextureAtlas);
        itemTextureAtlas = new TextureAtlas(TextureType.ITEM, "textures/item_texture.json", "item-atlas-descriptor", "items-opaque");
        manager.addResourceProcessor(itemTextureAtlas);
        manager.addRuntimeResourceHandler(itemTextureAtlas);
        try {
            FlipbookTextureAtlas flipbookTextureAtlas = new FlipbookTextureAtlas("textures/flipbook_textures.json", "flipbook-texture-descriptor");
            manager.addResourceProcessor(flipbookTextureAtlas);
            manager.addRuntimeResourceHandler(flipbookTextureAtlas);
        } catch (Throwable err) {
            reportResourceSetupError("Failed to initialize flipbook texture atlas descriptor", err);
        }
        try {
            MaterialProcessor materialProcessor = new MaterialProcessor("materials/entity.material", "material-override-entity", "custom-materials", "custom-shaders");
            manager.addRuntimeResourceHandler(materialProcessor.newShaderUniformList("uniforms.json", "shader-uniforms-override"));
            manager.addResourceProcessor(materialProcessor);
            manager.addRuntimeResourceHandler(materialProcessor);
        } catch (Throwable err2) {
            reportResourceSetupError("Failed to initialize material processor", err2);
        }
        try {
            ContentProcessor contentProcessor = new ContentProcessor();
            manager.addResourceProcessor(contentProcessor);
            manager.addRuntimeResourceHandler(contentProcessor);
            manager.addResourcePrefixes("resource_packs/vanilla/");
        } catch (Throwable err3) {
            reportResourceSetupError("Failed to initialize main content manager, you should restart app", err3);
        }
    }

    public static void addEnvironmentLibraries(ArrayList<File> libraries, File root) {
    }

    public static void getAdditionalNativeDirectories(Pack pack, ArrayList<LibraryDirectory> nativeDirectories) {
        innerCore.addNativeDirectories(nativeDirectories);
    }

    public static void getAdditionalJavaDirectories(Pack pack, ArrayList<JavaDirectory> javaDirectories) {
        innerCore.addJavaDirectories(javaDirectories);
    }

    public static void getAdditionalResourceDirectories(Pack pack, ArrayList<ResourceDirectory> list) {
        innerCore.addResourceDirectories(list);
    }

    public static void addMenuActivities(Pack pack, ArrayList<Pack.MenuActivityFactory> activities) {
        disableSslCertificateChecking();
        FileTools.initializeDirectories(pack.directory);
        DencityConverter.initializeDensity(null);
        FileTools.initializeDirectories(pack.getWorkingDirectory());
    }

    static TextureAtlas getBlockTextureAtlas() {
        return blockTextureAtlas;
    }

    static TextureAtlas getItemTextureAtlas() {
        return itemTextureAtlas;
    }
}
