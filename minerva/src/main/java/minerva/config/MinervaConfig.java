package minerva.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import github.soltaufintel.amalia.auth.webcontext.WebContext;
import github.soltaufintel.amalia.mail.Mail;
import github.soltaufintel.amalia.mail.MailSender;
import github.soltaufintel.amalia.web.action.Escaper;
import github.soltaufintel.amalia.web.config.AppConfig;
import minerva.base.StringService;
import minerva.releasenotes.ReleaseNotesConfig;

public class MinervaConfig {
    private final AppConfig config;
    private final boolean gitlab;
    
    public MinervaConfig(AppConfig config) {
        this.config = config;
        WebContext.setCookieName(this.config);
        
        // gitlab?
        String backend = env("MINERVA_BACKEND"); // for setting backend to file-system in IDE mode
        if (StringService.isNullOrEmpty(backend)) {
            backend = config.get("backend");
        }
        gitlab = "gitlab".equalsIgnoreCase(backend);
    }
    
    public boolean isDevelopment() {
        return config.isDevelopment();
    }
    
    public List<String> getLanguages() {
        List<String> languages = new ArrayList<>();
        for (String lang : config.get("languages", "de,en").split(",")) {
            String a = lang.trim().toLowerCase();
            if (languages.contains(a)) {
                throw new ConfigurationException("Duplicate key in setting 'languages'!");
            }
            languages.add(a);
        }
        if (languages.isEmpty() || languages.contains("")) {
            throw new ConfigurationException("Setting 'languages' not ok! Check config!");
        }
        return languages;
    }

    /**
     * @return false: persistence with local file system,
     * true: persistence with local file system and remote Gitlab
     */
    public boolean isGitlab() {
        return gitlab;
    }
    
    public String getWorkspacesFolder() {
        String ret = get("workspaces");
        if (ret == null || ret.trim().length() <= 3) {
            throw new ConfigurationException("Setting 'workspaces' not ok!");
        }
        ret = ret.trim().replace("\\", "/");
        if (ret.endsWith("/")) {
            ret = ret.substring(0, ret.length() - 1);
        }
        File f = new File(ret);
        f.mkdirs();
        if (!f.isDirectory()) {
            throw new ConfigurationException("Folder for setting 'workspaces' can not be created! "
                    + f.getAbsolutePath());
        }
        return ret;
    }
    
    public String getGitlabUrl() {
        String ret = get("gitlab.url");
        if (isGitlab() && StringService.isNullOrEmpty(ret)) {
            throw new ConfigurationException("Missing setting 'gitlab.url'!");
        }
        return ret;
    }
    
    public String getGitlabProject() {
        String ret = get("gitlab.project");
        if (isGitlab()) {
            if (StringService.isNullOrEmpty(ret)) {
                throw new ConfigurationException("Missing setting 'gitlab.project'!");
            } else if (!ret.contains("/")) {
                throw new ConfigurationException("Setting 'gitlab.project' must contain '/'!");
            }
        }
        return ret;
    }
    
    private String get(String key) {
        return config.get(key);
    }

    public List<String> getAdmins() {
        return splitPersons(get("admins"));
    }

    private List<String> splitPersons(String persons) {
        List<String> ret = new ArrayList<>();
        if (persons != null) {
            for (String name : persons.split(",")) {
                if (!name.isBlank()) {
                    ret.add(name.trim());
                }
            }
            Collections.sort(ret);
        }
        return ret;
    }

    public String getGitlabAppId() {
        return config.get("gitlab.appid");
    }
    
    public String getGitlabSecret() {
        return config.get("gitlab.secret");
    }
    
    public String getGitlabAuthCallback() {
        return config.get("gitlab.auth-callback");
    }

    public String getGitlabCommitPath() {
        return config.get("gitlab.commit-path", "/commit/");
    }

    public String getGitlabMergeRequestPath() {
        return config.get("gitlab.merge-request-path", "/merge_requests/");
    }

    public String getSearchUrl() {
        return config.get("search.url");
    }
    
    public String getSearchSitePrefix() {
        return config.get("search.site-prefix", "minerva-");
    }

    public String getMathJaxConverterURL(String expression) {
        // https://groups.google.com/g/mathjax-users/c/Tc4xbm61CqQ?pli=1
        // https://latex.codecogs.com/
        // https://math.vercel.app/home
        
        String url = config.get("mathjax-converter-url", "https://latex.codecogs.com/png.image?$p");
        String p = Escaper.urlEncode(expression, "0").replace("+", "%20");
        return url.replace("$p", p);
    }
    
