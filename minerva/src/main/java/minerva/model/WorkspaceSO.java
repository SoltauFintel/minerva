package minerva.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import gitper.User;
import gitper.Workspace;
import gitper.access.CommitHash;
import gitper.access.CommitMessage;
import gitper.access.DirAccess;
import minerva.MinervaWebapp;
import minerva.config.MinervaOptions;
import minerva.exclusions.Exclusions;
import minerva.persistence.filesystem.FileSystemDirAccess;
import minerva.seite.tag.TagNList;
import minerva.task.TaskService;

public class WorkspaceSO implements Workspace {
    private final UserSO user;
    private final String folder;
    private final String branch;
    private BooksSO books;
    private Boolean ok = null;
    private String userMessage;
    private Exclusions exclusions;
    
    public WorkspaceSO(UserSO user, String parentFolder, String branch) {
        this.user = user;
        this.folder = parentFolder + "/" + branch;
        this.branch = branch;
    }

    public UserSO getUser() {
        return user;
    }
    
    @Override
    public User user() {
    	return user.getUser();
    }

    @Override
    public String getFolder() {
        return folder;
    }

    @Override
    public String getBranch() {
        return branch;
    }

    @Override
    public DirAccess dao() {
        if (user.getUser().getDelayedPush().contains(branch)) {
            return new FileSystemDirAccess();
        } else {
            return user.dao();
        }
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
                if ("master".equals(branch)) {
                    TaskService.update(user);
                }
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
    
    public SeiteSO findPage(String id) {
        for (BookSO book : getBooks()) {
            SeiteSO seite = book._seiteById(id);
            if (seite != null) {
                return seite;
            }
        }
        return null;
    }

    public String getUserMessage() {
        return userMessage;
    }

    @Override
    public void pull() {
        pull(false);
    }
    
    public void pull(boolean forceClone) {
        dao().initWorkspace(this, forceClone);
        books = new BooksSO(this);
        info("User $l has updated workspace $b. Books: $n");
    }
    
    private void info(String text) {
        String login = user.getLogin();
		text = text
                .replace("$l", login)
                .replace("$b", branch)
                .replace("$n", "" + books.size());
		if (isAutomaticUser(login)) {
			Logger.debug(text);
		} else {
			Logger.info(text);
		}
    }
    
    public static void info(String login, String msg) {
		if (isAutomaticUser(login)) {
			Logger.debug(login + " | " + msg);
		} else {
			Logger.info(login + " | " + msg);
		}
	}

	private static boolean isAutomaticUser(String login) {
		return login != null && login.equalsIgnoreCase(MinervaOptions.CLEANUP_LOGIN.get());
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
		return new SearchSO(MinervaOptions.SEARCH_URL.get(), MinervaOptions.SEARCH_SITE_PREFIX.get(), this,
				MinervaWebapp.factory().getLanguages());
    }
    
    public ExclusionsSO getExclusions() {
        return new ExclusionsSO(this);
    }
    
    public Exclusions exclusions() {
		if (exclusions == null) {
			exclusions = new Exclusions(getExclusions().get());
		}
		return exclusions;
    }
    
    public void clearExclusionsCache() {
    	exclusions = null;
    }
    
    public void createBranch(String newBranch, String commit) {
        dao().createBranch(this, newBranch, commit);
    }

    @Override
    public void onPush() {
        StatesSO.onPush(user.getLogin(), branch);
    }
    
    public void onEditing(SeiteSO seite, boolean finished) {
        StatesSO.onEditing(user.getLogin(), branch, seite.getId(), finished);
    }
    
    public CommitHash getCommitHash() {
        return dao().getCommitHash(this);
    }

    public void save(CommitMessage commitMessage) {
        MinervaWebapp.factory().getBackendService().saveAll(commitMessage, this);
    }
    
    public PapierkorbSO getPapierkorb() {
        return new PapierkorbSO(this);
    }
}
