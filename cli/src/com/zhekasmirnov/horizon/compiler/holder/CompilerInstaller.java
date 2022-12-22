package com.zhekasmirnov.horizon.compiler.holder;

import com.zhekasmirnov.horizon.activity.util.DialogHelper;
import com.zhekasmirnov.horizon.compiler.exceptions.BadArchiveException;
import com.zhekasmirnov.horizon.compiler.packages.Environment;
import com.zhekasmirnov.horizon.compiler.packages.PackageInfo;
import com.zhekasmirnov.horizon.compiler.packages.PackageInstaller;
import com.zhekasmirnov.horizon.compiler.packages.RepoParser;
import com.zhekasmirnov.horizon.compiler.packages.RepoUtils;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.horizon.util.JsonIterator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.json.JSONException;
import org.json.JSONObject;

public class CompilerInstaller {
    static final boolean $assertionsDisabled = false;
    @SuppressWarnings("unused")
    private static final String MANIFEST = "manifest.json";
    @SuppressWarnings("unused")
    private static final String REPOSITORY_URL = "https://gitlab.com/zheka2304/horizon-assets/raw/master/toolchain/gcc/";
    public static final int TASK_CLEANUP = 1;
    public static final int TASK_DOWNLOAD = 4;
    public static final int TASK_INSTALL = 8;
    public static final int TASK_SEARCH_PACKAGES = 2;
    private final Object context;
    private final File downloadDir;
    private final File installationDir;
    private final List<ZipFile> blocks = new ArrayList<>();
    private File packageXmlFile = null;

    enum InstallationStatus {
        COMPLETED,
        TERMINATED,
        FAILED;

        public static InstallationStatus[] valuesCustom() {
            InstallationStatus[] valuesCustom = values();
            int length = valuesCustom.length;
            InstallationStatus[] installationStatusArr = new InstallationStatus[length];
            System.arraycopy(valuesCustom, 0, installationStatusArr, 0, length);
            return installationStatusArr;
        }
    }

    public CompilerInstaller(Object activity, File file) {
        this.context = activity;
        this.downloadDir = file;
        this.installationDir = new File(Environment.getToolchainsDir(activity));
    }

