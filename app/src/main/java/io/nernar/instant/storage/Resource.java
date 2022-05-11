package io.nernar.instant.storage;

import java.io.File;

public interface Resource {
	public String getId();
	public String getKey();
	public File getOutput();
}
