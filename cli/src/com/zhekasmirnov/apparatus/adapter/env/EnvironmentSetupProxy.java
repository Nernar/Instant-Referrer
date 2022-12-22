package com.zhekasmirnov.apparatus.adapter.env;

import com.zhekasmirnov.apparatus.modloader.ApparatusMod;
import java.io.File;

public interface EnvironmentSetupProxy {
    void addBehaviorPackDirectory(ApparatusMod apparatusMod, File file);

    void addGuiAssetsDirectory(ApparatusMod apparatusMod, File file);

    void addJavaDirectory(ApparatusMod apparatusMod, File file);

    void addNativeDirectory(ApparatusMod apparatusMod, File file);

    void addResourceDirectory(ApparatusMod apparatusMod, File file);

    void addResourcePackDirectory(ApparatusMod apparatusMod, File file);
}
