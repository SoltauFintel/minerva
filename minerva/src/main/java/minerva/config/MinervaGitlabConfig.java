package minerva.config;

import gitper.GitlabConfig;
import minerva.MinervaWebapp;

public class MinervaGitlabConfig implements GitlabConfig {
    private final MinervaConfig config;

    public MinervaGitlabConfig() {
        this(MinervaWebapp.factory().getConfig());
    }

    public MinervaGitlabConfig(MinervaConfig config) {
        this.config = config;
    }

    @Override
    public String getGitlabProject() {
        return config.getGitlabProject();
    }

    @Override
    public String getGitlabUrl() {
        return config.getGitlabUrl();
    }

    @Override
    public String getGitlabAppId() {
        return config.getGitlabAppId();
    }

    @Override
    public String getGitlabSecret() {
        return config.getGitlabSecret();
    }

    @Override
    public String getGitlabAuthCallback() {
        return config.getGitlabAuthCallback();
    }

    @Override
    public String getGitlabCommitPath() {
        return config.getGitlabCommitPath();
    }

    @Override
    public String getGitlabMergeRequestPath() {
        return config.getGitlabMergeRequestPath();
    }
}
