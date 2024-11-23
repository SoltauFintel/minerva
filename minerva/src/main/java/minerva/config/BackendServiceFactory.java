package minerva.config;

import gitper.BackendService;
import gitper.access.MultiPurposeDirAccess;
import gitper.persistence.filesystem.FileSystemBackendService;
import gitper.persistence.filesystem.FileSystemBackendService.MinervaFileSystemConfig;
import gitper.persistence.gitlab.GitlabBackendService;
import minerva.MinervaWebapp;
import minerva.base.NLS;
import minerva.seite.Seite;

public class BackendServiceFactory {

    public BackendService getBackendService(boolean gitlab, MinervaConfig config) {
    	if (gitlab) {
    		return new GitlabBackendService(new MinervaGitlabConfig(config));
    	} else {
    		return new FileSystemBackendService(getMinervaFileSystemConfig(config)) {
				@Override
				public Seite forceReloadIfCheap(String filenameMeta) {
		        	return new MultiPurposeDirAccess(getDirAccess()).load(filenameMeta, Seite.class);
				}
    		};
    	}
    }

	private MinervaFileSystemConfig getMinervaFileSystemConfig(MinervaConfig config) {
		return new MinervaFileSystemConfig() {

			@Override
			public String getWorkspacesFolder() {
				return MinervaWebapp.factory().getConfig().getWorkspacesFolder();
			}

			@Override
			public String getEditorPassword() {
				return MinervaWebapp.factory().getConfig().getEditorPassword();
			}

			@Override
			public String getUserFolder() {
				return config.getUserFolder();
			}

			@Override
			public String n(String lang, String key) {
				return NLS.get(lang, key);
			}
		};
	}
}
