package minerva.model;

import java.util.Set;
import java.util.TreeSet;

import minerva.access.CommitMessage;
import minerva.access.MultiPurposeDirAccess;
import minerva.exclusions.Exclusions;

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
        access().save(workspace.getFolder() + "/" + DN, //
                exclusions == null ? "" : exclusions, //
                new CommitMessage(DN), //
                workspace);
        workspace.clearExclusionsCache();
    }
    
    private MultiPurposeDirAccess access() {
        return new MultiPurposeDirAccess(workspace.dao());
    }

    public TreeSet<String> getCustomers() {
        return new TreeSet<>(workspace.exclusions().getCustomers());
    }

    public TreeSet<String> getSuggestedTags(Set<String> tags) {
        TreeSet<String> ret = new TreeSet<>();
        Exclusions exclusions = workspace.exclusions();
        for (String customer : exclusions.getCustomers()) {
            for (String tag : exclusions.getTags(customer)) {
                if (tag.startsWith("+") || tag.startsWith("-")) {
                    tag = tag.substring(1);
                }
                ret.add(tag);
            }
        }
        ret.removeAll(tags);
        return ret;
    }
}
