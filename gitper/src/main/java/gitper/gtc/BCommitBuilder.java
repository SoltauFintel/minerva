package gitper.gtc;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import github.soltaufintel.amalia.base.IdGenerator;

public class BCommitBuilder {

    public BCommit build(RevCommit commit, String changes) {
        if (commit == null) {
            return null;
        }
        BCommit ret = new BCommit();
        ret.setId(commit.getId().getName());
        ret.setShortMessage(commit.getShortMessage());
        ret.setAuthoredDate(getAuthoredDate(commit));
        ret.setAutor(commit.getAuthorIdent().getName());
        ret.setAutorInitialien(getAutorInitialien(commit));
        ret.setCommitDate(getCommitDate(commit));
        ret.setCommitterInitialien(getCommitterInitialien(commit));
        if (changes != null) {
            ret.setCid(IdGenerator.code6(changes));
        }
        return ret;
    }
    
    private String getAuthoredDate(RevCommit commit) {
        PersonIdent authorIdent = commit.getAuthorIdent();
        return d(authorIdent.getWhen().toInstant(), authorIdent);
    }

    public String getCommitDate(RevCommit commit) {
        Instant instant = Instant.ofEpochSecond(commit.getCommitTime());
        return d(instant, commit.getCommitterIdent());
    }

    private String d(Instant instant, PersonIdent ident) {
        ZoneId zoneId = ZoneId.of("Europe/Berlin");
        ZonedDateTime date = ZonedDateTime.ofInstant(instant, zoneId);
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String getAutorInitialien(RevCommit commit) {
        return initialien(commit.getAuthorIdent().getName());
    }

    private String getCommitterInitialien(RevCommit commit) {
        return initialien(commit.getCommitterIdent().getName());
    }

    private String initialien(String name) {
        try {
            String[] w = name.split(" ");
            return w[0].substring(0, 1) + w[1].substring(0, 1);
        } catch (Exception e) {
            return name;
        }
    }
}
