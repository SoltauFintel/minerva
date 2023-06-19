package minerva.config;

import java.io.File;
import java.util.List;

import minerva.access.DirAccess;
import minerva.auth.LoginService;
import minerva.base.NlsString;
import minerva.base.StringService;
import minerva.model.GitlabRepositorySO;
import minerva.model.GitlabSystemSO;
import minerva.model.UserSO;
import minerva.persistence.filesystem.FileSystemDirAccess;
import minerva.persistence.filesystem.FileSystemLoginService;
import minerva.persistence.gitlab.GitlabDirAccess;
import minerva.persistence.gitlab.GitlabLoginService;

/**
 * Wenn zwischen FileSystem und Gitlab Persistenz zu unterscheiden ist, liefert
 * diese Factory diejeweilige richtige Klasse zurück.
 */
public class MinervaFactory {
    private final MinervaConfig config;
    private final boolean gitlab;
    private final GitlabSystemSO gitlabSystem;
    private final GitlabRepositorySO gitlabRepository;
    private final List<String> languages;
    private final List<String> persons;
    
    public MinervaFactory(MinervaConfig config) {
        this.config = config;
        languages = config.getLanguages();
        gitlab = config.isGitlab();
        if (gitlab) {
            gitlabSystem = new GitlabSystemSO(config.getGitlabUrl());
            gitlabRepository = new GitlabRepositorySO(gitlabSystem, config.getGitlabProject());
        } else {
            gitlabSystem = null;
            gitlabRepository = null;
        }
        String folder = System.getenv("MINERVA_USERFOLDER");
        if (!StringService.isNullOrEmpty(folder)) {
            folder = " | static user folder: " + folder;
        } else {
            folder = "";
        }
        System.out.println("languages: " + languages + " | backend: " + getPersistenceInfo() + folder);
        persons = config.getPersons();
    }

    public MinervaConfig getConfig() {
        return config;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public LoginService getLoginService() {
        return gitlab ? new GitlabLoginService() : new FileSystemLoginService();
    }

    public boolean isGitlab() {
        return gitlab;
    }
    
    public boolean isCustomerVersion() {
        return "1".equals(System.getenv("MINERVA_KUNDE"));
    }

    public GitlabSystemSO getGitlabSystem() {
        return gitlabSystem;
    }

    public GitlabRepositorySO getGitlabRepository() {
        return gitlabRepository;
    }

    public DirAccess getDirAccess(UserSO user) {
        return gitlab ? new GitlabDirAccess() : new FileSystemDirAccess();
    }

    public String getPersistenceInfo() {
        return gitlab ? "Gitlab (" + gitlabRepository.getProjectUrl() + ")"
                : "Dateisystem (" + new File(getConfig().getWorkspacesFolder()).getAbsolutePath() + ")";
    }
    
    public void setNewSeiteTitle(NlsString title, String something) {
        for (String lang : languages) {
            if ("de".equals(lang)) {
                title.setString("de", "Neue Seite " + something);
            } else {
                title.setString(lang, "New page " + something);
            }
        }
    }
    
    public List<String> getPersons() {
        return persons;
    }
}
