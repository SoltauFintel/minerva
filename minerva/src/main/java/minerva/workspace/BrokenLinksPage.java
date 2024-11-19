package minerva.workspace;

import java.util.List;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.config.MinervaOptions;
import minerva.workspace.BrokenLinksService.BLBrokenLink;
import minerva.workspace.BrokenLinksService.BLLanguage;
import minerva.workspace.BrokenLinksService.BLPage;

public class BrokenLinksPage extends WPage {

    @Override
    protected void execute() {
        Logger.info(user.getLogin() + " | Broken Links");

        List<BLPage> pages = new BrokenLinksService().load(workspace);
        
        header("Broken Links");
        DataList list = list("pages");
        String info = "<p>O.g. Seite hat Verweise auf die folgenden Seiten, die aber nicht erreichbar sind:</p>";
        for (BLPage page : pages) {
            DataMap map = list.add();
            map.put("id", esc(page.getId()));
            map.put("title", esc(page.getTitle()));
            DataList list2 = map.list("languages");
            for (BLLanguage language : page.getLanguages()) {
                DataMap map2 = list2.add();
                map2.put("language", esc(language.getLanguage()));
                map2.put("info", info); info = "";
                DataList list3 = map2.list("brokenLinks");
                for (BLBrokenLink bl : language.getBrokenLinks()) {
                    DataMap map3 = list3.add();
                    map3.put("id", esc(bl.getId()));
                    map3.put("title", esc(bl.getTitle()));
                    map3.put("customers", esc(bl.getCustomers().stream().collect(Collectors.joining(", "))));
                }
            }
        }
        put("bookFolder", workspace.getBooks().get(0).getBook().getFolder());
        put("ohHosts", esc(MinervaOptions.OH_HOSTS.get()));
    }
}
