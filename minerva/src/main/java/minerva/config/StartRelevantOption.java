package minerva.config;

/**
 * Start relevant AppConfig options
 */
public interface StartRelevantOption {
    // Options needed by Amalia: port, app.name, mail.smtp-server, mail.from.mail-address

    String WORKSPACES = "workspaces";
    String WORK_FOLDER = "work-folder";
    String BACKEND = "backend";
    String HOST = "host";
    String ADMINS = "admins";
    String INDEX_LIMIT = "index.limit";
    String LANGUAGES = "languages";
    String ONE_LANG = "one.lang";

    String GITLAB_URL = "gitlab.url";
    String GITLAB_PROJECT = "gitlab.project";
    String GITLAB_APPID = "gitlab.appid";
    String GITLAB_SECRET = "gitlab.secret";
    String GITLAB_AUTH_CALLBACK = "gitlab.auth-callback";
    String GITLAB_COMMIT_PATH = "gitlab.commit-path";
    String GITLAB_MERGE_REQUEST_PATH = "gitlab.merge-request-path";
}
