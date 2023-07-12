package minerva.subscription;

import java.util.List;
import java.util.Map;

public class PageTitles {
    private Map<String, List<PageTitle>> lang;

    public Map<String, List<PageTitle>> getLang() {
        return lang;
    }

    public void setLang(Map<String, List<PageTitle>> lang) {
        this.lang = lang;
    }
}
