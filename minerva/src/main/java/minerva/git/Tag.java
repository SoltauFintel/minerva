package minerva.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.pmw.tinylog.Logger;

/**
 * Git tag name its commit.
 */
public class Tag implements IVersion {
    /** short name of Git tag */
    private String name;
    /** The commit which is marked by the tag. */
    private Commit commit;
    
    public Tag() {
    }

    /**
     * INTERNAL
     * @param tag -
     * @param git -
     */
    public Tag(Ref tag, Git git) {
        name = Repository.shortenRefName(tag.getName());
        try (RevWalk walk = new RevWalk(git.getRepository())) {
            RevCommit c = walk.parseCommit(tag.getObjectId());
            commit = new Commit(c);
        } catch (Exception e) {
            Logger.error("tag: " + name);
            throw new RuntimeException("Error retrieving commit for tag!", e);
        }
    }
    
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommitId() {
        return commit == null ? null : commit.getHash();
    }

    public Commit getCommit() {
        return commit;
    }

    public void setCommit(Commit commit) {
        this.commit = commit;
    }

    @Override
    public String toString() {
        return name;
    }
}
