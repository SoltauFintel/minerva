package gitper.persistence.gitlab.git;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import gitper.base.ICommit;
import gitper.base.StringService;

public class HCommit implements ICommit {
    private final RevCommit c;
    private List<String> files;
    
    HCommit(RevCommit c) {
        this.c = c;
    }
    
    @Override
    public String getMessage() {
        return c.getShortMessage();
    }
    
    @Override
    public String getCommitDateTime() {
        return c.getCommitterIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    @Override
    public String getHash() {
        return c.getId().getName();
    }

    @Override
    public String getHash7() {
        return StringService.seven(getHash());
    }

    @Override
    public String getAuthor() {
        return c.getAuthorIdent().getName();
    }
    
    @Override
    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}
