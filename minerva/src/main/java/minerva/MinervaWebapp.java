package minerva;

import org.pmw.tinylog.Level;

import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.timer.Timer;
import github.soltaufintel.amalia.web.WebApp;
import github.soltaufintel.amalia.web.builder.LoggingInitializer;
import github.soltaufintel.amalia.web.builder.WebAppBuilder;
import github.soltaufintel.amalia.web.config.AppConfig;
import github.soltaufintel.amalia.web.route.PingRouteDefinition.PingAction;
import github.soltaufintel.amalia.web.route.RouteDefinitions;
import github.soltaufintel.amalia.web.table.TableSortAction;
import gitper.Gitper;
import gitper.GitperInterface;
import gitper.User;
import gitper.persistence.gitlab.GitFactory;
import gitper.persistence.gitlab.GitlabAuthAction;
import gitper.persistence.gitlab.GitlabAuthCallbackAction;
import gitper.persistence.gitlab.GitlabAuthService;
import minerva.attachments.AttachmentsPage;
import minerva.attachments.DeleteAttachmentAction;
import minerva.attachments.DownloadAttachmentAction;
import minerva.attachments.EditAttachmentPage;
import minerva.attachments.SaveUserAttachmentCategoryAction;
import minerva.attachments.UploadAttachmentAction;
import minerva.auth.ActivateAdminRightsAction;
import minerva.auth.Book6LoginAction;
import minerva.auth.Book6StartAction;
import minerva.auth.LoggedOutPage;
import minerva.auth.Login2Page;
import minerva.auth.MinervaAuth;
import minerva.base.CustomErrorPage;
import minerva.base.MathPage;
import minerva.base.MessagePage;
import minerva.base.MinervaError404Page;
import minerva.base.MinervaErrorPage;
import minerva.base.MinervaPageInitializer;
import minerva.base.ServerlogPage;
import minerva.base.Tosmap;
import minerva.base.TosmapInfoPage;
import minerva.base.UpdatePagesMetricsAction;
import minerva.book.AddBookPage;
import minerva.book.BookPage;
import minerva.book.BooksPage;
import minerva.book.DeleteBookPage;
import minerva.book.EditBookPage;
import minerva.book.MenuPage;
import minerva.book.OrderTopLevelSeitePage;
import minerva.book.SelectLanguageAction;
import minerva.book.SortTopLevelSeiteAction;
import minerva.book.ToggleShowAllPagesAction;
import minerva.comment.CommentDoneAction;
import minerva.comment.CommentService;
import minerva.comment.CommentsPage;
import minerva.comment.DeleteCommentAction;
import minerva.comment.EditCommentPage;
import minerva.comment.SeiteCommentService;
import minerva.config.EditConfigPage;
import minerva.config.InfoAction;
import minerva.config.MinervaConfig;
import minerva.config.MinervaFactory;
import minerva.config.MinervaGitlabConfig;
import minerva.config.MinervaOptions;
import minerva.exclusions.CustomerModePage;
import minerva.exclusions.ExclusionsEditPage;
import minerva.exclusions.SelectCustomerModeAction;
import minerva.export.DownloadExportPage;
import minerva.export.ExportBookAction;
import minerva.export.ExportPage;
import minerva.export.ExportWorkspaceAction;
import minerva.export.GenericExportService.CleanupExportFolderTimer;
import minerva.export.SeitenauswahlPage;
import minerva.export.template.AddExportTemplateSetAction;
import minerva.export.template.DeleteExportTemplateSetAction;
import minerva.export.template.EditExportTemplateSetPage;
import minerva.export.template.ExportTemplateSetsPage;
import minerva.image.ImageDownloadAction;
import minerva.image.ImageUploadAction;
import minerva.keyvalue.AddValuesPage;
import minerva.keyvalue.DeleteValuesAction;
import minerva.keyvalue.EditValuesPage;
import minerva.keyvalue.ValuesListPage;
import minerva.migration.MigrationPage;
import minerva.model.JournalSO;
import minerva.model.JournalSO.HourlyJournalTimer;
import minerva.model.JournalSO.JournalTimer;
import minerva.papierkorb.DeleteWeggeworfeneSeiteAction;
import minerva.papierkorb.PapierkorbPage;
import minerva.papierkorb.PapierkorbUnterseitenPage;
import minerva.papierkorb.RecycleAction;
import minerva.postcontents.PostContentsAction;
import minerva.publish.PublishAction;
import minerva.search.IndexBooksAction;
import minerva.search.IndexBooksTimer;
import minerva.search.SearchPage;
import minerva.seite.AddSeiteAction;
import minerva.seite.CancelEditingAction;
import minerva.seite.EditHtmlPage;
import minerva.seite.EditSeitePage;
import minerva.seite.LiveSaveSeiteAction;
import minerva.seite.ViewSeitePage;
import minerva.seite.actions.CopyToWorkspacePage;
import minerva.seite.actions.DeleteSeitePage;
import minerva.seite.actions.DontChangePage;
import minerva.seite.actions.DuplicateSeitePage;
import minerva.seite.actions.LockedByPage;
import minerva.seite.actions.MoveSeiteAckPage;
import minerva.seite.actions.MoveSeiteAction;
import minerva.seite.actions.MoveSeitePage;
import minerva.seite.actions.OrderSeitePage;
import minerva.seite.actions.PagesWatchedByMePage;
import minerva.seite.actions.PullSeiteAction;
import minerva.seite.actions.SaveEditorsNoteAction;
import minerva.seite.actions.SeiteHistoryPage;
import minerva.seite.actions.SortSeiteAction;
import minerva.seite.actions.TocAction;
import minerva.seite.actions.ToggleFavoriteAction;
import minerva.seite.actions.ToggleWatchAction;
import minerva.seite.helpkeys.AllHelpKeysPage;
import minerva.seite.helpkeys.CleanupHelpKeysForHeadingsAction;
import minerva.seite.helpkeys.HelpKeysForHeadingPage;
import minerva.seite.helpkeys.HelpKeysPage;
import minerva.seite.link.CheckAllLinksPage;
import minerva.seite.link.CrossBookLinksPage;
import minerva.seite.link.LinkAnalysisPage;
import minerva.seite.link.LinkResolverPage;
import minerva.seite.tag.DeleteTagAction;
import minerva.seite.tag.TagCloudPage;
import minerva.seite.tag.TagWPage;
import minerva.seite.tag.TagsPage;
import minerva.subscription.AddMappingAction;
import minerva.subscription.BrokenMappingsPage;
import minerva.subscription.MappingPage;
import minerva.subscription.PushDataAction;
import minerva.subscription.SubscribeAction;
import minerva.task.MyTasksPage;
import minerva.task.TaskPrioAction;
import minerva.task.TasksCreatedByMePage;
import minerva.user.AddUserPage;
import minerva.user.DeleteUserAction;
import minerva.user.EditUserPage;
import minerva.user.UserAccess;
import minerva.user.UsersPage;
import minerva.user.quickbuttons.AddQuickbuttonAction;
import minerva.user.quickbuttons.DeleteQuickbuttonAction;
import minerva.user.quickbuttons.EditQuickbuttonPage;
import minerva.user.quickbuttons.QuickbuttonsPage;
import minerva.user.quickbuttons.TakeQuickbuttonAction;
import minerva.user.quickbuttons.ToggleQuickbuttonAction;
import minerva.user.quickbuttons.ToggleQuickbuttonsAction;
import minerva.validate.ValidationPage;
import minerva.validate.ValidatorService.UnusedImagesTimer;
import minerva.workspace.ActivateFSModeAction;
import minerva.workspace.AddWorkspacePage;
import minerva.workspace.BrokenLinksPage;
import minerva.workspace.CreateBranchPage;
import minerva.workspace.CurrentWorkspaceAction;
import minerva.workspace.DeactivateFSModePage;
import minerva.workspace.DeleteWorkspacePage;
import minerva.workspace.MergeBranchPage;
import minerva.workspace.PullWorkspaceAction;
import minerva.workspace.ToggleColoredHeadingsAction;
import minerva.workspace.WorkspaceHistoryPage;
import minerva.workspace.WorkspacesPage;
import spark.Spark;

