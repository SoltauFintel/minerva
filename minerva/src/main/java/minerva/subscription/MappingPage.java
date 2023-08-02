package minerva.subscription;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.base.StringService;
import minerva.seite.SPage;

public class MappingPage extends SPage {

    @Override
    protected void execute() {
        PageTitles titles = new SubscriptionService().loadPageTitles();
        
        header(n("customerMapping"));
        put("delete", n("delete").toLowerCase());
        put("hasMappings", false);
        DataList titles_list = list("titles");
        DataList linked_list = list("linked");

        if (titles != null && titles.getLang() != null) {
            List<PageTitle> titleList = titles.getLang().get(user.getPageLanguage());
            titleList.sort((a, b) -> StringService.umlaute(a.getTitle()).compareTo(
                    StringService.umlaute(b.getTitle())));
            createPageTitlesList(titleList, titles_list);
            createLinkedPagesList(titleList, linked_list);
        }
    }

    private void createPageTitlesList(List<PageTitle> titleList, DataList list) {
        for (PageTitle title : titleList) {
            DataMap map = list.add();
            String url = viewlink + "/add-mapping?ohid=" + u(title.getId());
            map.put("link", esc(url));
            map.put("forcelink", esc(url + "&m=f"));
            map.put("title", esc(title.getTitle()));
        }
    }

    private void createLinkedPagesList(List<PageTitle> titleList, DataList list) {
        String forceText = n("force");
        for (String key : seite.getSeite().getHelpKeys()) {
            boolean force = key.endsWith("!");
            if (force) {
                key = key.substring(0, key.length() - "!".length());
            }
            String theTitle = getTitle(key, titleList) + (force ? " (" + forceText + ")" : "");
            DataMap map = list.add();
            map.put("title", esc(theTitle));
            map.put("link", esc(viewlink + "/add-mapping?m=d&ohid=" + u(key)));
            put("hasMappings", true);
        }
    }

    private String getTitle(String key, List<PageTitle> titleList) {
        return titleList.stream().filter(title -> title.getId().equals(key)).map(title -> title.getTitle()).findFirst().orElse("#" + key);
    }
}
