package minerva.seite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;

public class AlleSeiten {
    /** key: Seite.parentId */
    private final Map<String, List<Seite>> seiten;
    
    public AlleSeiten(Map<String, String> files) {
        Gson gson = new Gson();
        seiten = files.entrySet().stream()
                .filter(e -> e.getKey().endsWith(SeiteSO.META_SUFFIX))
                .map(e -> gson.fromJson(e.getValue(), Seite.class))
                .filter(s -> s != null && s.getParentId() != null)
                .collect(Collectors.groupingBy(
                    Seite::getParentId,
                    HashMap::new,
                    Collectors.toList()
                ));
    }
    
    public int size() {
        return seiten.size();
    }

    public void fetch(String parentId, BookSO book, SeitenSO target) {
        var list = seiten.get(parentId);
        if (list != null) {
            for (Seite seite : list) {
                target.add(new SeiteSO(book, seite, this));
            }
        }
    }
}
