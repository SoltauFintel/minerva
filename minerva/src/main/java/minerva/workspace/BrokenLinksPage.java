package minerva.workspace;

import java.util.List;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import gitper.base.StringService;
import minerva.config.MinervaOptions;
import minerva.workspace.BrokenLinksService.BLBrokenLink;
import minerva.workspace.BrokenLinksService.BLCaller;
import minerva.workspace.BrokenLinksService.BLLanguage;
import minerva.workspace.BrokenLinksService.BLPage;
import minerva.workspace.BrokenLinksService.BLPages;
import minerva.workspace.BrokenLinksService.BrokenLink;

public class BrokenLinksPage extends WPage {

    @Override
    protected void execute() {
        Logger.info(user.getLogin() + " | " + branch + " | Broken Links");

        BLPages pages = new BrokenLinksService().load(workspace);
        
        header(n("BrokenLinks"));
        put("ohHosts", esc(MinervaOptions.OH_HOSTS.get().replace("\n", ", ")));
        put("bookFolder", esc(workspace.getBooks().get(0).getBook().getFolder()));
        String info = "<p>" + esc(n("brokenLinksInfo")) + ":</p>";
        DataList list = list("pages");
        for (BLPage page : pages.getPages()) {
            DataMap map = list.add();
            map.put("id", esc(page.getId()));
            map.put("title", esc(page.getTitle()));
            map.put("hasTitle", !page.getTitle().equals(page.getId()));
            map.put("book", esc(StringService.isNullOrEmpty(page.getBookTitle()) ? "" : esc("(" + page.getBookTitle() + ")")));
            DataList list2 = map.list("languages");
            for (BLLanguage language : page.getLanguages()) {
                DataMap map2 = list2.add();
                map2.put("language", esc(language.getLanguage().toUpperCase()));
                map2.put("info", info); info = "";
                DataList list3 = map2.list("brokenLinks");
                for (BLBrokenLink bl : language.getBrokenLinks()) {
                    DataMap map3 = list3.add();
                    map3.put("id", esc(bl.getId()));
                    map3.put("title", esc(bl.getTitle()));
                    map3.put("hasTitle", !bl.getTitle().equals(bl.getId()));
                    map3.put("customers", esc(bl.getCustomers().stream().collect(Collectors.joining(", "))));
                    map3.put("bookFolder", esc(bl.getBookFolder()));
                    DataList list4 = map3.list("tags");
                    bl.getTags().forEach(tag -> list4.add().put("tag", esc(tag)));
                }
                list3.sort((a, b) -> a.get("title").toString().compareTo(b.get("title").toString()));
            }
        }
        DataList list5 = list("other");
        for (BrokenLink bl : pages.getOtherBrokenLinks()) {
            DataMap map = list5.add();
            map.put("errorType", esc(bl.getErrorType()));
            map.put("url", esc(bl.getUrl()));
            map.put("customer", esc(bl.getCustomer()));
            DataList list6 = map.list("callers");
            for (List<BLCaller> clist : bl.getCallers().values()) {
                for (BLCaller c : clist) {
                    list6.add().put("details", esc(c.getDetails()));
                }
            }
        }
        put("hasOtherBrokenLinks", !pages.getOtherBrokenLinks().isEmpty());
    }
}