    public static void downloadFile(String inputStream, File file, final DialogHelper.ProgressInterface progressInterface) {
        if (file.exists()) {
            file.delete();
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("downloading file ");
        sb.append(file);
        sb.append(" from ");
        sb.append(inputStream);
        Logger.debug("CompilerInstaller", sb.toString());
        Object o = null;
        Object o2 = null;
        Object cause = null;
        HttpURLConnection _httpURLConnection = null;
        InputStream _stream = null;
        try {
            _httpURLConnection = (HttpURLConnection)new URL(inputStream).openConnection();
            try {
                _httpURLConnection.connect();
                if (_httpURLConnection.getResponseCode() == 200) {
                    final int contentLength = _httpURLConnection.getContentLength();
                    _stream = _httpURLConnection.getInputStream();
                    try {
                        o2 = new FileOutputStream(file);
                        try {
                            o = new byte[1048576];
                            long n = 0L;
                            while (true) {
                                final int read = _stream.read((byte[])o);
                                if (read == -1) {
                                    break;
                                }
                                if (progressInterface.isTerminated()) {
                                    _stream.close();
                                    file.delete();
                                    break;
                                }
                                n += read;
                                if (contentLength > 0) {
                                    progressInterface.onProgress(n / (float)contentLength);
                                }
                                ((OutputStream)o2).write((byte[])o, 0, read);
                            }
                            try {
                                ((OutputStream)o2).close();
                                if (_stream != null) {
                                    _stream.close();
                                }
                            }
                            catch (final IOException ex) {}
                            if (_httpURLConnection != null) {
                                _httpURLConnection.disconnect();
                            }
                            return;
                        }
                        catch (final Exception ex2) {}
                    }
                    catch (final Exception ex3) {}
                }
                final StringBuilder sb2 = new StringBuilder();
                sb2.append("invalid http-code returned code=");
                sb2.append(_httpURLConnection.getResponseCode());
                sb2.append(" message=");
                sb2.append(_httpURLConnection.getResponseMessage());
                sb2.append(" url=");
                sb2.append(inputStream);
                throw new RuntimeException(sb2.toString());
            }
            catch (final Exception ex4) {}
        }
        catch (final Exception _cause) {
            file = (File)o2;
            cause = _cause;
        }
        if (o2 != null) {
            try {
                ((OutputStream)o2).close();
            }
            catch (final IOException ex5) {
            }
        }
        if (_stream != null) {
            try {
                _stream.close();
            } catch (IOException e) {}
        }
        if (_httpURLConnection != null) {
            _httpURLConnection.disconnect();
        }
        throw new RuntimeException((Exception) cause);
    }

    private void downloadFile(String str, File file) {
        downloadFile(str, file, new DialogHelper.ProgressInterface() {
            @Override
            public boolean isTerminated() {
                return false;
            }

            @Override
            public void onProgress(double d) {
            }
        });
    }

    private JSONObject getManifest(boolean z) {
        File file = new File(this.downloadDir, "manifest");
        if (z) {
            try {
                downloadFile("https://gitlab.com/zheka2304/horizon-assets/raw/master/toolchain/gcc/manifest.json", file);
                return FileUtils.readJSON(file);
            } catch (Throwable th) {
                file.delete();
                throw new RuntimeException(th);
            }
        } else if (file.exists()) {
            try {
                return FileUtils.readJSON(file);
            } catch (IOException | JSONException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    private void clearInstallation(DialogHelper.ProgressDialogHolder progressDialogHolder) {
        FileUtils.clearFileTree(this.installationDir, false);
    }

    private void clearDownload(DialogHelper.ProgressDialogHolder progressDialogHolder) {
        this.blocks.clear();
        this.packageXmlFile = null;
        FileUtils.clearFileTree(this.downloadDir, false);
    }

    private void clear(DialogHelper.ProgressDialogHolder progressDialogHolder) {
        clearInstallation(progressDialogHolder);
        clearDownload(progressDialogHolder);
    }

    private void download(DialogHelper.ProgressDialogHolder progressDialogHolder) {
        Logger.debug("CompilerInstaller", "starting download");
        clearDownload(progressDialogHolder);
        JSONObject manifest = getManifest(true);
        this.packageXmlFile = new File(this.downloadDir, "packages.xml");
        try {
            downloadFile("https://gitlab.com/zheka2304/horizon-assets/raw/master/toolchain/gcc/" + manifest.getString("packages-xml"), this.packageXmlFile);
            Logger.debug("CompilerInstaller", "blocks to download: " + manifest.optJSONArray("blocks"));
            Iterator<Object> it = new JsonIterator<>(manifest.optJSONArray("blocks")).iterator();
            while (it.hasNext()) {
                String str = (String) it.next();
                File file = new File(this.downloadDir, str);
                if (file.exists()) {
                    file.delete();
                }
                if (progressDialogHolder.isTerminated()) {
                    return;
                }
                progressDialogHolder.onDownloadMessage("downloading " + str);
                downloadFile("https://gitlab.com/zheka2304/horizon-assets/raw/master/toolchain/gcc/" + str, file, progressDialogHolder);
                if (progressDialogHolder.isTerminated()) {
                    this.packageXmlFile = null;
                    return;
                }
                this.blocks.add(new ZipFile(file));
            }
        } catch (Throwable th2) {
            this.packageXmlFile.delete();
            this.packageXmlFile = null;
            throw new RuntimeException(th2);
        }
    }

    private void search(DialogHelper.ProgressDialogHolder progressDialogHolder) {
        File file = new File(this.downloadDir, "packages.xml");
        this.packageXmlFile = file;
        if (!file.exists()) {
            this.packageXmlFile = null;
        }
        this.blocks.clear();
        JSONObject manifest = getManifest(false);
        if (manifest != null) {
            Iterator<Object> it = new JsonIterator<>(manifest.optJSONArray("blocks")).iterator();
            while (it.hasNext()) {
                try {
                    this.blocks.add(new ZipFile(new File(this.downloadDir, (String) it.next())));
                } catch (Throwable th) {
                    this.packageXmlFile = null;
                    throw new RuntimeException(th);
                }
            }
        }
    }

    public boolean areInstallationPackagesFound() {
        return this.packageXmlFile != null;
    }

    private void install(DialogHelper.ProgressDialogHolder progressDialogHolder) {
        clearInstallation(progressDialogHolder);
        File file = new File(this.downloadDir, ".temp.zip");
        try {
            RepoUtils.setVersion();
            PackageInstaller packageInstaller = new PackageInstaller(this.context);
            List<PackageInfo> parseRepoXml = new RepoParser().parseRepoXml(FileUtils.readFileText(this.packageXmlFile));
            int i = 0;
            for (PackageInfo packageInfo : parseRepoXml) {
                if (progressDialogHolder.isTerminated()) {
                    return;
                }
                progressDialogHolder.onDownloadMessage("installing " + packageInfo.getFileName());
                int i2 = i + 1;
                double d = i;
                double size = parseRepoXml.size();
                Double.isNaN(d);
                Double.isNaN(size);
                progressDialogHolder.onProgress(d / size);
                InputStream inputStream = null;
                Iterator<ZipFile> it = this.blocks.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ZipFile next = it.next();
                    ZipEntry entry = next.getEntry("repo/" + packageInfo.getFileName());
                    if (entry != null) {
                        Logger.debug("CompilerInstaller", "unpacking package " + packageInfo.getFileName() + " from " + next.getName() + ":" + entry.getName() + " to " + file);
                        inputStream = next.getInputStream(entry);
                        break;
                    }
                }
                if (inputStream != null) {
                    file.getParentFile().mkdirs();
                    FileUtils.unpackInputStream(inputStream, file);
                    Logger.debug("CompilerInstaller", "installing package from " + file);
                    try {
                        packageInstaller.install(file, packageInfo);
                        Logger.debug("CompilerInstaller", "installed package " + packageInfo.getFileName());
                    } catch (BadArchiveException e) {
                        e.printStackTrace();
                    }
                } else {
                    Logger.error("CompilerInstaller", "failed to find package " + packageInfo.getFileName());
                }
                i = i2;
            }
            new File(this.installationDir, ".installation-lock").createNewFile();
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }

    public boolean deleteInstallationLock() {
        return new File(this.installationDir, ".installation-lock").delete();
    }

    public boolean getInstallationLock() {
        return new File(this.installationDir, ".installation-lock").exists();
    }

    public InstallationStatus runInstallationSequence(boolean z) {
        if (z) {
            if (DialogHelper.awaitDecision(2131623976, "GCC Compiler is missing or corrupt, to proceed, you should reinstall it from already downloaded packages", 2131624110, 17039360)) {
                DialogHelper.ProgressDialogHolder progressDialogHolder = new DialogHelper.ProgressDialogHolder(2131624030, 2131623982);
                progressDialogHolder.open();
                progressDialogHolder.setText("Installing GCC compiler packages...");
                deleteInstallationLock();
                Throwable runInstallationTasks = runInstallationTasks(progressDialogHolder, 10);
                progressDialogHolder.close();
                if (runInstallationTasks != null) {
                    Logger.error("CompilerInstaller", "installation task finished with error: " + runInstallationTasks);
                    if (DialogHelper.awaitDecision(2131623976, "GCC Compiler and its installation packages are missing or corrupt, to proceed, you should download and install it again", 2131624110, 17039360)) {
                        DialogHelper.ProgressDialogHolder progressDialogHolder2 = new DialogHelper.ProgressDialogHolder(2131624030, 2131623982);
                        progressDialogHolder2.open();
                        progressDialogHolder2.setText("Installing GCC compiler packages...");
                        Throwable runInstallationTasks2 = runInstallationTasks(progressDialogHolder2, 13);
                        progressDialogHolder2.close();
                        if (runInstallationTasks2 != null) {
                            Logger.error("CompilerInstaller", "installation task finished with error: " + runInstallationTasks2);
                            return InstallationStatus.FAILED;
                        }
                    }
                }
                if (getInstallationLock()) {
                    return InstallationStatus.COMPLETED;
                }
            }
            return InstallationStatus.TERMINATED;
        }
        if (DialogHelper.awaitDecision(2131623976, "To compile C and C++ you need to install GCC compiler (additional 35 MB should be downloaded)", 2131624110, 17039360)) {
            DialogHelper.ProgressDialogHolder progressDialogHolder3 = new DialogHelper.ProgressDialogHolder(2131624030, 2131623982);
            progressDialogHolder3.setText("Installing GCC compiler packages...");
            progressDialogHolder3.open();
            deleteInstallationLock();
            Throwable runInstallationTasks3 = runInstallationTasks(progressDialogHolder3, 13);
            progressDialogHolder3.close();
            if (runInstallationTasks3 != null) {
                Logger.error("CompilerInstaller", "installation task finished with error: " + runInstallationTasks3);
                return InstallationStatus.FAILED;
            } else if (getInstallationLock()) {
                return InstallationStatus.COMPLETED;
            }
        }
        return InstallationStatus.TERMINATED;
    }

    public Throwable runSilentInstallationTasks(int i) {
        return runInstallationTasks(new DialogHelper.ProgressDialogHolder(2131624030, 2131623982), i);
    }

    public Throwable runInstallationTasks(DialogHelper.ProgressDialogHolder progressDialogHolder, int i) {
        if ((i & 1) != 0) {
            try {
                if (progressDialogHolder.isTerminated()) {
                    return null;
                }
                clear(progressDialogHolder);
            } catch (Throwable th) {
                return th;
            }
        }
        if ((i & 4) != 0) {
            if (progressDialogHolder.isTerminated()) {
                return null;
            }
            download(progressDialogHolder);
        }
        if ((i & 2) != 0) {
            if (progressDialogHolder.isTerminated()) {
                return null;
            }
            search(progressDialogHolder);
        }
        if ((i & 8) == 0 || progressDialogHolder.isTerminated()) {
            return null;
        }
        install(progressDialogHolder);
        return null;
    }
}
