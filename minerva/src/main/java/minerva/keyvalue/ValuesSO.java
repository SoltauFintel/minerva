package minerva.keyvalue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.Gson;

import minerva.access.CommitMessage;
import minerva.access.MultiPurposeDirAccess;
import minerva.base.MList;
import minerva.model.WorkspaceSO;

public class ValuesSO extends MList<Values> {
    public static final String KEYTYPE_REGIMES = "REGIMES";
    public static final String KEYTYPE_SYNCHRON = "SYNCHRON";
    public static final String KEYTYPE_INSTRUMENTS = "INSTRUMENTS";
    public static final String KEYTYPE_BUSINESSTRANSACTIONS = "BUSINESS_TRANSACTIONS";
    public static final String KEYTYPE_USAGES = "USAGES";
    public static final String KEYTYPE_CUSTOMERS = "CUSTOMERS";
    
    private static final String DN_PREFIX = "values_";
    private final WorkspaceSO workspace;
    
    public ValuesSO(WorkspaceSO workspace) {
        super((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
        this.workspace = workspace;
        String folder = workspace.getFolder();
        Map<String, String> files = workspace.dao().loadAllFiles(folder);
        Gson gson = new Gson();
        for (Entry<String, String> e : files.entrySet()) {
            if (e.getKey().startsWith(DN_PREFIX)) {
                add(gson.fromJson(e.getValue(), Values.class));
            }
        }
    }

    public void saveNew(Values v) {
        for (Values i : this) {
            if (i.getKey().equals(v.getKey())) {
                throw new RuntimeException("Technischer Schlüsseltyp bereits vergeben!");
            }
        }
        add(v);
        String dn = filename(v);
        new MultiPurposeDirAccess(workspace.dao()).save(dn, v,
                new CommitMessage("Neuer Schlüsseltyp " + v.getKey()), workspace);
    }

    public void save(Values v) {
        String dn = filename(v);
        new MultiPurposeDirAccess(workspace.dao()).save(dn, v,
                new CommitMessage("Schlüsseltyp " + v.getKey()), workspace);
    }
    
    public Values find(String key) {
        for (Values v : this) {
            if (v.getKey().equals(key)) {
                return v;
            }
        }
        return null;
    }

    public void delete(String key) {
        for (Values v : this) {
            if (v.getKey().equals(key)) {
                Set<String> filenames = new HashSet<>();
                filenames.add(filename(v));
                workspace.dao().deleteFiles(filenames, new CommitMessage("Lösche Schlüsseltyp: " + key), workspace,
                        new ArrayList<>());
                remove(v);
                break;
            }
        }
    }
    
    private String filename(Values v) {
        return workspace.getFolder() + "/" + DN_PREFIX + v.getKey() + ".json";
    }
}
