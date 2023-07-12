package minerva.subscription;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.seite.SPage;

public class MappingPage extends SPage {

    @Override
    protected void execute() {
        PageTitles titles = new SubscriptionService().loadPageTitles();
        
        header(n("customerMapping"));
        put("pageTitle", esc(seite.getTitle()));
        DataList list = list("titles");
        DataList list2 = list("linked");
        boolean hasMappings = false;
        if (titles != null && titles.getLang() != null) {
            List<PageTitle> titleList = titles.getLang().get(user.getPageLanguage());
            for (PageTitle t : titleList) {
                DataMap map = list.add();
                String url = viewlink + "/add-mapping?ohid=" + u(t.getId());
                map.put("link", esc(url));
                map.put("forcelink", esc(url + "&m=f"));
                map.put("title", esc(t.getTitle()));
            }
            String forceText = n("force");
            for (String id : seite.getSeite().getHelpKeys()) {
                id = id.trim();
                boolean force = id.endsWith("!");
                if (force) {
                    id = id.substring(0, id.length() - "!".length()).trim();
                }
                DataMap map2 = list2.add();
                String x = "#" + id;
                for (PageTitle t : titleList) {
                    if (t.getId().equals(id)) {
                        x = t.getTitle();
                        break;
                    }
                }
                map2.put("title", esc(x + (force ? " (" + forceText + ")" : "")));
                map2.put("link", esc(viewlink + "/add-mapping?m=d&ohid=" + u(id)));
                hasMappings = true;
            }
        }
        put("delete", n("delete").toLowerCase());
        put("hasMappings", hasMappings);
    }
}
