package gitper.gtc;

import java.util.List;

import gitper.base.ICommit;
import gitper.base.StringService;

public class BCommit implements ICommit {
    private String id;
    private String shortMessage;
    private String autor;
    private String autorInitialien;
    private String authoredDate;
    private String committerInitialien;
    private String commitDate;
    /** changes ID */
    private String cid;
    private List<String> files;

    public BCommit() {}
    
    public String getShortMessage() {
        return shortMessage;
    }

    public void setShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
    }

    public String getAutorInitialien() {
        return autorInitialien;
    }

    public void setAutorInitialien(String autorInitialien) {
        this.autorInitialien = autorInitialien;
    }

    public String getAuthoredDate() {
        return authoredDate;
    }

    public void setAuthoredDate(String authoredDate) {
        this.authoredDate = authoredDate;
    }

    public String getCommitterInitialien() {
        return committerInitialien;
    }

    public void setCommitterInitialien(String committerInitialien) {
        this.committerInitialien = committerInitialien;
    }

    public String getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(String commitDate) {
        this.commitDate = commitDate;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    @Override
    public String getHash() {
        return id;
    }

    @Override
    public String getHash7() {
        return StringService.seven(id);
    }

    @Override
    public String getMessage() {
        return getShortMessage();
    }

    @Override
    public String getAuthor() {
        return autor;
    }

    @Override
    public String getCommitDateTime() {
        return getCommitDate().substring(0, "2025-08-15 08:00".length()); // without seconds
    }

    @Override
    public List<String> getFiles() {
        return files;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}
