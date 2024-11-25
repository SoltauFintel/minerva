package minerva.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.config.AppConfig;
import gitper.base.FileService;

public class MinervaOptions {
	public static final String MAIN_CONFIG = "main";
	
	// All labels in English language!
	
	public static final OptionCategory CAT_GENERAL = new OptionCategory("General");
	public static final Option TIMER_ACTIVE = add(CAT_GENERAL, "timer-active", "Automatic processing")
			.setHint("1: on, other value: off");
	public static final Option PDF_TAGS = add(CAT_GENERAL, "pdf-tags", "PDF tags"); 
	public static final Option MATHJAX_CONVERTER_URL = add(CAT_GENERAL, "mathjax-converter-url", "MathJax converter URL")
			.setDefaultValue("https://latex.codecogs.com/png.image?{p}").setHint("vars: {p}"); 
    public static final Option OH_HOSTS = add(CAT_GENERAL, "oh-hosts", "OH hosts", OptionType.TEXTAREA)
            .setHint("Each entry in a line: specify protocol, host and port")
            .setDefaultValue("http://192.168.40.77:4190\nhttp://192.168.40.77:4490")
            .notForCustomerVersion();
	
	private static final OptionCategory CAT_SEARCH = new OptionCategory("Search");
	public static final Option SEARCH_URL = add(CAT_SEARCH, "search.url", "Search URL")
			.setHint("protocol + host name + port");
	public static final Option SEARCH_SITE_PREFIX = add(CAT_SEARCH, "search.site-prefix", "Search site prefix")
			.setDefaultValue("minerva-")
			.setHint("usually ends with '-'");

	private static final OptionCategory CAT_WATCH = new OptionCategory("Changed page notification (watch)");
	public static final Option MAIL_WATCH_SUBJECT = add(CAT_WATCH, "mail.watch.subject", "Subject")
			.setDefaultValue("watched page modified").setHint("var: {pageTitle}");
	public static final Option MAIL_WATCH_BODY = add(CAT_WATCH, "mail.watch.body", "Mail text", OptionType.TEXTAREA)
			.setHint("vars: {pageTitle}, {url}");
	
	private static final OptionCategory CAT_COMMENT = new OptionCategory("New comment notification");
	public static final Option MAIL_COMMENT_SUBJECT = add(CAT_COMMENT, "mail.comment.subject", "Subject")
			.setDefaultValue("new comment").setHint("var: {pageTitle}");
	public static final Option MAIL_COMMENT_BODY = add(CAT_COMMENT, "mail.comment.body", "Mail text", OptionType.TEXTAREA)
			.setHint("vars: {pageTitle}, {host}, {commentPath}, {myTasksPath}");
	
	private static final OptionCategory CAT_TAG_NEW_PAGE = new OptionCategory("tag new page");
	public static final Option TNP_TAG = add(CAT_TAG_NEW_PAGE, "tag-new-page.tag", "set tag");
	public static final Option TNP_EXCEPT_USERS = add(CAT_TAG_NEW_PAGE, "tag-new-page.except-users", "except for users");
	public static final Option TNP_BOOKS = add(CAT_TAG_NEW_PAGE, "tag-new-page.books", "for book folders").setHint("empty: for all books");

	private static final OptionCategory CAT_JIRA = new OptionCategory("Jira Cloud");
	public static final Option JIRA_CUSTOMER = add(CAT_JIRA, "jira.customer", "Customer").notForCustomerVersion();
	public static final Option JIRA_MAIL = add(CAT_JIRA, "jira.mail", "Mail").notForCustomerVersion();
	public static final Option JIRA_TOKEN = add(CAT_JIRA, "jira.token", "Token", OptionType.SECRET).notForCustomerVersion();

	private static final OptionCategory CAT_CLEANUP = new OptionCategory("Cleanup service");
	public static final Option CLEANUP_CRON = add(CAT_CLEANUP, "cleanup.cron", "cron").setDefaultValue("0 0 23 ? * MON-FRI"); // werktags 23:00 Uhr
	public static final Option CLEANUP_LOGIN = add(CAT_CLEANUP, "cleanup.login", "Login")
			.setHint("Login and password are also used by other timer-controlled services.")
			.notForCustomerVersion();
	public static final Option CLEANUP_PASSWORD = add(CAT_CLEANUP, "cleanup.password", "Password", OptionType.SECRET).notForCustomerVersion();
	public static final Option CLEANUP_BRANCHES = add(CAT_CLEANUP, "cleanup.branches", "Branches").setDefaultValue("master").notForCustomerVersion();

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
			try {
                optionValues = new HashMap<>(FileService.loadJsonFile(configFile, Map.class));
            } catch (Exception e) {
                throw new RuntimeException("Error loading config file: " + configFile.getAbsolutePath(), e);
            }
		} else {
			Logger.info("Config file does not exist: " + configFile.getAbsolutePath() + " | Starting with config with default option values.");
			optionValues = new HashMap<>();
		}
		
		// Save the configuration when starting the application
		try {
			File f = new File(configFile.getParentFile(), configFile.getName() + "-backup");
			FileService.saveJsonFile(f, optionValues);
			Logger.debug("Config backup: " + f.getAbsolutePath());
		} catch (Exception e) {
			Logger.error(e);
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
		cats.add(CAT_WATCH);
		cats.add(CAT_COMMENT);
		cats.add(CAT_JIRA);
		cats.add(CAT_CLEANUP);
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
        String ret = config.get(StartRelevantOption.WORKSPACES);
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
