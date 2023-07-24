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

public class MinervaConfig {
    private static final String SMTP = "mail.smtp-server"; // TODO Amalia constant
    private final AppConfig config;

    public MinervaConfig(AppConfig config) {
        this.config = config;
        WebContext.setCookieName(this.config);
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
        String backend = env("BACKEND"); // for setting backend to file-system in IDE mode
        if (StringService.isNullOrEmpty(backend)) {
            backend = config.get("backend");
        }
        return "gitlab".equalsIgnoreCase(backend);
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

    public List<String> getPersons() {
        return splitPersons(get("persons"));
    }

    public List<String> getPersonsWithExportRight() {
        return splitPersons(get("persons-with-export-right"));
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

    public String getMailAddress(String login) {
        return config.get("mail.address." + login);
    }

    public String getNoteSubject() {
        return config.get("mail.note.subject", "new comment");
    }
    
    /**
     * @return never null
     */
    public String getNoteBody() {
        return config.get("mail.note.body", "").replace("\\n", "\n");
    }
    
    public boolean readyForNoteNotifications() {
        return !config.get(SMTP, "").isEmpty()
                && !getNoteSubject().isEmpty()
                && !getNoteBody().isEmpty();
    }
    
    public String getWatchSubject() {
        return config.get("mail.watch.subject", "watched page modified");
    }
    
    public String getWatchBody() {
        return config.get("mail.watch.body", "").replace("\\n", "\n");
    }

    public boolean readyForWatchNotifications() {
        return !config.get(SMTP, "").isEmpty()
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
        return env("EDITORPASSWORD");
    }
    
    public String getUserFolder() {
        return env("USERFOLDER");
    }
    
    public String getSubscribers() {
        return env("SUBSCRIBERS");
    }
    
    public String getKunde() {
        return env("KUNDE");
    }
    
    public String getMigration() {
        return env("MIGRATION");
    }
    
    public String getMigrationUsers() {
        return env("MIGRATIONUSERS");
    }
    
    public String getMigrationSourceFolder() {
        return env("MIGRATIONSOURCEFOLDER");
    }
    
    public String getMigrationHelpKeysFolder() {
        return env("MIGRATIONHELPKEYSFOLDER");
    }
    
    private String env(String name) {
        String ret = System.getenv("MINERVA_" + name);
        return ret == null ? "" : ret;
    }
}
