package minerva;

import static github.soltaufintel.amalia.web.action.Escaper.esc;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.auth.webcontext.WebContext;
import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.web.WebApp;
import github.soltaufintel.amalia.web.action.Page;
import github.soltaufintel.amalia.web.action.PageInitializer;
import github.soltaufintel.amalia.web.builder.WebAppBuilder;
import github.soltaufintel.amalia.web.route.RouteDefinitions;
import minerva.auth.MinervaAuth;
import minerva.base.MessagePage;
import minerva.base.MinervaError404Page;
import minerva.base.MinervaErrorPage;
import minerva.base.NLS;
import minerva.book.AddBookPage;
import minerva.book.BookPage;
import minerva.book.BooksPage;
import minerva.book.DeleteBookPage;
import minerva.book.EditBookPage;
import minerva.book.OrderTopLevelSeitePage;
import minerva.book.SelectLanguageAction;
import minerva.book.SortTopLevelSeiteAction;
import minerva.config.MinervaConfig;
import minerva.config.MinervaFactory;
import minerva.image.ImageDownloadAction;
import minerva.image.ImageUploadAction;
import minerva.migration.MigrationPage;
import minerva.model.BookSO;
import minerva.model.BooksSO;
import minerva.model.StatesSO;
import minerva.model.UserSO;
import minerva.model.WorkspaceSO;
import minerva.seite.AddSeiteAction;
import minerva.seite.DeleteSeitePage;
import minerva.seite.EditSeitePage;
import minerva.seite.MoveSeiteAckPage;
import minerva.seite.MoveSeiteAction;
import minerva.seite.MoveSeitePage;
import minerva.seite.OrderSeitePage;
import minerva.seite.PostContentsAction;
import minerva.seite.PullSeiteAction;
import minerva.seite.SortSeiteAction;
import minerva.seite.ViewSeitePage;
import minerva.seite.link.LinkResolverPage;
import minerva.seite.note.AddNotePage;
import minerva.seite.note.DeleteNoteAction;
import minerva.seite.note.EditNotePage;
import minerva.seite.note.NotesPage;
import minerva.seite.tag.DeleteTagAction;
import minerva.seite.tag.TagCloudPage;
import minerva.seite.tag.TagWPage;
import minerva.seite.tag.TagsPage;
import minerva.workspace.AddWorkspacePage;
import minerva.workspace.CurrentWorkspaceAction;
import minerva.workspace.DeleteWorkspacePage;
import minerva.workspace.PullWorkspace;
import minerva.workspace.WorkspacesPage;

public class MinervaWebapp extends RouteDefinitions {
    public static final String VERSION = "0.2.0";
    private static MinervaFactory factory;
    
    @Override
    public void routes() {
        // Workspace
        get("/", WorkspacesPage.class);
        get("/w", CurrentWorkspaceAction.class);
        get("/w/:branch/pull", PullWorkspace.class);
        get("/w/:branch/delete", DeleteWorkspacePage.class);
        form("/create-workspace", AddWorkspacePage.class);
        
        // Book
        get("/b/:branch", BooksPage.class);
        form("/b/:branch/add", AddBookPage.class);
        form("/b/:branch/:book/edit", EditBookPage.class);
        get("/b/:branch/:book/delete", DeleteBookPage.class);
        get("/b/:branch/select-language/:lang", SelectLanguageAction.class);
        get("/b/:branch/:book", BookPage.class);
        form("/b/:branch/:book/order", OrderTopLevelSeitePage.class);
        get("/b/:branch/:book/sort", SortTopLevelSeiteAction.class);
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
        get("/s/:branch/:book/:id/move", MoveSeitePage.class);
        get("/s/:branch/:book/:id/move-ack", MoveSeiteAckPage.class);
        get("/s/:branch/:book/:id/moved", MoveSeiteAction.class);
        
        // Image
        post("/s-image-upload/:branch/:book/:id", ImageUploadAction.class);
        get("/s/:branch/:book/img/:id/:dn", ImageDownloadAction.class);
        get("/s-edit/:branch/:book/img/:id/:dn", ImageDownloadAction.class); // Image download must also work in edit mode.

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
        
        // Sonstiges
        get("/message", MessagePage.class);
        get("/migration/:branch", MigrationPage.class);
    }

    public static void main(String[] args) {
        WebApp webapp = new WebAppBuilder(VERSION)
            .withTemplatesFolders(MinervaWebapp.class, "/templates")
            .withPageInitializer(new MinervaPageInitializer())
            .withErrorPage(MinervaErrorPage.class, MinervaError404Page.class)
            .withAuth(new MinervaAuth())
            .withInitializer(config -> factory = new MinervaFactory(new MinervaConfig(config)))
            .withRoutes(new MinervaWebapp())
            .build();
        webapp.boot();
    }
    
    public static class MinervaPageInitializer extends PageInitializer {
        
        @Override
        public void initPage(Context ctx, Page page) {
            page.put("title", "Minerva");
            WebContext wctx = new WebContext(ctx);
            boolean hasUser = wctx.session().isLoggedIn();
            page.put("abmelden", "Abmelden?");
            page.put("hasUser", hasUser);
            page.put("user", esc(wctx.session().getLogin()));
            page.put("gitlab", factory().getConfig().isGitlab());
            booksForMenu(ctx, page, hasUser);
            if (hasUser) {
                UserSO user = StatesSO.get(ctx).getUser();
                String userLang = user.getLanguage();
                page.put("abmelden", NLS.get(userLang, "logout"));
            }
        }

        private void booksForMenu(Context ctx, Page page, boolean hasUser) {
            DataList list = page.list("booksForMenu");
            page.put("bookslinkForMenu", "/w");
            if (!hasUser) {
                page.put("branch", "");
                return;
            }
            UserSO user = StatesSO.get(ctx).getUser();
            String userLang = user.getLanguage();
            WorkspaceSO workspace = user.getCurrentWorkspace();
            if (workspace != null) {
                BooksSO books = workspace.getBooks();
                for (BookSO book : books) {
                    DataMap map = list.add();
                    map.put("folder", esc(book.getBook().getFolder()));
                    map.put("title", esc(book.getBook().getTitle().getString(userLang)));
                }
            }
            page.put("branch", esc(workspace.getBranch()));
        }
    }
    
    public static MinervaFactory factory() {
        return factory;
    }
}
