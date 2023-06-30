package minerva;

import org.pmw.tinylog.Level;

import github.soltaufintel.amalia.web.WebApp;
import github.soltaufintel.amalia.web.builder.LoggingInitializer;
import github.soltaufintel.amalia.web.builder.WebAppBuilder;
import github.soltaufintel.amalia.web.config.AppConfig;
import github.soltaufintel.amalia.web.route.RouteDefinitions;
import minerva.auth.Login2Page;
import minerva.auth.MinervaAuth;
import minerva.base.MessagePage;
import minerva.base.MinervaError404Page;
import minerva.base.MinervaErrorPage;
import minerva.base.MinervaPageInitializer;
import minerva.book.AddBookPage;
import minerva.book.BookPage;
import minerva.book.BooksPage;
import minerva.book.DeleteBookPage;
import minerva.book.EditBookPage;
import minerva.book.OrderTopLevelSeitePage;
import minerva.book.SelectLanguageAction;
import minerva.book.SortTopLevelSeiteAction;
import minerva.config.InfoAction;
import minerva.config.MinervaConfig;
import minerva.config.MinervaFactory;
import minerva.exclusions.ExclusionsEditPage;
import minerva.image.ImageDownloadAction;
import minerva.image.ImageUploadAction;
import minerva.migration.MigrationPage;
import minerva.persistence.gitlab.GitlabAuthAction;
import minerva.persistence.gitlab.GitlabAuthCallbackAction;
import minerva.preview.PreviewBookPage;
import minerva.preview.PreviewCustomerPage;
import minerva.preview.PreviewPage;
import minerva.publish.PublishAction;
import minerva.search.IndexWorkspaceAction;
import minerva.search.SearchPage;
import minerva.seite.AddSeiteAction;
import minerva.seite.AllHelpKeysPage;
import minerva.seite.DeleteSeitePage;
import minerva.seite.EditSeitePage;
import minerva.seite.HelpKeysPage;
import minerva.seite.MoveSeiteAckPage;
import minerva.seite.MoveSeiteAction;
import minerva.seite.MoveSeitePage;
import minerva.seite.OrderSeitePage;
import minerva.seite.PostContentsAction;
import minerva.seite.PullSeiteAction;
import minerva.seite.SeiteHistoryPage;
import minerva.seite.SortSeiteAction;
import minerva.seite.ViewSeitePage;
import minerva.seite.link.CheckAllLinksPage;
import minerva.seite.link.LinkResolverPage;
import minerva.seite.note.AddNotePage;
import minerva.seite.note.AllNotesPage;
import minerva.seite.note.DeleteNoteAction;
import minerva.seite.note.EditNotePage;
import minerva.seite.note.MyTasksPage;
import minerva.seite.note.NoteDoneAction;
import minerva.seite.note.NotesPage;
import minerva.seite.tag.DeleteTagAction;
import minerva.seite.tag.TagCloudPage;
import minerva.seite.tag.TagWPage;
import minerva.seite.tag.TagsPage;
import minerva.subscription.SubscribeAction;
import minerva.workspace.AddWorkspacePage;
import minerva.workspace.CurrentWorkspaceAction;
import minerva.workspace.DeleteWorkspacePage;
import minerva.workspace.PullWorkspace;
import minerva.workspace.WorkspacesPage;
import spark.Spark;

public class MinervaWebapp extends RouteDefinitions {
    public static final String VERSION = "0.2.0";
    private static MinervaFactory factory;
    
