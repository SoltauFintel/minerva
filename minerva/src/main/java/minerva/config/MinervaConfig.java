package minerva.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pmw.tinylog.Logger;

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
            backend = config.get(StartRelevantOption.BACKEND);
        }
        gitlab = "gitlab".equalsIgnoreCase(backend);
    }
    
    public boolean isDevelopment() {
        return config.isDevelopment();
    }
    
    public List<String> getLanguages() {
        List<String> languages = new ArrayList<>();
        for (String lang : config.get(StartRelevantOption.LANGUAGES, "de,en").split(",")) {
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
    	return MinervaOptions.getWorkspacesFolder(config);
    }
    
    public String getGitlabUrl() {
        String ret = get(StartRelevantOption.GITLAB_URL);
        if (isGitlab() && StringService.isNullOrEmpty(ret)) {
            throw new ConfigurationException("Missing setting 'gitlab.url'!");
        }
        return ret;
    }
    
    public String getGitlabProject() {
        String ret = get(StartRelevantOption.GITLAB_PROJECT);
        if (isGitlab()) {
			if (StringService.isNullOrEmpty(ret)) {
				throw new ConfigurationException("Missing setting '" + StartRelevantOption.GITLAB_PROJECT + "'!");
			} else if (!ret.contains("/")) {
				throw new ConfigurationException("Setting '" + StartRelevantOption.GITLAB_PROJECT + "' must contain '/'!");
            }
        }
        return ret;
    }
    
    private String get(String key) {
        return config.get(key);
    }

    public List<String> getAdmins() {
        return splitPersons(get(StartRelevantOption.ADMINS));
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
        return config.get(StartRelevantOption.GITLAB_APPID);
    }
    
    public String getGitlabSecret() {
        return config.get(StartRelevantOption.GITLAB_SECRET);
    }
    
    public String getGitlabAuthCallback() {
        return config.get(StartRelevantOption.GITLAB_AUTH_CALLBACK);
    }

    public String getGitlabCommitPath() {
        return config.get(StartRelevantOption.GITLAB_COMMIT_PATH, "/commit/");
    }

    public String getGitlabMergeRequestPath() {
        return config.get(StartRelevantOption.GITLAB_MERGE_REQUEST_PATH, "/merge_requests/");
    }

    public String getMathJaxConverterURL(String expression) {
        // https://groups.google.com/g/mathjax-users/c/Tc4xbm61CqQ?pli=1
        // https://latex.codecogs.com/
        // https://math.vercel.app/home
        
        String url = MinervaOptions.MATHJAX_CONVERTER_URL.get();
        String p = Escaper.urlEncode(expression, "0").replace("+", "%20");
        return url.replace("{p}", p);
    }
    
    public void sendMail(Mail mail) {
        if (StringService.isNullOrEmpty(config.get(MailSender.SMTP_SERVER))) {
        	Logger.info("\"" + MailSender.SMTP_SERVER + "\" is not set -> don't send mail \"" + mail.getSubject() + "\" to " + mail.getToEmailaddress());
        } else {
        	mail.setSendername("Minerva");
        	new MailSender().send(mail, config);
        }
    }

    /**
     * @return never null
     */
    public String getCommentBody() {
    	return replaceHost(MinervaOptions.MAIL_COMMENT_BODY.get());
    }
    
    public boolean readyForCommentNotifications() {
        return hasMailServer()
                && MinervaOptions.MAIL_COMMENT_SUBJECT.isSet()
                && MinervaOptions.MAIL_COMMENT_BODY.isSet();
    }
    
    public String getWatchBody() {
        return replaceHost(MinervaOptions.MAIL_WATCH_BODY.get());
    }
    
    private String replaceHost(String body) {
		return body == null ? "" : body.replace("{host}", getHost());
    }

    public boolean readyForWatchNotifications() {
        return hasMailServer()
            && MinervaOptions.MAIL_WATCH_SUBJECT.isSet()
            && MinervaOptions.MAIL_WATCH_BODY.isSet();
    }
    
    private boolean hasMailServer() {
    	return !StringService.isNullOrEmpty(config.get(MailSender.SMTP_SERVER));
    }
    
    public String getWorkFolder() {
        return config.get(StartRelevantOption.WORK_FOLDER, "");
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

    @Deprecated
    public String getReleaseNotesBaseUrl() {
        return config.get("release-notes.base-url");
    }
    
    @Deprecated
    public String getReleaseNotesToken() {
        return config.get("release-notes.token");
    }
    
    public String[] getReleaseNotesBookTitles() {
        String c = MinervaOptions.RELEASE_NOTES_BOOK_TITLES.get();
        if (StringService.isNullOrEmpty(c)) {
            return new String[0];
        } else {
            return c.split(",");
        }
    }
    
    public List<ReleaseNotesConfig> loadReleaseNotesConfigs() {
    	List<ReleaseNotesConfig> ret = new ArrayList<>();
    	String lines = MinervaOptions.RELEASE_NOTES_CUSTOMERS.get();
    	for (String line : lines.split("\n")) {
    		String[] w = line.split(",");
            if (w.length < 5 || w[4].isEmpty()) {
                throw new RuntimeException("Release Notes customer line \"" + line + "\" is not valid! Please fix configuration.");
            }
            ReleaseNotesConfig e = new ReleaseNotesConfig();
            e.setRootTitle(w[0].trim());
            e.setLanguage(w[1].trim());
            e.setTicketPrefix(w[2].trim());
            e.setSpaceKey(w[3].trim());
            e.setCustomer(w[4].trim());
            ret.add(e);
    	}
    	ret.sort((a, b) -> a.getCustomer().compareToIgnoreCase(b.getCustomer()));
    	return ret;
    }
    
    public String[] getPDF_tags() {
    	String pdfTags = MinervaOptions.PDF_TAGS.get();
    	if (pdfTags == null) {
    	    return new String[0];
    	}
        return pdfTags.split(","); // nicht_drucken
    }
    
    /**
     * @return tag text. New pages will get this tag. Can be null or empty.
     */
    public String getTagNewPage_tag() {
    	return MinervaOptions.TNP_TAG.get();
    }
    
    /**
     * see getTagNewPage_tag
     * @return comma separated user list. If an user of this list creates a new page no tag will be created.
     */
    public String getTagNewPage_exceptUsers() {
    	return MinervaOptions.TNP_EXCEPT_USERS.get();
    }

    /**
     * see getTagNewPage_tag
     * @return comma separated book folders. Tags will only be created for given books. If list is null or empty tags will be created for all books.
     */
    public String getTagNewPage_books() {
    	return MinervaOptions.TNP_BOOKS.get();
    }
    
    public List<String> getOneLang() {
        List<String> langs = new ArrayList<>();
        langs.add(config.get(StartRelevantOption.ONE_LANG, "de"));
        return langs;
    }
    
    /**
     * @return if there are more subpages than this the will be hidden and instead a table will be displayed
     */
    public int getMaxSubfeatures() {
        return 20;
    }
    
    public int getIndexLimit() {
        return config.getInt(StartRelevantOption.INDEX_LIMIT, 60);
    }

	public String getHost() {
		return config.get(StartRelevantOption.HOST);
	}
}
