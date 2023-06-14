package minerva.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.config.AppConfig;
import minerva.MinervaWebapp;
import minerva.access.DirAccess;
import minerva.seite.tag.TagNList;

public class WorkspaceSO {
    private final UserSO user;
    private final String folder;
    private final String branch;
    private BooksSO books;
    private Boolean ok = null;
    
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
            // Late access on books. This is the first access onto the workspace. Pull Git repo!
            try {
                dao().initWorkspace(this, false);
                ok = Boolean.TRUE;
                books = new BooksSO(this);
                info("User $l accesses workspace $b for the first time. Books: $n");
            } catch (Exception e) {
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
        AppConfig config = new AppConfig();
        return new SearchSO(config.get("search.url"), config.get("search.site.prefix", "minerva-"),
                this, MinervaWebapp.factory().getLanguages());
    }
}
