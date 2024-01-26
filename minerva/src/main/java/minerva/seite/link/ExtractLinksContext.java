package minerva.seite.link;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minerva.model.SeiteSO;

public class ExtractLinksContext {
    private final Map<String, List<Link>> cache = new HashMap<>();
    
    public List<Link> extractLinks(SeiteSO seite, String lang) {
        String key = seite.getId() + "#" + lang;
        List<Link> links = cache.get(key);
        if (links == null) {
            links = LinkService.extractLinks(seite.getContent().getString(lang), false);
            cache.put(key, links);
        }
        return links;
    }
}
