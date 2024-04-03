package minerva.mask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;

import minerva.access.CommitMessage;
import minerva.access.MultiPurposeDirAccess;
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
        return new MultiPurposeDirAccess(workspace.dao()).load(dn(tag), Mask.class);
    }
    
    public void saveMask(Mask mask) {
        new MultiPurposeDirAccess(workspace.dao()).save(dn(mask.getTag()), mask,
                new CommitMessage("Mask " + mask.getTag()), workspace);
    }
    
    private String dn(String tag) {
        return folder + "/" + tag + ".json";
    }
    
    public void deleteMask(String tag) {
        if (!new MultiPurposeDirAccess(workspace.dao()).delete(dn(tag), new CommitMessage("Delete mask " + tag), workspace)) {
            throw new RuntimeException("Error deleting mask " + tag + "!");
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
