package minerva.base;

import java.io.File;

import gitper.base.FileService;
import minerva.MinervaWebapp;

/**
 * Workspace File
 */
public abstract class WFile<T> extends File {
	private final Class<T> type;

	/**
	 * @param type specify String.class if it's a plain text file
	 * @param dn -
	 */
	protected WFile(Class<T> type, String dn) {
		super(MinervaWebapp.factory().getConfig().getWorkspacesFolder(), dn);
		this.type = type;
	}

	public String loadPlainTextFile() {
		return FileService.loadPlainTextFile(this);
	}

	public void savePlainTextFile(String content) {
		FileService.savePlainTextFile(this, content);
	}

	public T loadJsonFile() {
		return FileService.loadJsonFile(this, type);
	}

	public void saveJsonFile(T data) {
		FileService.saveJsonFile(this, data);
	}
}
