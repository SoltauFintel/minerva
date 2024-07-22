package minerva;

import org.pmw.tinylog.Level;

import github.soltaufintel.amalia.web.WebApp;
import github.soltaufintel.amalia.web.builder.LoggingInitializer;
import github.soltaufintel.amalia.web.builder.WebAppBuilder;
import github.soltaufintel.amalia.web.config.AppConfig;
import github.soltaufintel.amalia.web.route.PingRouteDefinition.PingAction;
import github.soltaufintel.amalia.web.route.RouteDefinitions;
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
import minerva.base.MathPage;
import minerva.base.MessagePage;
import minerva.base.MinervaError404Page;
import minerva.base.MinervaErrorPage;
import minerva.base.MinervaPageInitializer;
import minerva.base.ServerlogPage;
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
import minerva.config.MinervaOptions;
import minerva.exclusions.ExclusionsEditPage;
import minerva.export.DownloadExportPage;
import minerva.export.ExportBookAction;
import minerva.export.ExportCsvBookAction;
import minerva.export.ExportPage;
import minerva.export.ExportWorkspaceAction;
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
import minerva.mask.AddMaskPage;
import minerva.mask.DeleteMaskAction;
import minerva.mask.EditFeatureFieldsPage;
import minerva.mask.EditRelationsPage;
import minerva.mask.FeaturesTablePage;
import minerva.mask.MasksPage;
import minerva.mask.ResponsibilitiesPage;
import minerva.mask.ViewMaskPage;
import minerva.mask.field.AddMaskFieldPage;
import minerva.mask.field.DeleteMaskFieldAction;
import minerva.mask.field.EditMaskFieldPage;
import minerva.mask.field.SortMaskFieldAction;
import minerva.migration.MigrationPage;
import minerva.model.JournalSO;
import minerva.model.JournalSO.JournalTimer;
import minerva.papierkorb.DeleteWeggeworfeneSeiteAction;
import minerva.papierkorb.PapierkorbPage;
import minerva.papierkorb.PapierkorbUnterseitenPage;
import minerva.papierkorb.RecycleAction;
import minerva.persistence.gitlab.GitlabAuthAction;
import minerva.persistence.gitlab.GitlabAuthCallbackAction;
import minerva.postcontents.PostContentsAction;
import minerva.preview.PreviewBookPage;
import minerva.preview.PreviewCustomerPage;
import minerva.preview.PreviewPage;
import minerva.publish.PublishAction;
import minerva.releasenotes.SelectRNCustomer2Page;
import minerva.releasenotes.SelectRNRelease2Page;
import minerva.search.IndexWorkspaceAction;
import minerva.search.SearchPage;
import minerva.seite.AddSeiteAction;
import minerva.seite.AllHelpKeysPage;
import minerva.seite.CancelEditingAction;
import minerva.seite.CleanupHelpKeysForHeadingsAction;
import minerva.seite.DeleteSeitePage;
import minerva.seite.DuplicateSeiteAction;
import minerva.seite.EditHtmlPage;
import minerva.seite.EditSeitePage;
import minerva.seite.HelpKeysForHeadingPage;
import minerva.seite.HelpKeysPage;
import minerva.seite.LockedByPage;
import minerva.seite.OrderSeitePage;
import minerva.seite.PagesWatchedByMePage;
import minerva.seite.PullSeiteAction;
import minerva.seite.SaveEditorsNoteAction;
import minerva.seite.SeiteHistoryPage;
import minerva.seite.SortSeiteAction;
import minerva.seite.TocAction;
import minerva.seite.ToggleFavoriteAction;
import minerva.seite.ToggleWatchAction;
import minerva.seite.ViewSeitePage;
import minerva.seite.link.CheckAllLinksPage;
import minerva.seite.link.LinkAnalysisPage;
import minerva.seite.link.LinkResolverPage;
import minerva.seite.move.MoveSeiteAckPage;
import minerva.seite.move.MoveSeiteAction;
import minerva.seite.move.MoveSeitePage;
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
import minerva.user.UsersPage;
import minerva.validate.ValidationPage;
import minerva.validate.ValidatorService.UnusedImagesTimer;
import minerva.workspace.ActivateFSModeAction;
import minerva.workspace.AddWorkspacePage;
import minerva.workspace.CreateBranchPage;
import minerva.workspace.CurrentWorkspaceAction;
import minerva.workspace.DeactivateFSModePage;
import minerva.workspace.DeleteWorkspacePage;
import minerva.workspace.MergeBranchPage;
import minerva.workspace.PullWorkspaceAction;
import minerva.workspace.WorkspaceHistoryPage;
import minerva.workspace.WorkspacesPage;
import spark.Spark;

public class MinervaWebapp extends RouteDefinitions {
    public static final String VERSION = "3.2.0";
    private static MinervaFactory factory;
    
