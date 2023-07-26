package minerva.persistence.gitlab.git;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import minerva.base.StringService;

public class HCommit {
    private final RevCommit c;
    private List<String> files;
    
    HCommit(RevCommit c) {
        this.c = c;
    }
    
    public String getMessage() {
        return c.getShortMessage();
    }
    
    public String getCommitDateTime() {
        return c.getCommitterIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    public String getHash() {
        return c.getId().getName();
    }

    public String getHash7() {
        String ret = getHash();
        if (!StringService.isNullOrEmpty(ret) && ret.length() > 7) {
            return ret.substring(0, 7);
        }
        return ret;
    }

    public String getAuthor() {
        return c.getAuthorIdent().getName();
    }
    
    // getNewPath, getChangeType
    public List<DiffEntry> loadFiles(Git git) {
        // https://github.com/centic9/jgit-cookbook/blob/master/src/main/java/org/dstadler/jgit/porcelain/DiffFilesInCommit.java
        try {
            return git.diff()
                    .setOldTree(prepareTreeParser(git.getRepository(), c.getParent(0).getId().getName()))
                    .setNewTree(prepareTreeParser(git.getRepository(), getHash()))
                    .call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(repository.resolve(objectId));
            RevTree tree = walk.parseTree(commit.getTree().getId());
            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }
            walk.dispose();
            return treeParser;
        }
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}
