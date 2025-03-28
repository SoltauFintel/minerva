package minerva.seite.actions;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.base.DeliverHtmlContent;
import minerva.book.BookPage;
import minerva.model.SeiteSO;
import minerva.seite.SPage;

public class DeleteSeitePage extends SPage {
    public static DeliverHtmlContent<SeiteSO> additionalInfo = seite -> "";
    
    @Override
    protected void execute() {
        render = false;
        String title = seite.getTitle();
        String parentId = seite.getSeite().getParentId();

        header(n("deletePage"));

        if ("d".equals(ctx.queryParam("m"))) {
            workspace.getPapierkorb().push(seite);
            seite.remove();
            seite.log("page deleted");

            Logger.info("Page (incl. all subpages) deleted by " + user.getLogin() + ": " + branch + "/"
                    + bookFolder + "/" + id + " \"" + title + "\"");

            if (parentId.equals(SeiteSO.ROOT_ID)) {
                ctx.redirect(booklink);
            } else {
                ctx.redirect("/s/" + esc(branch) + "/" + esc(bookFolder) + "/" + esc(parentId));
            }
        } else {
            DataList list = list("linkingPages");
            for (SeiteSO s : seite.linksTo(langs)) {
                DataMap map = list.add();
                map.put("link", "/s/" + branch + "/" + bookFolder + "/" + s.getId());
                map.put("title", esc(s.getTitle()));
            }
			put("additionalInfo", esc(n(additionalInfo.getHTML(seite))));
            put("hasLinkingPages", !list.isEmpty());
            put("pagesLinkToThisPage", list.size() == 1 ? n("pageLinkToThisPage") : n("pagesLinkToThisPage"));
            render = true;
            if (book.isNotPublic()) {
                BookPage.oneLang(model, book);
            }
        }
    }
}
