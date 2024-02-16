package minerva.config;

import minerva.persistence.filesystem.FileSystemBackendService;
import minerva.persistence.gitlab.GitlabBackendService;

public class BackendServiceFactory {

    public BackendService getBackendService(boolean gitlab, MinervaConfig config) {
        return gitlab ? new GitlabBackendService(config) : new FileSystemBackendService(config);
    }
}
