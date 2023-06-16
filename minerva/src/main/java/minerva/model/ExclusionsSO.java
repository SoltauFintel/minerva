package minerva.model;

import java.util.Set;
import java.util.TreeSet;

import minerva.access.MultiPurposeDirAccess;
import minerva.exclusions.Exclusions;
import minerva.exclusions.ExclusionsService;

public class ExclusionsSO {
    public static final String DN = "exclusions.txt";
    private final WorkspaceSO workspace;
    
    public ExclusionsSO(WorkspaceSO workspace) {
        this.workspace = workspace;
    }
    
    public String get() {
        String content = access().load(workspace.getFolder() + "/" + DN);
        return content == null ? "" : content;
    }
    
    public void set(String exclusions) {
        access().save(workspace.getFolder() + "/" + DN, exclusions == null ? "" : exclusions, DN, workspace);
    }
    
    private MultiPurposeDirAccess access() {
        return new MultiPurposeDirAccess(workspace.dao());
    }
    
    // Ist nicht so interessant.
    public TreeSet<String> getVisibleForCustomers(Set<String> tags) {
        TreeSet<String> ret = new TreeSet<>();
        Exclusions o = new Exclusions(get());
        ExclusionsService sv = new ExclusionsService();
        sv.setExclusions(o);
        sv.setTags(tags);
        for (String customer : o.getCustomers()) {
            sv.setCustomer(customer);
            if (sv.isAccessible()) {
                ret.add(customer);
            }
        }
        return ret;
    }

    public TreeSet<String> getCustomers() {
        return new TreeSet<>(new Exclusions(get()).getCustomers());
    }
}