public class MinervaWebapp extends RouteDefinitions {
    public static final String VERSION = "4.02.0";
    private static MinervaFactory factory;
    
    @Override
    public void routes() {
        workspacesAndBooks();
        oneBook();
        page();
        images();
        tags();
        comments();
        users();
        values();
        misc();
        restApi();
    }

    private void workspacesAndBooks() { // Workspaces, 1 workspace == n books
        get("/", WorkspacesPage.class);
        get("/w", CurrentWorkspaceAction.class);
        get("/w/:branch", BooksPage.class);
        get("/w/:branch/pull", PullWorkspaceAction.class);
        form("/w/:branch/exclusions/edit", ExclusionsEditPage.class);
        get("/w/:branch/delete", DeleteWorkspacePage.class);
        get("/w/:branch/my-tasks/:id", TaskPrioAction.class);
        get("/w/:branch/my-tasks", MyTasksPage.class);
        get("/w/:branch/tasks-created-by-me", TasksCreatedByMePage.class);
        get("/w/:branch/pages-watched-by-me", PagesWatchedByMePage.class);
        form("/create-workspace", AddWorkspacePage.class);
        get("/w/:branch/help-keys", AllHelpKeysPage.class);
        get("/w/:branch/history", WorkspaceHistoryPage.class);
        get("/w/:branch/index", IndexBooksAction.class);
        form("/w/:branch/search", SearchPage.class);
        get("/w/:branch/push-data", PushDataAction.class);
        get("/w/:branch/language", SelectLanguageAction.class);
        get("/w/:branch/broken-mappings", BrokenMappingsPage.class);
        get("/w/:branch/broken-links", BrokenLinksPage.class);
        get("/w/:branch/activate-f-s-mode", ActivateFSModeAction.class);
        form("/w/:branch/deactivate-f-s-mode", DeactivateFSModePage.class);
        get("/w/:branch/recycle/pop/:id", RecycleAction.class);
        get("/w/:branch/recycle/delete/:id", DeleteWeggeworfeneSeiteAction.class);
        get("/w/:branch/recycle/subpages/:id", PapierkorbUnterseitenPage.class);
        form("/w/:branch/recycle", PapierkorbPage.class);
        get("/w/:branch/customer-mode/:customer", SelectCustomerModeAction.class);
        get("/w/:branch/customer-mode", CustomerModePage.class);
        get("/w/:branch/toggle-colored-headings", ToggleColoredHeadingsAction.class);
        get("/w/:branch/menu", MenuPage.class);

        // Export
        get("/w/:branch/books/export", ExportWorkspaceAction.class); // all books
        form("/w/:branch/export", ExportPage.class); // Export selection page
        form("/w/:branch/pages/export", SeitenauswahlPage.class);
        get("/w/:branch/download-export/:id/:dn", DownloadExportPage.class);

        get("/ets/:branch", ExportTemplateSetsPage.class);
        get("/ets/:branch/add", AddExportTemplateSetAction.class);
        form("/ets/:branch/edit/:id", EditExportTemplateSetPage.class);
        get("/ets/:branch/delete/:id", DeleteExportTemplateSetAction.class);
    }

