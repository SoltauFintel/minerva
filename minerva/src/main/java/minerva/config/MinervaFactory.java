package minerva.config;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import minerva.access.DirAccess;
import minerva.auth.LoginService;
import minerva.base.FileService;
import minerva.base.NlsString;
import minerva.git.CommitMessage;
import minerva.model.GitlabRepositorySO;
import minerva.model.GitlabSystemSO;
import minerva.model.SeiteSO;
import minerva.model.UserSO;
import minerva.persistence.filesystem.FileSystemDirAccess;
import minerva.persistence.filesystem.FileSystemLoginService;
import minerva.persistence.gitlab.GitlabDirAccess;
import minerva.persistence.gitlab.GitlabLoginService;
import minerva.seite.IPageChangeStrategy;
import minerva.seite.PageChange;

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
    private final List<String> personsWithExportRight;
    private final List<String> admins;
    private final IPageChangeStrategy pageChangeStrategy;
    
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
        final String folder = config.getUserFolder();
        String folderInfo = "";
        if (!folder.isEmpty()) {
            folderInfo = " | static user folder: " + folder;
        }
        System.out.println("languages: " + languages + " | backend: " + getPersistenceInfo() + folderInfo);
        persons = config.getPersons();
        admins = config.getAdmins();
        personsWithExportRight = config.getPersonsWithExportRight();
        personsWithExportRight.addAll(admins);
        
        // denkbar wären: A. alle speichern
        //   B. nur 1 speichern
        //   C. alle speichern, aber nur letzten anzeigen
        //   D. Git Historie
        pageChangeStrategy = new IPageChangeStrategy() { // strategy C (merge friendlier)
            @Override
            public void set(String comment, SeiteSO seite) {
                PageChange change = new PageChange();
                change.setComment(comment);
                change.setUser(seite.getLogin());
                change.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                List<PageChange> changes = seite.getSeite().getChanges();
                changes.add(change);
            }
            
            @Override
            public CommitMessage getCommitMessage(String comment, SeiteSO seite) {
                return new CommitMessage(seite, comment);
            }
            
            @Override
            public List<PageChange> getChanges(SeiteSO seite) {
                return seite.getSeite().getChanges();
            }
        };
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
        return "1".equals(config.getKunde());
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

    public List<String> getPersonsWithExportRight() {
        return personsWithExportRight;
    }

    public List<String> getAdmins() {
        return admins;
    }

    public IPageChangeStrategy getPageChangeStrategy() {
        return pageChangeStrategy;
    }
    
    public File getWorkFolder(String name) {
        String workFolder = config.getWorkFolder();
        if (workFolder.isEmpty()) {
            return new File(name);
        } else {
            return new File(workFolder, name);
        }
    }
    
    public List<String> getLogins() {
        return FileService.listFolders(new File(config.getWorkspacesFolder()));
    }
}