    public String getMailHost() {
        return config.get("mail.host");
    }
    
    public String getMailLogin() {
        return config.get("mail.login");
    }
    
    public String getMailPassword() {
        return config.get("mail.password");
    }
    
    public String getMailFromAddress() {
        return config.get("mail.from-address");
    }
    
    public void sendMail(Mail mail) {
        mail.setSendername("Minerva");
        new MailSender().send(mail, config);
    }

    public String getCommentSubject() {
        return config.get("mail.comment.subject", "new comment");
    }
    
    /**
     * @return never null
     */
    public String getCommentBody() {
        return config.get("mail.comment.body", "").replace("{host}", config.get("host")).replace("\\n", "\n");
    }
    
    public boolean readyForCommentNotifications() {
        return !config.get(MailSender.SMTP_SERVER, "").isEmpty()
                && !getCommentSubject().isEmpty()
                && !getCommentBody().isEmpty();
    }
    
    public String getWatchSubject() {
        return config.get("mail.watch.subject", "watched page modified");
    }
    
    public String getWatchBody() {
        return config.get("mail.watch.body", "").replace("{host}", config.get("host")).replace("\\n", "\n");
    }

    public boolean readyForWatchNotifications() {
        return !config.get(MailSender.SMTP_SERVER, "").isEmpty()
                && !getWatchSubject().isEmpty()
                && !getWatchBody().isEmpty();
    }
    
    public String getWorkFolder() {
        return config.get("work-folder", "");
    }
    
    /**
     * @return book 6 editor password, not null
     */
    public String getEditorPassword() {
        return env("MINERVA_EDITORPASSWORD");
    }
    
    public String getUserFolder() {
        return env("MINERVA_USERFOLDER");
    }
    
    public String getSubscribers() {
        return env("MINERVA_SUBSCRIBERS");
    }
    
    public boolean hasSubscribers() {
        return !getSubscribers().isEmpty();
    }
    
    public String getKunde() {
        return env("MINERVA_KUNDE");
    }
    
    public String getMigration() {
        return env("MINERVA_MIGRATION");
    }
    
    public String getMigrationUsers() {
        return env("MINERVA_MIGRATIONUSERS");
    }
    
    public String getMigrationSourceFolder() {
        return env("MINERVA_MIGRATIONSOURCEFOLDER");
    }
    
    public String getMigrationHelpKeysFolder() {
        return env("MINERVA_MIGRATIONHELPKEYSFOLDER");
    }
    
    private String env(String name) {
        String ret = System.getenv(name);
        return ret == null ? "" : ret;
    }
    
    public String getReleaseNotesBaseUrl() {
        return config.get("release-notes.base-url");
    }
    
    public String getReleaseNotesToken() {
        return config.get("release-notes.token");
    }
    
    public String[] getReleaseNotesBookTitles() {
        String c = config.get("release-notes.book-titles");
        if (StringService.isNullOrEmpty(c)) {
            return new String[0];
        } else {
            return c.split(",");
        }
    }
    
    public List<ReleaseNotesConfig> loadReleaseNotesConfigs() {
        List<ReleaseNotesConfig> ret = new ArrayList<>();
        int i = 0;
        while (true) {
            String c = config.get("release-notes.config" + ++i);
            if (c == null) {
                break;
            }
            String[] w = c.split(",");
            if (w.length < 5 || w[4].isEmpty()) {
                throw new RuntimeException("release-notes.config" + (i - 1) + " entry is not ok! Format is: root title, de|en, ticket prefix, space key, customer");
            }
            ReleaseNotesConfig e = new ReleaseNotesConfig();
            e.setRootTitle(w[0]);
            e.setLanguage(w[1]);
            e.setTicketPrefix(w[2]);
            e.setSpaceKey(w[3]);
            e.setCustomer(w[4]);
            ret.add(e);
        }
        ret.sort((a, b) -> a.getCustomer().compareToIgnoreCase(b.getCustomer()));
        return ret;
    }
    
    public String[] getPDF_tags() {
        return config.get("pdf-tags", "").split(","); // nicht_drucken
    }
    
    public String getOldHelpKeysReaderJiraUrl() {
        return config.get("OldHelpKeysReader.jira-url");
    }
    
    public String getJiraCustomer() {
        return config.get("jira.customer");
    }
    
    public String getJiraMail() {
        return config.get("jira.mail"); // user login
    }
    
    public String getJiraToken() {
        return config.get("jira.token");
    }
}
