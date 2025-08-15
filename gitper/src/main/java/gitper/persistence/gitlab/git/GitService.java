package gitper.persistence.gitlab.git;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.pmw.tinylog.Logger;

import gitper.User;
import gitper.access.CommitMessage;
import gitper.base.ICommit;
import gitper.base.StringService;
import gitper.gtc.Repository;
import gitper.persistence.gitlab.GitFactory;

/**
 * Control Git repository with quite low level functions: clone, fetch, pull, tag, branch, commit, select branch/commit.
 */
public class GitService {
	private static final String HANDLE = "GitService";
    public static final String ADD_ALL_FILES = "$$ALL";
    private final File workspace;
    private final boolean onlyRemoteBranches;
    
    /**
     * only remote branches constructor
     * @param workspace the local directory for the Git repository and working tree
     */
    public GitService(File workspace) {
        this(workspace, true);
    }
    
    /**
     * @param workspace the local directory for the Git repository and working tree
     * @param onlyRemoteBranches true: methods like getBranches() deliver only remote branches,
     * false: methods like getBranches() deliver local and remote branches.
     * If you work in a local repo use false. If you cloned a remote repo use true.
     */
    public GitService(File workspace, boolean onlyRemoteBranches) {
        this.workspace = workspace;
        this.onlyRemoteBranches = onlyRemoteBranches;
    }

    /**
     * Git clone action
     * <p>This call may take several minutes.</p>
     * @param url URL of remote Git repository
     * <br>Please be careful that there is no leading or trailing whitespace within the arguments!
     * This may end in an "Illegal character in path" URI exception.
     * @param user user to log into remote Git repository
     * @param branch which branch should be active after clone, e.g. "master"
     * @param bare false: with repository and with workspace, true: with repository and without workspace
     */
    public void clone(String url, User user, String branch, boolean bare) {
    	synchronized (HANDLE) {
	        try (Git result = Git.cloneRepository()
	                .setURI(GitFactory.handleUrl(url, user))
	                .setCredentialsProvider(GitFactory.getUsernamePasswordCredentialsProvider(user))
	                .setBranch(branch)
	                .setDirectory(workspace)
	                .setBare(bare)
	                .call()) {
	        } catch (Exception e) {
	            e.printStackTrace(); // Brauch ich für die Fehleranalyse. Ich habe den Verdacht, dass tinylog zu viel vom Stacktrace nicht ausgibt.
	            Logger.error("Error cloning Git repository! URL: " + url + " | user: " + user.getLogin()
	                + " | branch: " + branch);
	            throw new RuntimeException("Error accessing Git repository! Please try logout and login.", e);
	        }
    	}
    }
    
    /**
     * Git fetch action
     * @param user user to log into remote Git repository
     */
    public void fetch(User user) {
    	synchronized (HANDLE) {
	        try (Git git = Git.open(workspace)) {
	            git.fetch()
	                .setCredentialsProvider(GitFactory.getUsernamePasswordCredentialsProvider(user))
	                .call();
	        } catch (Exception e) {
	            throw new RuntimeException("Error fetching Git repository!", e);
	        }
    	}
    }

    /**
     * Git pull action
     * <p>Please call clearTags() before if you want to ensure that deleted tags on remote do not exist in local repo.</p>
     * @param user user to log into remote Git repository
     */
    public void pull(User user) {
    	synchronized (HANDLE) {
	        try (Git git = Git.open(workspace)) {
	            git.pull()
	                .setCredentialsProvider(GitFactory.getUsernamePasswordCredentialsProvider(user))
	                .call();
	        } catch (Exception e) {
	            throw new RuntimeException("Error pulling Git repository!", e);
	        }
    	}
    }

    /**
     * This call may take 4 seconds.
     * @return false if there is a change in the work tree, e.g. an added or changed file
     * <br>true if there is nothing to be committed (but maybe something to push)
     * <br>Of course any changes in gitignore folders are not detected (e.g. build folder).
     */
    public boolean isWorkspaceClean() {
        try (Git git = Git.open(workspace)) {
            return git.status().call().isClean();
        } catch (Exception e) {
            throw new RuntimeException("Error while detecting if workspace is clean!", e);
        }
    }

