package minerva.mask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.Gson;

import minerva.access.CommitMessage;
import minerva.base.StringService;
import minerva.mask.field.MaskField;
import minerva.model.WorkspaceSO;

public class MasksService {
    private final WorkspaceSO workspace;
    private final String folder;
    
    public MasksService(WorkspaceSO workspace) {
        this.workspace = workspace;
        folder = workspace.getFolder() + "/masks";
    }

    public List<Mask> getMasks() {
        List<Mask> ret = new ArrayList<>();
        Map<String, String> files = workspace.dao().loadAllFiles(folder);
        Gson gson = new Gson();
        for (Entry<String, String> e : files.entrySet()) {
            if (e.getKey().endsWith(".json")) {
                ret.add(gson.fromJson(e.getValue(), Mask.class));
            }
        }
        ret.sort((a, b) -> a.getTag().compareToIgnoreCase(b.getTag()));
        return ret;
    }
    
    public Mask getMask(String tag) {
        Set<String> filenames = new HashSet<>();
        String dn = folder + "/" + tag + ".json";
        filenames.add(dn);
        Map<String, String> files = workspace.dao().loadFiles(filenames);
        String json = files.get(dn);
        return json == null ? null : new Gson().fromJson(json, Mask.class);
    }
    
    public void saveMask(Mask mask) {
        Map<String, String> files = new HashMap<>();
        files.put(folder + "/" + mask.getTag() + ".json", StringService.prettyJSON(mask));
        workspace.dao().saveFiles(files, new CommitMessage("Mask " + mask.getTag()), workspace);
    }
    
    public void deleteMask(String tag) {
        Set<String> filenames = new HashSet<>();
        filenames.add(folder + "/" + tag + ".json");
        List<String> cantBeDeleted = new ArrayList<>();
        workspace.dao().deleteFiles(filenames, new CommitMessage("Delete mask " + tag), workspace, cantBeDeleted);
        if (!cantBeDeleted.isEmpty()) {
            throw new RuntimeException("Deleting mask " + tag + " wasn't successfully!");
        }
        // TODO Nutzdaten auch löschen?
    }
    
    public void changeOrder(String tag, String id, boolean up) {
        Mask mask = getMask(tag);
        if (mask == null) {
            return;
        }
        List<MaskField> fi = mask.getFields();
        for (int i = 0; i < fi.size(); i++) {
            MaskField f = fi.get(i);
            if (f.getId().equals(id)) {
                if (up && i > 0) {
                    fi.remove(i);
                    fi.add(i - 1, f);
                    saveMask(mask);
                    return;
                } else if (!up && i + 1 < fi.size()) {
                    fi.remove(i);
                    fi.add(i + 1, f);
                    saveMask(mask);
                    return;
                }
            }
        }
    }
}
