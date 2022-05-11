package io.nernar.instant.storage;

public class TranslationResource extends AbstractResource {
	public String id = "none";
	
	public TranslationResource(String id) {
		if (id != null) this.id = id;
	}
	
	public TranslationResource(String id, String english) {
		this(id);
		put("en", english);
	}
	
	public TranslationResource(String id, String english, String russian) {
		this(id, english);
		put("ru", russian);
	}
	
	public TranslationResource(String id, String... keysAndSource) {
		this(id);
		if (keysAndSource.length % 2 == 1) {
			throw new IllegalArgumentException("Translation must contain [key, value...]");
		}
		for (int i = 0; i < keysAndSource.length / 2; i += 2) {
			put(keysAndSource[i * 2], keysAndSource[i * 2 + 1]);
		}
	}
	
	@Override
	public String getId() {
		return id;
	}
}
