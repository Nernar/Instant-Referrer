package io.nernar.instant.cli;

import java.io.File;

import com.zhekasmirnov.horizon.compiler.packages.Environment;
import com.zhekasmirnov.horizon.launcher.ContextHolder;
import com.zhekasmirnov.horizon.launcher.pack.ExternalPackRepository;
import com.zhekasmirnov.horizon.launcher.pack.PackDirectory;
import com.zhekasmirnov.horizon.launcher.pack.PackHolder;
import com.zhekasmirnov.horizon.launcher.pack.PackRepository;
import com.zhekasmirnov.horizon.launcher.pack.PackStorage;

public class IndependentPackLauncher {
	private final PackStorage packStorage;
	private final PackRepository packRepository;

	public IndependentPackLauncher() {
		ContextHolder contextHolder = new ContextHolder(this);
		this.packStorage = new PackStorage(contextHolder, new File(Environment.getExternalHorizonDirectory(), "packs"));
		this.packRepository = new ExternalPackRepository("https://gitlab.com/zhekasmirnov/horizon-cloud-config/raw/master/packs.json");
	}

	public PackStorage getPackStorage() {
		return this.packStorage;
	}

	public PackHolder from(PackDirectory directory) {
		PackHolder holder = this.packStorage.loadPackHolderFromDirectory(directory);
		if (holder.getState() == PackHolder.State.INSTALLED) {
			this.packRepository.fetch();
			holder.packDirectory.fetchFromRepo(this.packRepository);
			boolean success;
			try {
				holder.refreshUpdateInfoNow();
				success = holder.selectAndLoadPack();
			} catch (Throwable e) {
				e.printStackTrace();
				success = false;
			}
			if (success) {
				return holder;
			}
			System.out.println("Failed to load previously selected pack, it is not installed or corrupted");
		} else {
			System.out.println("Selected pack in non-installed state: " + holder.getState().name());
		}
		return null;
	}
}
