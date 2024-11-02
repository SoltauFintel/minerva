package minerva.config;

import gitper.BackendService;
import gitper.persistence.gitlab.GitlabBackendService;
import minerva.persistence.filesystem.FileSystemBackendService;

public class BackendServiceFactory {

    public BackendService getBackendService(boolean gitlab, MinervaConfig config) {
        return gitlab ? new GitlabBackendService(new MinervaGitlabConfig()) : new FileSystemBackendService(config);
    }
}
