package gitper.gtc;

import java.util.List;

public record GitFileChanges(BCommit commit, List<GitFileChange> changes) {
}
