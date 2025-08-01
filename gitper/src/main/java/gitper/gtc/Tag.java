package gitper.gtc;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.pmw.tinylog.Logger;

public class Tag {
	private final String name;
	private final String sort;
	private String commitDate;
	private boolean used = false;
	
	public Tag(Ref tag, Git git) {
		name = Repository.shortenRefName(tag.getName());
		sort = makeSort();
        try (RevWalk walk = new RevWalk(git.getRepository())) {
            RevCommit c = walk.parseCommit(tag.getObjectId());
            BCommit commit = new BCommitBuilder().build(c, null);
            commitDate = commit.getCommitDate();
        } catch (Exception e) {
			Logger.error(e.getClass().getSimpleName() + " in Tag class [" + name + "]: " + e.getMessage());
		}
	}

	/** master branch */
	public Tag() {
		name = "master";
		sort = " ";
		commitDate = "";
	}
	
	private Tag(String name) {
		this.name = name;
		sort = makeSort();
		commitDate = "";
	}
	
	public static Tag create4Test(String name) {
		return new Tag(name);
	}
	
	private String makeSort() {
		String n = name.replace(".x", "").replace("root_", "");
		for (int i = 0; i < n.length(); i++) {
			char c = n.charAt(i);
			if (!((c >= '0' && c <= '9') || c == '.')) {
				return name;
			}
		}
		String ret = "";
		String w[] = n.split("\\.");
		for (int i = 0; i < w.length; i++) {
			ret += num4(w[i]) + ".";
		}
		return ret;
	}

	private String num4(String n) {
		String ret = "" + Integer.parseInt(n);
		while (ret.length() < 4) {
			ret = "0" + ret;
		}
		return ret;
	}

	public String getName() {
		return name;
	}
	
	public String getNameWithoutRoot() {
		if (name.startsWith("root_")) {
			return name.substring("root_".length());
		}
		return name;
	}
	
	public boolean isNumericName() {
		String n = getNameWithoutRoot();
		return n.charAt(0) >= '1' && n.charAt(0) <= '9';
	}
	
	public int dots() {
		return _dots(name);
	}
	
	public static int _dots(String x) {
		if (x.startsWith("root_")) {
			x = x.substring("root_".length());
		}
		int dots = 0;
		if (x.endsWith(".x")) {
			x = x.substring(0, x.length() - ".x".length());
			dots = 1;
		}
		for (int i = 0; i < x.length(); i++) {
			char c = x.charAt(i);
			if (c == '.') {
				dots++;
			} else if (c < '0' || c > '9') {
				return 0;
			}
		}
		return dots;
	}
	
	public String sort() {
		return sort;
	}

    public String getCommitDate() {
        return commitDate;
    }

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}
}
