package minerva.git;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.eclipse.jgit.revwalk.RevCommit;

import minerva.base.StringService;

public class HCommit {
    private final RevCommit c;
    
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
}
