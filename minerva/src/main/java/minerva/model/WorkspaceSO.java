package minerva.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.access.CommitHash;
import minerva.access.DirAccess;
import minerva.config.MinervaConfig;
import minerva.config.MinervaFactory;
import minerva.seite.tag.TagNList;

public class WorkspaceSO {
    private final UserSO user;
    private final String folder;
    private final String branch;
    private BooksSO books;
    private Boolean ok = null;
    private String userMessage;
    
    public WorkspaceSO(UserSO user, String parentFolder, String branch) {
        this.user = user;
        this.folder = parentFolder + "/" + branch;
        this.branch = branch;
    }

    public UserSO getUser() {
        return user;
    }

    public String getFolder() {
        return folder;
    }

    public String getBranch() {
        return branch;
    }

    public DirAccess dao() {
        return user.dao();
    }

    public BooksSO getBooks() {
        if (books == null) {
            // Late access on books. This is the first access onto the workspace. If Git backend: Pull repo!
            userMessage = null;
            try {
                dao().initWorkspace(this, false);
                ok = Boolean.TRUE;
                books = new BooksSO(this);
                info("$l | $b | User accesses workspace for the first time. Books: $n");
            } catch (Exception e) {
                userMessage = e.getMessage();
                if (Boolean.FALSE.equals(ok)) {
                    Logger.error("Can't init workspace");
                } else {
                    Logger.error(e);
                    ok = Boolean.FALSE;
                }
            }
        }
        return books;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void pull() {
        pull(false);
    }
    
    public void pull(boolean forceClone) {
        dao().initWorkspace(this, forceClone);
        books = new BooksSO(this);
        info("User $l has updated workspace $b. Books: $n");
    }
    
    private void info(String text) {
        Logger.info(text
                .replace("$l", user.getUser().getLogin())
                .replace("$b", branch)
                .replace("$n", "" + books.size()));
    }

    public List<SeiteSO> findTag(String tag) {
        String x = TagsSO.cleanTag(tag);
        List<SeiteSO> ret = new ArrayList<>();
        for (BookSO book : books) {
            ret.addAll(book.findTag(x));
        }
        return ret.stream()
                .sorted((a, b) -> a.getSort().compareToIgnoreCase(b.getSort()))
                .collect(Collectors.toList());
    }

    public TagNList getAllTags() {
        TagNList tags = new TagNList();
        books.forEach(book -> book.addAllTags(tags));
        return tags;
    }

    public Boolean getOk() {
        return ok;
    }
    
    public SearchSO getSearch() {
        MinervaFactory fac = MinervaWebapp.factory();
        MinervaConfig cfg = fac.getConfig();
        return new SearchSO(cfg.getSearchUrl(), cfg.getSearchSitePrefix(), this, fac.getLanguages());
    }
    
    public ExclusionsSO getExclusions() {
        return new ExclusionsSO(this);
    }
    
    public void createBranch(String newBranch, String commit) {
        dao().createBranch(this, newBranch, commit);
    }

    public void onPush() {
        StatesSO.onPush(user.getLogin(), branch);
    }
    
    public void onEditing(SeiteSO seite, boolean finished) {
        StatesSO.onEditing(user.getLogin(), branch, seite.getId(), finished);
    }
    
    public CommitHash getCommitHash() {
        return dao().getCommitHash(this);
    }
}
