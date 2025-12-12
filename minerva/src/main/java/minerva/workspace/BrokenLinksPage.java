package minerva.workspace;

import java.util.List;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.base.IdGenerator;
import gitper.base.StringService;
import minerva.config.MinervaOptions;
import minerva.workspace.BrokenLinksService.BLBrokenLink;
import minerva.workspace.BrokenLinksService.BLLanguage;
import minerva.workspace.BrokenLinksService.BLPage;
import minerva.workspace.BrokenLinksService.BLPages;

public class BrokenLinksPage extends WPage {
    private boolean showAll;
    private boolean hidden = false;
    private DataMap map;
    private DataMap map2;
    private int n = 0;
    private int nn = 0;
    
    @Override
    protected void execute() {
        showAll = "all".equals(ctx.queryParam("m"));
        Logger.info(user.getLogin() + " | " + branch + " | Broken Links");

        BLPages pages = new BrokenLinksService().load(workspace);
        var ignored = user.getIgnoredBrokenLinks();
        
        header(n("BrokenLinks"));
        put("ohHosts", esc(MinervaOptions.OH_HOSTS.get().replace("\r", "").replace("\n", ", ")));
        put("bookFolder", esc(workspace.getBooks().get(0).getBook().getFolder()));
        put("showAll", showAll);
        String info = "<p>" + esc(n("brokenLinksInfo")) + ":</p>";
        DataList list = list("pages");
        for (BLPage page : pages.getPages()) {
            map = list.add();
            map.put("id", esc(page.getId()));
            map.put("title", esc(page.getTitle()));
            map.put("hasTitle", !page.getTitle().equals(page.getId()));
            map.put("book", esc(StringService.isNullOrEmpty(page.getBookTitle()) ? "" : "(" + page.getBookTitle() + ")"));
            map.put("visible", false);
            DataList list2 = map.list("languages");
            for (BLLanguage language : page.getLanguages()) {
                map2 = list2.add();
                map2.put("language", esc(language.getLanguage().toUpperCase()));
                map2.put("info", info); info = "";
                map2.put("visible", false);
                DataList list3 = map2.list("brokenLinks");
                for (BLBrokenLink bl : language.getBrokenLinks()) {
                    brokenLink(bl, page, ignored, list3);
                }
                list3.sort((a, b) -> a.get("title").toString().compareTo(b.get("title").toString()));
            }
        }
        putInt("n", n);
        put("nn", "+" + (nn - n));
        put("hidden", hidden);
    }

    private void brokenLink(BLBrokenLink bl, BLPage page, List<String> ignored, DataList list3) {
        var id2 = bl.getId().contains("/") ? "link" + IdGenerator.code6(bl.getId()) : bl.getId();
        id2 = page.getId() + "_" + id2;
        var visible = !ignored.contains(id2);
        nn++;
        if (visible) {
            n++;
        } else {
            hidden = true;
        }
        if (showAll) {
            visible = true;
        }
        if (visible) {
            map2.put("visible", true);
            map.put("visible", true);
        }
        DataMap map3 = list3.add();
        map3.put("id", esc(bl.getId()));
        map3.put("id2", esc(id2));
        map3.put("ignored", ignored.contains(id2));
        map3.put("visible", visible);
        map3.put("title", esc(bl.getTitle()));
        map3.put("hasTitle", !bl.getTitle().equals(bl.getId()));
        map3.put("customers", esc(bl.getCustomers().stream().collect(Collectors.joining(", "))));
        map3.put("bookFolder", esc(bl.getBookFolder()));
        map3.put("is404", bl.getErrorType().contains("(404)"));
        map3.put("errorType", esc(bl.getErrorType()));
        DataList list4 = map3.list("tags");
        bl.getTags().forEach(tag -> list4.add().put("tag", esc(tag)));
    }
}
