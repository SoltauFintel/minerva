package gitper.gtc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FileUtils;
import org.pmw.tinylog.Logger;

public class Repository {
	private static final String HANDLE = "HANDLE_R";
	private final RepositoryDefinition repo;
	private Git git;

	public Repository(RepositoryDefinition repo) {
		this.repo = repo;
	}

	public void pull() {
		if (repo.getLocalFolder().isDirectory()) {
			synchronized (HANDLE) {
				try {
					FetchCommand fetch = getGit().fetch();
					fetch.setCredentialsProvider(new UsernamePasswordCredentialsProvider(repo.getUser(), repo.getPassword()));
					fetch.call();
				} catch (GitAPIException e) {
					Logger.error("pull error: " + repo.getUrl() + " => " + repo.getLocalFolder().getAbsolutePath());
					Logger.error(e);
					throw new RuntimeException("Error pulling Git repository");
				}
			}
		} else {
			cloneRepo();
		}
	}

	public void cloneRepo() {
		close();
		synchronized (HANDLE) {
			try {
				if (repo.getLocalFolder().isDirectory()) {
					FileUtils.delete(repo.getLocalFolder(), FileUtils.RECURSIVE);
				}
				Files.createDirectory(repo.getLocalFolder().toPath());
	
				Logger.info("cloning Git repository " + repo.getUrl() + " => " + repo.getLocalFolder().getAbsolutePath());
				CloneCommand clone = Git.cloneRepository();
				clone.setDirectory(repo.getLocalFolder());
				clone.setURI(repo.getUrl());
				clone.setCredentialsProvider(new UsernamePasswordCredentialsProvider(repo.getUser(), repo.getPassword()));
				clone.setBare(true);
				clone.call();
				Logger.info("  clone ok");
			} catch (GitAPIException | IOException e) {
				Logger.error("clone error: " + repo.getUrl() + " => " + repo.getLocalFolder().getAbsolutePath());
				Logger.error(e);
				throw new RuntimeException("Error cloning Git repository");
			}
		}
	}

	public Git getGit() {
		if (git == null) {
			try {
				git = Git.open(repo.getLocalFolder());
			} catch (IOException e) {
				Logger.error("open Git error: " + repo.getUrl() + " => " + repo.getLocalFolder().getAbsolutePath());
				Logger.error(e);
				throw new RuntimeException("Error opening Git repository");
			}
		}
		return git;
	}
	
	public RevCommit loadCommit(String id) { // teuer
		ObjectId commitId = ObjectId.fromString(id);
		try (RevWalk revWalk = new RevWalk(getGit().getRepository())) {
			return revWalk.parseCommit(commitId);
		} catch (Exception e) {
			Logger.error("Error loading commit #" + id + ": " + e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}
	
	public BCommit loadBCommit(String commitId) {
		RevCommit commit = loadCommit(commitId);
		if (commit == null) {
			return null;
		}
		return new BCommitBuilder().build(commit, getChanges2(commit));
	}

	public List<Tag> getTags(String contains) {
		synchronized (HANDLE) {
			try {
				Git git = getGit();
				return git.tagList().call().stream()
						.filter(tag -> tag.getName().contains(contains))
						.map(tag -> new Tag(tag, git))
						.collect(Collectors.toList());
			} catch (GitAPIException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public String getBranchStartDate(String branch) {
		if (!"master".equals(branch)) {
			synchronized (HANDLE) {
				String x = "root_" + branch;
				try {
					Git git = getGit();
					List<Ref> tags = git.tagList().call();
					for (Ref ref : tags) {
						String name = org.eclipse.jgit.lib.Repository.shortenRefName(ref.getName());
						if (name.equals(x)) {
							try (RevWalk walk = new RevWalk(git.getRepository())) {
								RevCommit c = walk.parseCommit(ref.getObjectId());
								if (c != null) {
									return new BCommitBuilder().getCommitDate(c);
								}
							}
						}
					}
				} catch (Exception e) {
					Logger.error(e);
				}
			}
		}
        return "";
	}

	public String getChanges(String commitId) {
		return getChanges2(loadCommit(commitId));
	}

	private String getChanges2(RevCommit commit) { // teuer
		synchronized (HANDLE) {
			ByteArrayOutputStream boas = new ByteArrayOutputStream();
			try {
				if (commit.getParentCount() == 0) {
					return "first commit #" + commit.getId().getName();
				}
				RevCommit parent = commit.getParent(0);
				try (DiffFormatter diffFormatter = new DiffFormatter(boas)) {
					diffFormatter.setRepository(getGit().getRepository());
					for (DiffEntry entry : diffFormatter.scan(parent, commit)) {
						diffFormatter.format(diffFormatter.toFileHeader(entry));
					}
				}
				return new String(boas.toByteArray());
			} catch (Throwable e) {
				if (e instanceof OutOfMemoryError || e.getCause() instanceof OutOfMemoryError) {
					// 38553635 Bytes = 36 MB      XDEV-5823
					return "No changes because of OutOfMemoryError for commit #" + commit.getId().getName();
				}
				Logger.error("getChanges error for commit " + commit.getId().getName());
				Logger.error(e);
				return "No changes because of an " + e.getClass().getName() + " for commit #" + commit.getId().getName();
			}
		}
	}
	
    /**
	 * @return hash of current commit (HEAD), e.g. "f65bb8c600a3ea1eabdbdcad1f6bd381f00636b6"
	 */
	public String getCurrentCommitHash() {
		synchronized (HANDLE) {
			try {
				Iterator<RevCommit> iter = getGit().log().setMaxCount(1).call().iterator();
				return iter.hasNext() ? iter.next().getName() : "-";
			} catch (Exception e) {
				Logger.error(e);
				return "?";
			}
		}
	}

	public void close() {
		if (git != null) {
			try {
				git.close();
			} catch (Exception ignore) {
			}
			git = null;
		}
	}
}
