package io.nernar.instant.launcher;

import io.nernar.instant.referrer.InstantReferrer;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import java.util.ArrayList;

public class InstantableMod extends Mod {
	public ArrayList<Executable> compiledInstantSources = new ArrayList<>();
	public boolean isInstantRunning = false;
	
	public InstantableMod(String dir) {
		super(dir);
	}
	
	@Override
	public void onImportExecutable(Executable exec) {
		exec.injectValueIntoScope("isInstant", Boolean.valueOf(InstantReferrer.inInstantDistribution()));
		super.onImportExecutable(exec);
	}
	
	@Override
	public ArrayList<Executable> getAllExecutables() {
		ArrayList<Executable> all = super.getAllExecutables();
		all.addAll(this.compiledInstantSources);
		return all;
	}
	
	public void RunInstantScripts() {
		if (this.isEnabled) {
			if (this.isInstantRunning) {
				throw new RuntimeException("mod " + this + " is already running instant scripts.");
			}
			this.isInstantRunning = true;
			for (int i = 0; i < this.compiledInstantSources.size(); i++) {
				this.compiledInstantSources.get(i).run();
			}
		}
	}
}
