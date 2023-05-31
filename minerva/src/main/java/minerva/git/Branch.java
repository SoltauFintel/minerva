package minerva.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

/**
 * Branch name and its HEAD commit. This class knows whether it is a local or a remote branch.
 * The HEAD commit is only loaded when required. 
 */
public class Branch implements IVersion {
    private static final String LOCAL = "refs/heads/";
    private static final String REMOTE = "refs/remotes/origin/";
    private final Ref ref;
    private final String name;
    private final Git git;
    /** last commit of the branch */
    private Commit head;
    
    /**
     * INTERNAL
     * @param ref branch
     * @param git -
     */
    public Branch(Ref ref, Git git) {
        this.ref = ref;
        name = ref.getName();
        this.git = git;
    }
    
    /**
     * INTERNAL
     * You should overwrite getHead().
     * @param name full branch name
     */
    public Branch(String name) {
        this.name = name;
        ref = null;
        git = null;
    }

    @Override
    public String getName() {
        return shorten(name);
    }
    
    public static String shorten(String pName) {
        return pName.replace(LOCAL, "").replace(REMOTE, "");
    }
    
    public String getFullName() {
        return name;
    }

    public Commit getHead() {
        if (head == null) {
            try (RevWalk walk = new RevWalk(git.getRepository())) {
                RevCommit commit = walk.parseCommit(ref.getObjectId());
                head = new Commit(commit);
            } catch (Exception e) {
                throw new RuntimeException("Error getting HEAD commit for branch!\n" + getFullName(), e);
            }
        }
        return head;
    }
    
    @Override
    public String toString() {
        return "branch " + getName() + (getHead() == null ? "" : " (HEAD " + getHead() + ")");
    }
}
