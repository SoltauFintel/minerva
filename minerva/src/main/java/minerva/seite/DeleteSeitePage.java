package minerva.seite;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.model.SeiteSO;

public class DeleteSeitePage extends SPage {

    @Override
    protected void execute() {
        String title = seite.getTitle();
        String parentId = seite.getSeite().getParentId();

        header(n("deletePage"));

        if ("d".equals(ctx.queryParam("m"))) {
            seite.remove();

            Logger.info("Page (incl. all subpages) deleted by " + user.getUser().getLogin() + ": " + branch + "/"
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
            put("hasLinkingPages", !list.isEmpty());
            put("pagesLinkToThisPage", list.size() == 1 ? n("pageLinkToThisPage") : n("pagesLinkToThisPage"));
        }
    }
}
