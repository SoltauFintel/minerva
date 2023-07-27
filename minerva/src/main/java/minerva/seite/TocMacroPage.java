package minerva.seite;

import java.util.List;
import java.util.Set;

public interface TocMacroPage {

    String getId();

    String getTitle(String lang);

    Set<String> getTags();
    
    boolean isVisible(String customer, String lang);

    int getTocHeadingsLevels();
    
    int getTocSubpagesLevels();

    List<TocMacroPage> getSubpages(String lang);
}