    @Override
    public void routes() {
        workspacesAndBooks();
        oneBook();
        page();
        images();
        tags();
        comments();
        preview();
        users();
        values();
        masks();
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
        get("/w/:branch/index", IndexWorkspaceAction.class);
        form("/w/:branch/search", SearchPage.class);
        get("/w/:branch/push-data", PushDataAction.class);
        get("/w/:branch/language", SelectLanguageAction.class);
        get("/w/:branch/broken-mappings", BrokenMappingsPage.class);
        get("/w/:branch/activate-f-s-mode", ActivateFSModeAction.class);
        form("/w/:branch/deactivate-f-s-mode", DeactivateFSModePage.class);
        get("/w/:branch/recycle/pop/:id", RecycleAction.class);
        get("/w/:branch/recycle/delete/:id", DeleteWeggeworfeneSeiteAction.class);
        get("/w/:branch/recycle/subpages/:id", PapierkorbUnterseitenPage.class);
        form("/w/:branch/recycle", PapierkorbPage.class);
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
        get("/b/:branch/:book/export-csv", ExportCsvBookAction.class); // XXX temp.
        get("/b/:branch/:book/validate", ValidationPage.class);
        get("/b/:branch/:book/rn-select-customer2", SelectRNCustomer2Page.class);
        form("/b/:branch/:book/rn-select-release2", SelectRNRelease2Page.class);
        get("/b/", CurrentWorkspaceAction.class); // falls man sich dahin verirren sollte
    }

    private void page() {
        get("/s/:branch/:book/:id", ViewSeitePage.class);
        form("/s-edit/:branch/:book/:id", EditSeitePage.class); // Wegen den Images hänge ich hier nicht "/edit" hinten dran, sondern ändere den 1. Pfadteil auf "s-edit".
        form("/s/:branch/:book/:id/html", EditHtmlPage.class);
        post("/post-contents/:type", PostContentsAction.class);
        get("/s/:branch/:book/:id/cancel", CancelEditingAction.class);
        get("/s/:branch/:book/:id/locked", LockedByPage.class);
        get("/s/:branch/:book/:id/pull", PullSeiteAction.class);
        get("/s/:branch/:book/:id/delete", DeleteSeitePage.class);
        get("/s/:branch/:book/:id/duplicate", DuplicateSeiteAction.class);
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
        get("/p/:branch/:customer/:book/:lang/img/:id/:dn", ImageDownloadAction.class); // Image download must also work in preview mode.

        get("/sc/:branch/:book/:id/img/:commentId/:dn", ImageDownloadAction.class); // view mode
        get("/sc/:branch/:book/:id/img/:commentId/:dn", ImageDownloadAction.class); // edit mode
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

    private void preview() {
        get("/p/:branch/:customer/:book/:lang/:id", PreviewPage.class);
        get("/p/:branch/:customer/:book/:lang", PreviewBookPage.class);
        get("/p/:branch", PreviewCustomerPage.class);
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
    }

    private void values() {
        form("/values/:branch/add", AddValuesPage.class);
        form("/values/:branch/edit/:key", EditValuesPage.class);
        get("/values/:branch/delete/:key", DeleteValuesAction.class);
        get("/values/:branch", ValuesListPage.class);
    }

    private void masks() {
        get("/mask/:branch", MasksPage.class);
        form("/mask/:branch/add", AddMaskPage.class);
        get("/mask/:branch/:tag", ViewMaskPage.class);
        get("/mask/:branch/:tag/delete", DeleteMaskAction.class);
        form("/mask/:branch/:tag/add-field", AddMaskFieldPage.class);
        form("/mask/:branch/:tag/:id/edit-field", EditMaskFieldPage.class);
        get("/mask/:branch/:tag/:id/delete-field", DeleteMaskFieldAction.class);
        get("/mask/:branch/:tag/:id/sort-field", SortMaskFieldAction.class);
        
        form("/ff/:branch/:book/:id", EditFeatureFieldsPage.class);
        form("/fr/:branch/:book/:id", EditRelationsPage.class);
        form("/f/:branch/:book/:id", FeaturesTablePage.class);
        get("/responsibilities/:branch/:book", ResponsibilitiesPage.class);
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
                .withInitializer(config -> CommentService.services.put(ctx -> ctx.path().startsWith("/sc/"), SeiteCommentService.class))
                .withInitializer(config -> JournalTimer.start(config))
                .withInitializer(config -> UnusedImagesTimer.startTimer())
                .withAuth(new MinervaAuth());
    }
    
    public static void info() {
        System.out.println("languages: " + MinervaWebapp.factory().getLanguages()
                + " | backend: " + MinervaWebapp.factory().getBackendService().getInfo("en")
                + MinervaWebapp.factory().getFolderInfo());
    }
    
    public static MinervaFactory factory() {
        return factory;
    }
    
    public static void bootForTest() {
        factory = new MinervaFactory(new MinervaConfig(new AppConfig()));        
    }
}
