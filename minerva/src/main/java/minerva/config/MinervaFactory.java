package minerva.config;

import java.io.File;
import java.util.List;

import minerva.access.CommitMessage;
import minerva.auth.MinervaAuth;
import minerva.base.FileService;
import minerva.base.NlsString;
import minerva.base.StringService;
import minerva.model.SeiteSO;
import minerva.seite.IPageChangeStrategy;
import minerva.seite.PageChange;

/**
 * Wenn zwischen FileSystem und Gitlab Persistenz zu unterscheiden ist, liefert
 * diese Factory diejeweilige richtige Klasse zurück.
 */
public class MinervaFactory {
    public static BackendServiceFactory backendServiceFactory = new BackendServiceFactory();
    private final MinervaConfig config;
    private final boolean gitlab;
    private final List<String> languages;
    private final List<String> admins;
    private final IPageChangeStrategy pageChangeStrategy;
    private final BackendService backendService;
    
    public MinervaFactory(MinervaConfig config) {
        this.config = config;
        languages = config.getLanguages();
        gitlab = config.isGitlab();
        admins = config.getAdmins();
        
        backendService = backendServiceFactory.getBackendService(gitlab, config);
        
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
                change.setDate(StringService.now());
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
    
    public String getInitialLanguage() {
        return (MinervaAuth.browserLanguage != null && languages.contains(MinervaAuth.browserLanguage))
                ? MinervaAuth.browserLanguage
                : languages.get(0);
    }

    public BackendService getBackendService() {
        return backendService;
    }

    public boolean isGitlab() {
        return gitlab;
    }
    
    public void gitlabOnlyPage() {
        if (!gitlab) {
            throw new RuntimeException("Page only for Gitlab mode");
        }
    }
    
    public boolean isCustomerVersion() {
        return "1".equals(config.getKunde());
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
    
    public String getFolderInfo() {
        final String folder = config.getUserFolder();
        String folderInfo = "";
        if (!folder.isEmpty()) {
            folderInfo = " | static user folder: " + folder;
        }
        return folderInfo;
    }
}
