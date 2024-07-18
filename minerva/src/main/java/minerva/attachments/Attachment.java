package minerva.attachments;

import java.util.ArrayList;
import java.util.List;

public class Attachment {
    private final String filename;
    private final List<String> categories = new ArrayList<>();

    public Attachment(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void fromString(String pCategories) {
        for (String cat : pCategories.split(",")) {
            cat = cat.trim();
            if (!cat.isEmpty()) {
                categories.add(cat);
            }
        }
    }
    
    public List<String> getCategories() {
        return categories;
    }
}