    /**
     * onlyRemoteBranches setting is used.
     * @return branch name list
     */
    public List<String> getBranchNames() {
        final String LOCAL = "refs/heads/";
        final String REMOTE = "refs/remotes/origin/";
        try (Git git = Git.open(workspace)) {
            return git.branchList()
                    .setListMode(onlyRemoteBranches ? ListMode.REMOTE : ListMode.ALL)
                    .call()
                    .stream()
                    .filter(ref -> !"HEAD".equals(ref.getName()))
                    .map(ref -> ref.getName().replace(LOCAL, "").replace(REMOTE, ""))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error loading branches!", e);
        }
    }

    /**
     * Creates branch on current commit.
     * This method creates no root tag!
     * See other branch() method if you also want to push the commit.
     * @param name branch name, e.g. "3.21.x"
     */
    public void branch(String name) {
        branch(name, null, null);
    }

    /**
     * Creates branch and pushs it.
     * This method creates no root tag!
     * @param name branch name, e.g. "3.21.x"
     * @param commit null: current commit, otherwise commit hash or tag name
     * @param user user to log into remote Git repository, null: don't push
     */
    public void branch(String name, String commit, User user) {
        if (StringService.isNullOrEmpty(name)) {
            throw new RuntimeException("Branch name is not valid!");
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '.' || c == '_'
                    || c == '-')) {
                throw new RuntimeException("Branch name is not valid!");
            }
        }
        // TO.DO Maybe there's an better implementation for this! branchCreate().setStartPoint?
        String m = null;
        if (commit != null) {
            m = getCurrentBranch();
            selectCommit(commit);
        }
        try {
            branch(name, user);
        } finally {
            if (commit != null) {
                switchToBranch(m);
            }
        }
    }
    
    private void branch(String name, User user) {
    	synchronized (HANDLE) {
	        String action = "creating";
	        try (Git git = Git.open(workspace)) {
	            // step 1: create
	            git.branchCreate()
	                .setName(name)
	                .call();
            
	            // step 2: push
	            if (user != null) {
	                try {
	                    action = "pushing";
	                    git.push()
	                        .setRemote("origin")
	                        .setRefSpecs(new RefSpec(name + ":" + name))
	                        .setCredentialsProvider(GitFactory.getUsernamePasswordCredentialsProvider(user))
	                        .call();
	                } catch (Exception up) {
	                    try {
	                        git.branchDelete()
	                            .setBranchNames(name)
	                            .call();
	                        Logger.warn("branch push error! -> compensation: local branch " + name + " deleted");
	                    } catch (Throwable ignore) { //
	                    }
	                    throw up;
	                }
	            }
	        } catch (RefAlreadyExistsException e) {
	            // This also happens if the user doubleclicks the submit button.
	            Logger.error(e);
	            throw new RuntimeException("Branch name already exists. Please choose another name.");
	        } catch (Exception e) {
	            throw new RuntimeException("Error " + action + " a branch!", e);
	        }
    	}
    }
    
    /**
     * checkout: Set HEAD to other commit
     * @param commit commit hash, can be short form
     * A tag name should also work.
     */
    public void selectCommit(String commit) {
    	synchronized (HANDLE) {
    		try (Git git = Git.open(workspace)) {
    			git.checkout()
    			.setName(commit)
    			.call();
    		} catch (Exception e) {
    			throw new IllegalArgumentException("Error selecting commit! " + e.getMessage(), e);
    		}
		}
    }

    /**
     * checkout branch
     * @param branch e.g. "3.03.x"
     */
    public void switchToBranch(String branch) {
    	synchronized (HANDLE) {
	        try (Git git = Git.open(workspace)) {
	            try {
	                // try for locally existing branch
	                git.checkout()
	                    .setName(branch)
	                    .call();
	            } catch (RefNotFoundException e) {
	                // create local branch from remote tracking branch
	                git.checkout()
	                    .setName(branch)
	                    .setCreateBranch(true)
	                    .call();
	            }            
	        } catch (Exception e) {
	            throw new RuntimeException("Error while switching branch to " + branch, e);
	        }
    	}
    }

    /**
     * @return hash of current commit (HEAD), e.g. "f65bb8c600a3ea1eabdbdcad1f6bd381f00636b6"
     */
    public String getCurrentCommitHash() {
        try (Git git = Git.open(workspace)) {
            Iterator<RevCommit> iter = git.log().setMaxCount(1).call().iterator();
            return iter.hasNext() ? iter.next().getName() : null;
        } catch (Exception e) {
            throw new RuntimeException("Error getting current commit hash!", e);
        }
    }
    
    /**
     * @return e.g. "master"
     */
    public String getCurrentBranch() {
        try (Git git = Git.open(workspace)) {
            return git.getRepository().getBranch();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Add all changes, commit and push them.
     * @param commitMessage not null
     * @param authorName also committer name
     * @param mail email address of author/committer
     * @param user user to log into remote Git repository, null: don't push
     * @param password password to log into remote Git repository
     * @param addFilenames files to change or that are new
     * @param removeFilenames files to delete
     * @return commit hash of newly created commit
     */
    public String commit(CommitMessage commitMessage, String authorName, String mail, User user,
            Set<String> addFilenames, Set<String> removeFilenames) {
        if (commitMessage == null) {
            throw new IllegalArgumentException("commitMessage must not be null!");
        }
        if (authorName == null || authorName.trim().isEmpty()) {
            throw new IllegalArgumentException("authorName must not be empty!");
        }
        if (mail == null || mail.trim().isEmpty()) {
            throw new IllegalArgumentException("mail must not be empty!");
        }
        synchronized (HANDLE) {
	        try (Git git = Git.open(workspace)) {
	        	boolean allowEmpty = false;
	            if (addFilenames.size() == 1 && addFilenames.contains(ADD_ALL_FILES)) {
	                git.add().addFilepattern(".").call();
	            } else {
	                if (!addFilenames.isEmpty()) {
	                    AddCommand add = git.add();
	                    addFilenames.forEach(filename -> add.addFilepattern(filename));
	                    add.call();
	                }
	                if (!removeFilenames.isEmpty()) {
	                    RmCommand rm = git.rm();
	                    removeFilenames.forEach(filename -> rm.addFilepattern(filename));
	                    rm.call();
	                    allowEmpty = true;
	                }
	            }
	            RevCommit commit = git.commit()
	                .setMessage(commitMessage.toString())
	                .setAuthor(authorName, mail)
	                .setCommitter(authorName, mail)
	                .setAllowEmpty(allowEmpty)
	                .call();
	            if (user != null) {
	                git.push()
	                    .setCredentialsProvider(GitFactory.getUsernamePasswordCredentialsProvider(user))
	                    .call();
	            }
	            return commit.getName();
	        } catch (EmptyCommitException e) {
	            throw new MinervaEmptyCommitException(e.getMessage());
	        } catch (Exception e) {
	            Logger.error(e); // Das muss leider so, damit keine Info verloren geht.
	            throw new RuntimeException("Error committing changes! (See log.) Try to logout and login.");
	        }
        }
    }

    public List<ICommit> getFileHistory(String file, boolean followRenames) {
        try (Git git = Git.open(workspace)) {
            Iterable<RevCommit> commits;
            if (followRenames) {
                commits = new LogFollowCommand(git.getRepository(), file).call(); // expensive
            } else {
                commits = git.log().addPath(file).call();
            }
            List<ICommit> ret = new ArrayList<>();
            for (RevCommit commit : commits) {
                //if (commit.getParentCount() == 1) {      auskommentiert damit Erst-Commit sichtbar wird
                    ret.add(new HCommit(commit));
                //}
            }
            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<ICommit> getHtmlChangesHistory(int start, int size) {
        try (Git git = Git.open(workspace)) {
        	final var repository = git.getRepository();
            Iterable<RevCommit> commits = git.log().setSkip(start).setMaxCount(size).call();
            List<ICommit> ret = new ArrayList<>();
            for (RevCommit commit : commits) {
                if (commit.getParentCount() == 1) {
                    if (!commit.getShortMessage().startsWith("(Migration)")) {
                        HCommit hc = new HCommit(commit);
                        var changes = Repository.loadFileChanges(commit, repository);
                        if (changes != null) {
                        	hc.setFiles(changes.changes().stream()
                        			.map(i -> i.path())
                        			.filter(dn -> dn.endsWith(".html"))
                        			.collect(Collectors.toList()));
                        } else {
                        	hc.setFiles(List.of());
                        }
                        ret.add(hc);
                    }
                }
            }
            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public boolean areThereRemoteUpdates(String targetBranch, User user) {
    	synchronized (HANDLE) {
	        try (Git git = Git.open(workspace)) {
	            FetchResult f = git.fetch()
	                    .setCredentialsProvider(GitFactory.getUsernamePasswordCredentialsProvider(user))
	                    .call();
	            long n = f.getTrackingRefUpdates().stream().filter(i -> i.getRemoteName().endsWith("/" + targetBranch)).count();
	            return n > 0;
	        } catch (Exception e) {
	            Logger.error(e);
	            return true;
	        }
    	}
    }
}
