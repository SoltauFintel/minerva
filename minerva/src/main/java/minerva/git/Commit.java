package minerva.git;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jgit.revwalk.RevCommit;

public class Commit {
    private final String id;
    private final ZonedDateTime commitDate;
    private final String author;
    private final String fullMessage;
    private final String shortMessage;

    public Commit(String id, ZonedDateTime commitDate, String author, String shortMessage, String fullMessage) {
        this.id = id;
        this.commitDate = commitDate;
        this.author = author;
        this.shortMessage = shortMessage;
        this.fullMessage = fullMessage;
    }

    /**
     * INTERNAL
     * @param c -
     */
    public Commit(RevCommit c) {
        id = c.getName();
        Instant commitInstant = Instant.ofEpochSecond(c.getCommitTime());
        ZoneId zoneId = c.getCommitterIdent().getTimeZone().toZoneId();
        commitDate = ZonedDateTime.ofInstant(commitInstant, zoneId);
        author = c.getAuthorIdent().getName();
        fullMessage = c.getFullMessage();
        shortMessage = c.getShortMessage();
    }

    public String getHash() {
        return id;
    }

    public ZonedDateTime getCommitDate() {
        return commitDate;
    }
    
    public String getCommitDateForDisplay() {
        return commitDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public String getCommitDateForSort() {
        return commitDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getCommitDateForManifest() {
        return commitDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss"));
    }

    public String getAuthor() {
        return author;
    }

    public String getFullMessage() {
        return fullMessage;
    }

    public String getShortMessage() {
        return shortMessage;
    }
    
    @Override
    public String toString() {
        return "commit " + id + ": \"" + shortMessage + "\"";
    }
}