    private void oneBook() {
        form("/b/:branch/add", AddBookPage.class);
        form("/b/:branch/:book/edit", EditBookPage.class);
        get("/b/:branch/:book/delete", DeleteBookPage.class);
        get("/b/:branch/:book", BookPage.class);
        form("/b/:branch/:book/order", OrderTopLevelSeitePage.class);
        get("/b/:branch/:book/sort", SortTopLevelSeiteAction.class);
        get("/b/:branch/:book/cal", CheckAllLinksPage.class);
        get("/b/:branch/:book/show-all-pages", ToggleShowAllPagesAction.class);
        get("/b/:branch/:book/export", ExportBookAction.class);
        get("/b/:branch/:book/validate", ValidationPage.class);
        get("/b/", CurrentWorkspaceAction.class); // falls man sich dahin verirren sollte
    }

    private void page() {
        get("/s/:branch/:book/:id", ViewSeitePage.class);
        form("/s-edit/:branch/:book/:id", EditSeitePage.class); // Wegen den Images hänge ich hier nicht "/edit" hinten dran, sondern ändere den 1. Pfadteil auf "s-edit".
        get("/s-dont/:branch/:book/:id", DontChangePage.class);
        form("/s/:branch/:book/:id/html", EditHtmlPage.class);
        post("/post-contents/:type", PostContentsAction.class);
        get("/s/:branch/:book/:id/cancel", CancelEditingAction.class);
        get("/s/:branch/:book/:id/locked", LockedByPage.class);
        get("/s/:branch/:book/:id/pull", PullSeiteAction.class);
        get("/s/:branch/:book/:id/delete", DeleteSeitePage.class);
        get("/s/:branch/:book/:id/duplicate", DuplicateSeitePage.class);
        get("/s/:branch/:book/:parentid/add", AddSeiteAction.class);
        form("/s/:branch/:book/:id/order", OrderSeitePage.class);
        get("/s/:branch/:book/:id/sort", SortSeiteAction.class);
        get("/s/:branch/:book/:id/move-select-target", MoveSeitePage.class);
        get("/s/:branch/:book/:id/move-ack", MoveSeiteAckPage.class);
        get("/s/:branch/:book/:id/move", MoveSeiteAction.class);
        get("/s/:branch/:book/:id/history", SeiteHistoryPage.class);
        form("/s/:branch/:book/:id/help-keys", HelpKeysPage.class);
        form("/s/:branch/:book/:id/mapping", MappingPage.class);
        form("/s/:branch/:book/:id/add-mapping", AddMappingAction.class);
        get("/s/:branch/:book/:id/toggle-favorite", ToggleFavoriteAction.class);
        get("/s/:branch/:book/:id/toggle-watch", ToggleWatchAction.class);
        get("/s/:branch/:book/:id/links", LinkAnalysisPage.class);
        post("/s/:branch/:book/:id/toc", TocAction.class);
        post("/s/:branch/:book/:id/editorsnote", SaveEditorsNoteAction.class);
        form("/s/:branch/:book/:id/help-keys/:lang/:h", HelpKeysForHeadingPage.class);
        get("/s/:branch/:book/:id/cleanup-hkh", CleanupHelpKeysForHeadingsAction.class);
        get("/s/:branch/:book/:id/cross-book-links", CrossBookLinksPage.class);
        post("/s/:branch/:book/:id/live-save", LiveSaveSeiteAction.class);
        form("/s/:branch/:book/:id/ctw", CopyToWorkspacePage.class);
        
        // Attachments
        get("/s/:branch/:book/:id/attachments/:dn", DownloadAttachmentAction.class);
        get("/s/:branch/:book/:id/attachments", AttachmentsPage.class);
        post("/s/:branch/:book/:id/upload-attachment", UploadAttachmentAction.class);
        form("/s/:branch/:book/:id/edit-attachment/:dn", EditAttachmentPage.class);
        get("/s/:branch/:book/:id/delete-attachment/:dn", DeleteAttachmentAction.class);
        post("/attachment-category", SaveUserAttachmentCategoryAction.class);

        // Links
        form("/links/:branch/:book/:id", LinkResolverPage.class);
    }

