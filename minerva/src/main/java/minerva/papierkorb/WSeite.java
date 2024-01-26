package minerva.papierkorb;

import java.util.ArrayList;
import java.util.List;

import minerva.base.NlsString;

public class WSeite {
    private String id;
    private final NlsString title = new NlsString();
    private final List<WSeite> unterseiten = new ArrayList<>(); // id, title

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NlsString getTitle() {
        return title;
    }

    public List<WSeite> getUnterseiten() {
        return unterseiten;
    }
}