    @Override
    public void routes() {
        // TODO /w (=Workspace(s)) und /b (=Book(s)) sind nicht klar getrennt, wobei
        //      Workspace=Books was ja auch keine klare Trennung ist. Vielleicht /b nur bei (1) book verwenden?!
        
        // Workspace
        get("/", WorkspacesPage.class);
        get("/w", CurrentWorkspaceAction.class);
        get("/w/:branch/pull", PullWorkspace.class);
        get("/w/:branch/delete", DeleteWorkspacePage.class);
        form("/w/:branch/exclusions/edit", ExclusionsEditPage.class);
        get("/w/:branch/my-tasks", MyTasksPage.class);
        form("/create-workspace", AddWorkspacePage.class);
        get("/w/:branch/help-keys", AllHelpKeysPage.class);
        
        // Book
        get("/b/:branch", BooksPage.class);
        form("/b/:branch/add", AddBookPage.class);
        get("/b/:branch/index", IndexWorkspaceAction.class);
        get("/b/:branch/search", SearchPage.class);
        form("/b/:branch/:book/edit", EditBookPage.class);
        get("/b/:branch/:book/delete", DeleteBookPage.class);
        get("/b/:branch/language", SelectLanguageAction.class);
        get("/b/:branch/:book", BookPage.class);
        form("/b/:branch/:book/order", OrderTopLevelSeitePage.class);
        get("/b/:branch/:book/sort", SortTopLevelSeiteAction.class);
        get("/b/:branch/:book/notes", AllNotesPage.class);
        get("/b/:branch/:book/cal", CheckAllLinksPage.class);
        get("/b/", CurrentWorkspaceAction.class); // falls man sich dahin verirren sollte
        
        // Seite
        get("/s/:branch/:book/:id", ViewSeitePage.class);
        form("/s-edit/:branch/:book/:id", EditSeitePage.class); // Wegen den Images hänge ich hier nicht "/edit" hinten dran, sondern ändere den 1. Pfadteil auf "s-edit".
        post("/s/:branch/:book/:id/post-contents", PostContentsAction.class);
        get("/s/:branch/:book/:id/pull", PullSeiteAction.class);
        get("/s/:branch/:book/:id/delete", DeleteSeitePage.class);
        get("/s/:branch/:book/:parentid/add", AddSeiteAction.class);
        form("/s/:branch/:book/:id/order", OrderSeitePage.class);
        get("/s/:branch/:book/:id/sort", SortSeiteAction.class);
        get("/s/:branch/:book/:id/move-select-target", MoveSeitePage.class);
        get("/s/:branch/:book/:id/move-ack", MoveSeiteAckPage.class);
        get("/s/:branch/:book/:id/move", MoveSeiteAction.class);
        get("/s/:branch/:book/:id/history", SeiteHistoryPage.class);
        form("/s/:branch/:book/:id/help-keys", HelpKeysPage.class);
        
        // Image
        post("/s-image-upload/:branch/:book/:id", ImageUploadAction.class);
        get("/s/:branch/:book/img/:id/:dn", ImageDownloadAction.class);
        get("/s-edit/:branch/:book/img/:id/:dn", ImageDownloadAction.class); // Image download must also work in edit mode.
        get("/p/:branch/:customer/:book/:lang/img/:id/:dn", ImageDownloadAction.class); // Image download must also work in preview mode.

        // Links
        form("/links/:branch/:book/:id", LinkResolverPage.class);
        
        // tags
        form("/s/:branch/:book/:id/tags", TagsPage.class);
        get("/s/:branch/:book/:id/delete-tag", DeleteTagAction.class);
        get("/w/:branch/tag/:tag", TagWPage.class);
        get("/w/:branch/tag-cloud", TagCloudPage.class);
        
        // Notes
        get("/s/:branch/:book/:id/notes", NotesPage.class);
        form("/s/:branch/:book/:id/add-note", AddNotePage.class);
        form("/s/:branch/:book/:id/edit-note", EditNotePage.class);
        form("/s/:branch/:book/:id/delete-note", DeleteNoteAction.class);
        form("/s/:branch/:book/:id/note-done", NoteDoneAction.class);

        // Preview
        get("/p/:branch/:customer/:book/:lang/:id", PreviewPage.class);
        get("/p/:branch/:customer/:book/:lang", PreviewBookPage.class);
        get("/p/:branch", PreviewCustomerPage.class);
        
        // Sonstiges
        get("/message", MessagePage.class);
        get("/migration/:branch", MigrationPage.class);
        get("/gitlab-auth", GitlabAuthAction.class);
        get("/gitlab-auth-callback", GitlabAuthCallbackAction.class);
        addNotProtected("/gitlab-auth");
        form("/login2", Login2Page.class);
        addNotProtected("/login2");
        
        // REST API
        addNotProtected("/rest");
        get("/rest/info", InfoAction.class); // not protected
        Spark.get("/rest/publish", new PublishAction()); // protected by query params // TODO Ist das mit Amalia Code möglich?
        get("/rest/subscribe", SubscribeAction.class); // protected by known subscribers
    }

    public static void main(String[] args) {
        WebApp webapp = new WebAppBuilder(VERSION)
            .withLogging(new LoggingInitializer(Level.INFO, "{date} {level}  {message}"))
            .withTemplatesFolders(MinervaWebapp.class, "/templates")
            .withPageInitializer(new MinervaPageInitializer())
            .withErrorPage(MinervaErrorPage.class, MinervaError404Page.class)
            .withInitializer(config -> factory = new MinervaFactory(new MinervaConfig(config)))
            .withAuth(new MinervaAuth())
            .withRoutes(new MinervaWebapp())
            .build();
        webapp.boot();
    }
    
    public static MinervaFactory factory() {
        return factory;
    }
    
    public static void bootForTest() {
        factory = new MinervaFactory(new MinervaConfig(new AppConfig()));        
    }
}