    private void images() {
        // upload
        post("/s-image-upload/:branch/:book/:id", ImageUploadAction.class);
        post("/image-upload/:branch/:book/:id/comment", ImageUploadAction.class);
        
        // download
        get("/s/:branch/:book/img/:id/:dn", ImageDownloadAction.class);
        get("/s-edit/:branch/:book/img/:id/:dn", ImageDownloadAction.class); // Image download must also work in edit mode.

        get("/sc/:branch/:book/:id/img/:commentId/:dn", ImageDownloadAction.class); // view+edit mode
    }

    private void tags() {
        form("/s/:branch/:book/:id/tags", TagsPage.class);
        get("/s/:branch/:book/:id/delete-tag", DeleteTagAction.class);
        get("/w/:branch/tag/:tag", TagWPage.class);
        get("/w/:branch/tag-cloud", TagCloudPage.class);
    }

    private void comments() {
        get("/sc/:branch/:book/:id/comments", CommentsPage.class);
        form("/sc/:branch/:book/:id/comment", EditCommentPage.class);
        get("/sc/:branch/:book/:id/delete-comment", DeleteCommentAction.class);
        get("/sc/:branch/:book/:id/comment-done", CommentDoneAction.class);
    }

    private void users() {
        get("/users", UsersPage.class);
        form("/user/add", AddUserPage.class);
        form("/user/:login", EditUserPage.class);
        get("/user/:login/delete", DeleteUserAction.class);
        get("/gitlab-auth", GitlabAuthAction.class);
        get("/gitlab-auth-callback", GitlabAuthCallbackAction.class);
        addNotProtected("/gitlab-auth");
        form("/backdoor", Login2Page.class);
        addNotProtected("/backdoor");
        get("/activate-admin-rights", ActivateAdminRightsAction.class);
        get("/error", CustomErrorPage.class);
        form("/q/config", QuickbuttonsPage.class);
        get("/q/add", AddQuickbuttonAction.class);
        get("/q/take", TakeQuickbuttonAction.class);
        get("/q/only-me", ToggleQuickbuttonAction.class);
        get("/q/delete", DeleteQuickbuttonAction.class);
        form("/q/edit", EditQuickbuttonPage.class);
        get("/w/:branch/toggle-quickbuttons", ToggleQuickbuttonsAction.class);
    }

    private void values() {
        form("/values/:branch/add", AddValuesPage.class);
        form("/values/:branch/edit/:key", EditValuesPage.class);
        get("/values/:branch/delete/:key", DeleteValuesAction.class);
        get("/values/:branch", ValuesListPage.class);
    }

