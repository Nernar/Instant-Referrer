package com.zhekasmirnov.horizon.compiler.packages;

import com.pdaxrom.utils.Utils;
import com.zhekasmirnov.horizon.compiler.Shell;
import com.zhekasmirnov.horizon.compiler.exceptions.BadArchiveException;
import com.zhekasmirnov.horizon.compiler.exceptions.NotEnoughCacheException;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import java.io.File;
import java.io.IOException;

public class PackageInstaller {
    @SuppressWarnings("unused")
    private static final String TAG = "PackageInstaller";
    private final String mCCToolsDir;
    private final Object mContext;
    private final String mInstalledDir;
    private final String mToolchainDir;

    public PackageInstaller(Object context) {
        this.mContext = context;
        this.mToolchainDir = Environment.getToolchainsDir(context);
        this.mInstalledDir = Environment.getInstalledPackageDir(this.mContext);
        this.mCCToolsDir = Environment.getCCtoolsDir(this.mContext);
    }

    public void install(File file, PackageInfo packageInfo) throws NotEnoughCacheException, BadArchiveException {
        String absolutePath = file.getAbsolutePath();
        String str = this.mToolchainDir;
        Logger.debug("PackageInstaller", "Unpack file " + absolutePath + " to " + str);
        int unzippedSize = Utils.unzippedSize(absolutePath);
        Logger.debug("PackageInstaller", "Unzipped size " + unzippedSize);
        Logger.debug("PackageInstaller", "Available (blocks) ?(?)");
        String str2 = this.mInstalledDir;
        File file2 = new File(str2, String.valueOf(packageInfo.getName()) + ".list");
        if (Utils.unzip(absolutePath, str, file2.getAbsolutePath()) != 0) {
            if (file2.exists()) {
                file2.delete();
            }
            throw new BadArchiveException(file.getName());
        }
        String[] strArr = {"pkgdesc", "prerm", "postinst"};
        File file3 = null;
        for (int i = 0; i < 3; i++) {
            String str3 = strArr[i];
            File file4 = new File(this.mToolchainDir, str3);
            if (file4.exists()) {
                try {
                    String str4 = this.mInstalledDir;
                    File file5 = new File(str4, String.valueOf(packageInfo.getName()) + "." + str3);
                    Logger.info("PackageInstaller", "Copy file to " + file5);
                    Utils.copyDirectory(file4, file5);
                    if (str3.equals("postinst")) {
                        file3 = file5;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Logger.error("PackageInstaller", "Copy " + str3 + " file failed " + e);
                }
                file4.delete();
            }
        }
        try {
            finishInstallPackage(file3);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    private void finishInstallPackage(File file) throws IOException {
        File file2 = new File(this.mCCToolsDir, "Examples");
        if (file2.exists()) {
            try {
                Logger.info("PackageInstaller", "Move Examples to SD card");
                Utils.copyDirectory(file2, new File(Environment.getSdCardExampleDir()));
                Utils.deleteDirectory(file2);
            } catch (IOException e) {
                e.printStackTrace();
                Logger.error("PackageInstaller", "Can't copy examples directory " + e);
            }
        }
        Logger.info("PackageInstaller", "Execute postinst file " + file);
        if (file != null) {
            Utils.chmod(file.getAbsolutePath(), 493);
            Shell.exec(this.mContext, file);
            Logger.info("PackageInstaller", "Executed postinst file");
        }
    }
}
