package minerva.mask;

import java.util.Map;
import java.util.Map.Entry;

import org.pmw.tinylog.Logger;

import com.google.gson.Gson;

import minerva.access.CommitMessage;
import minerva.access.DirAccess;
import minerva.access.MultiPurposeDirAccess;
import minerva.base.StringService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;

public class FeatureFieldsService {

    public FeatureFields get(SeiteSO seite) {
        FeatureFields ff = new MultiPurposeDirAccess(seite.getBook().dao()).load(dn(seite), FeatureFields.class);
        return ff == null ? FeatureFields.create(seite) : ff;
    }
    
    public void set(SeiteSO seite, FeatureFields featureFields) {
        if (StringService.isNullOrEmpty(featureFields.getSeiteId())
                || StringService.isNullOrEmpty(featureFields.getMaskTag())) {
            throw new IllegalArgumentException("seiteId and/or maskTag must not be empty");
        }
        BookSO book = seite.getBook();
        new MultiPurposeDirAccess(book.dao()).save(dn(seite), featureFields, new CommitMessage(seite, "feature fields"), book.getWorkspace());
    }
    
    public void delete(SeiteSO seite) {
        BookSO book = seite.getBook();
        if (!new MultiPurposeDirAccess(book.dao()).delete(dn(seite), new CommitMessage(seite, "feature fields deleted"),
                book.getWorkspace())) {
            throw new RuntimeException("Error deleting feature fileds for page " + seite.getId());
        }
    }
    
    public static String dn(SeiteSO seite) {
        return seite.getBook().getFolder() + "/feature-fields/" + seite.getId() + ".ff";
    }
    
    public boolean findValue(SeiteSO excludeSeite, String id, String value) {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        DirAccess dao = excludeSeite.getBook().dao();
        long start = System.currentTimeMillis();
        Map<String, String> files = dao.loadAllFiles(excludeSeite.getBook().getFolder() + "/feature-fields", ".ff");
        Gson gson = new Gson();
        boolean ret = false;
        for (Entry<String, String> e : files.entrySet()) {
            if (!e.getKey().endsWith("/feature-fields/" + excludeSeite.getId() + ".ff")) {
                FeatureFields ff = gson.fromJson(e.getValue(), FeatureFields.class);
                String cv = ff.get(id);
                if (cv != null && cv.equals(value)) {
                    ret = true;
                    break;
                }
            }
        }
        long end = System.currentTimeMillis();
        Logger.info("find value \"" + value + "\" in field " + id + ": " + (ret ? "found" : "not found") + " | " + (end - start) + "ms");
        return ret;
    }
}