    private void misc() {
        get("/logged-out", LoggedOutPage.class);
        addNotProtected("/logged-out");
        get("/message", MessagePage.class);
        addNotProtected("/message");
        get("/migration/:branch", MigrationPage.class);
        get("/math", MathPage.class);
        get("/serverlog", ServerlogPage.class);
        form("/branch/:branch", CreateBranchPage.class);
        form("/merge/:branch", MergeBranchPage.class);
        get("/rest/ping", PingAction.class);
        Spark.get("/rest/cleanup-journals", (req, res) -> JournalSO.cleanupAllJournals());
        form("/config", EditConfigPage.class);
        form("/tablesort/:id/:col", TableSortAction.class);
        get("/update-metrics", UpdatePagesMetricsAction.class);
        addNotProtected("/update-metrics");
        get("/tosmap", TosmapInfoPage.class);
        addNotProtected("/tosmap");
    }

    private void restApi() {
        addNotProtected("/rest");
        get("/rest/info", InfoAction.class); // not protected
        get("/rest/publish", PublishAction.class); // protected by query params
        get("/rest/subscribe", SubscribeAction.class); // protected by known subscribers
        post("/book6/login", Book6LoginAction.class);
        addNotProtected("/book6/login");
        get("/book6/start", Book6StartAction.class);
        addNotProtected("/book6/start");
    }

    public static void main(String[] args) {
        WebApp webapp = getWebAppBuilder(VERSION)
            .withPageInitializer(new MinervaPageInitializer())
            .withRoutes(new MinervaWebapp())
            .build();
        webapp.boot();
        info();
    }
    
    public static WebAppBuilder getWebAppBuilder(String version) {
        return new WebAppBuilder(version)
                .withLogging(new LoggingInitializer(Level.INFO, "{date} {level}  {message}"))
                .withTemplatesFolders(MinervaWebapp.class, "/templates")
                .withErrorPage(MinervaErrorPage.class, MinervaError404Page.class)
                .withInitializer(config -> MinervaOptions.options = new MinervaOptions(MinervaOptions.getConfigFile(MinervaOptions.MAIN_CONFIG, config)))
                .withInitializer(config -> factory = new MinervaFactory(new MinervaConfig(config)))
                .withInitializer(config -> initGitper())
                .withInitializer(config -> CommentService.services.put(ctx -> ctx.path().startsWith("/sc/"), SeiteCommentService.class))
                .withInitializer(config -> {
                    Timer.TIMER_ACTIVE = MinervaOptions.TIMER_ACTIVE.get();
                    Timer.TIMER_ACTIVE_LABEL = MinervaOptions.TIMER_ACTIVE.getLabel();
                    Timer.create(config);
                    Timer.INSTANCE.createTimer(CleanupExportFolderTimer.class, "0 0 3 ? * *"); // 3:00 daily
                    Timer.INSTANCE.createTimer(HourlyJournalTimer.class, "0 0 8-19 * * ?"); // daily every hour   TODO später auf alle 3h setzen
                    Timer.INSTANCE.createTimer(JournalTimer.class, "0 0 6 1 * ?"); // first day of month 6:00
                    Timer.INSTANCE.createTimer(IndexBooksTimer.class, "0 15 8 ? * MON-FRI");
                    Timer.INSTANCE.createTimer(UnusedImagesTimer.class, UnusedImagesTimer.cron(), true); // 23:00
                })
                .withAuth(config -> new MinervaAuth());
    }
    
    public static void info() {
        System.out.println("languages: " + MinervaWebapp.factory().getLanguages()
                + " | backend: " + MinervaWebapp.factory().getBackendService().getInfo("en")
                + MinervaWebapp.factory().getFolderInfo() + " | workspaces folder: " + factory().getConfig().getWorkspacesFolder());
    }
    
    public static MinervaFactory factory() {
        return factory;
    }
    
    public static void bootForTest() {
        factory = new MinervaFactory(new MinervaConfig(new AppConfig()));        
    }
    
    private static void initGitper() {
        Gitper.gitperInterface = new GitperInterface() {
            
            @Override
            public void login2(Context ctx, gitper.User user) {
                MinervaAuth.login2(ctx, (minerva.user.User) user);
            }
            
            @Override
            public Object initWithAccessToken(String token) {
                return GitFactory.initWithAccessToken(token, new MinervaGitlabConfig().getGitlabUrl());
            }
            
            @Override
            public User loadUser(String login, boolean create, String mail) {
                return UserAccess.loadUser(login, create, mail);
            }
            
            @Override
            public gitper.User createUser(String login) {
                minerva.user.User user = new minerva.user.User();
                user.setLogin(login);
                return user;
            }
            
            @Override
            public Object tosmap_pop(String state) {
                return Tosmap.pop(state);
            }
            
            @Override
            public void tosmap_add(String state, long t) {
                Tosmap.add(state, t, state);
            }

            @Override
            public GitlabAuthService authService() {
                return new GitlabAuthService(new MinervaGitlabConfig());
            }
        };
    }
}
