package minerva.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.config.AppConfig;
import minerva.base.FileService;

public class MinervaOptions {
	public static final String MAIN_CONFIG = "main";
	
	// All labels in English language!
	
	public static final OptionCategory CAT_GENERAL = new OptionCategory("General");
	public static final Option PDF_TAGS = add(CAT_GENERAL, "pdf-tags", "PDF tags"); 
	
	private static final OptionCategory CAT_SEARCH = new OptionCategory("Search");
	public static final Option SEARCH_URL = add(CAT_SEARCH, "search.url", "Search URL")
			.setHint("protocol + host name + port");
	public static final Option SEARCH_SITE_PREFIX = add(CAT_SEARCH, "search.site-prefix", "Search site prefix")
			.setDefaultValue("minerva-")
			.setHint("usually ends with '-'");

	private static final OptionCategory CAT_MAIL = new OptionCategory("Mail server");
	public static final Option MAIL_SMTP_SERVER = add(CAT_MAIL, "mail.smtp-server", "SMTP server")
			.setHint("protocol + host name + port");
	public static final Option MAIL_FROM_MAIL_ADDRESS = add(CAT_MAIL, "mail.from.mail-address", "Sender mail address");

	private static final OptionCategory CAT_WATCH = new OptionCategory("Changed page notification (watch)");
	public static final Option MAIL_WATCH_SUBJECT = add(CAT_WATCH, "mail.watch.subject", "Subject");
	public static final Option MAIL_WATCH_BODY = add(CAT_WATCH, "mail.watch.body", "Mail text", OptionType.TEXTAREA)
			.setHint("vars: {pageTitle}, {host}, {branch}, {bookFolder}, {pageId}"); // TODO fertige URLs wären besser
	
	private static final OptionCategory CAT_COMMENT = new OptionCategory("New comment notification");
	public static final Option MAIL_COMMENT_SUBJECT = add(CAT_COMMENT, "mail.comment.subject", "Subject");
	public static final Option MAIL_COMMENT_BODY = add(CAT_COMMENT, "mail.comment.body", "Mail text", OptionType.TEXTAREA)
			.setHint("vars: {pageTitle}, {host}, {commentPath}, {myTasksPath}");
	
	private static final OptionCategory CAT_TAG_NEW_PAGE = new OptionCategory("tag new page");
	public static final Option TNP_TAG = add(CAT_TAG_NEW_PAGE, "tag-new-page.tag", "set tag");
	public static final Option TNP_EXCEPT_USERS = add(CAT_TAG_NEW_PAGE, "tag-new-page.except-users", "except for users");
	public static final Option TNP_BOOKS = add(CAT_TAG_NEW_PAGE, "tag-new-page.tag", "for book folders").setHint("empty: for all books");

	/** global instance, same for all users, must be set at program start */
	public static MinervaOptions options;

	private final File configFile;
	final Map<String, String> optionValues;
	
	@SuppressWarnings("unchecked")
	public MinervaOptions(File configFile) {
		if (configFile == null) {
			throw new IllegalArgumentException("configFile must not be null");
		}
		this.configFile = configFile;
		if (configFile.isFile()) {
			optionValues = new HashMap<>(FileService.loadJsonFile(configFile, Map.class));
		} else {
			Logger.warn("Config file does not exist: " + configFile.getAbsolutePath());
			optionValues = new HashMap<>();
		}
	}
	
	public void save() {
		FileService.saveJsonFile(configFile, optionValues);
		Logger.info("Config file saved: " + configFile.getAbsolutePath());
	}
	
	public List<OptionCategory> getCategories() {
		List<OptionCategory> cats = new ArrayList<>();
		cats.add(CAT_GENERAL);
		cats.add(CAT_TAG_NEW_PAGE);
		cats.add(CAT_SEARCH);
		cats.add(CAT_MAIL);
		cats.add(CAT_WATCH);
		cats.add(CAT_COMMENT);
		return cats;
	}
	
	protected static Option add(OptionCategory cat, String key, String label) {
		return add(cat, key, label, OptionType.TEXT);
	}
	
	protected static Option add(OptionCategory cat, String key, String label, OptionType type) {
		return cat.add(new Option(key, label, type));
	}
    
    public static File getConfigFile(String name, AppConfig config) {
    	return new File(getWorkspacesFolder(config), name + "-config.json");
    }
	
    public static String getWorkspacesFolder(AppConfig config) {
        String ret = config.get("workspaces");
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
}
