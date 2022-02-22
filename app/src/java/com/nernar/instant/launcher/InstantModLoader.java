package com.nernar.instant.launcher;

import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class InstantModLoader extends ModLoader {
	
	public static void initialize() {
		instance = new InstantModLoader();
	}
	
	public void startInstantMods() {
		for (int i = 0; i < this.modsList.size(); i++) {
            LoadingUI.setTextAndProgressBar("Instant Mods: " + (i + 1) + "/" + this.modsList.size() + " ", ((i * 0.3f) / this.modsList.size()) + 0.7f);
			if (this.modsList.get(i) instanceof InstantableMod) ((InstantableMod) this.modsList.get(i)).RunInstantScripts();
        }
	}
	
	public static void runInstantModsViaNewModLoader() {
		try {
			Class<?> ApparatusModLoader = Class.forName("com.zhekasmirnov.apparatus.modloader.ApparatusModLoader");
			Object singleton = ApparatusModLoader.getMethod("getSingleton").invoke(null);
			List apparatusModList = (List) singleton.getClass().getMethod("getAllMods").invoke(singleton);
			Class<?> LegacyInnerCoreMod = Class.forName("com.zhekasmirnov.apparatus.modloader.LegacyInnerCoreMod");
			int progress = 1;
			int total = apparatusModList.size();
			for (Object apparatusMod : apparatusModList) {
				if (apparatusMod != null && apparatusMod.getClass().isAssignableFrom(LegacyInnerCoreMod)) {
					LoadingUI.setTextAndProgressBar("Instant Mods  " + progress + "/" + total + "...", ((progress / total) * 0.3f) + 0.5f);
					try {
						Object apparatusModInfo = apparatusMod.getClass().getMethod("getInfo").invoke(apparatusMod);
						LoadingUI.setTip((String) apparatusModInfo.getClass().getMethod("getString", String.class, String.class).invoke(apparatusModInfo, "displayed_name", ""));
					} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
						LoadingUI.setTip(e.getLocalizedMessage());
					}
					Mod mod = (Mod) apparatusMod.getClass().getMethod("getLegacyModInstance").invoke(apparatusMod);
					if (mod instanceof InstantableMod) ((InstantableMod) mod).RunInstantScripts();
				}
				progress++;
			}
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			throw new UnsupportedOperationException(e);
		} catch (InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
