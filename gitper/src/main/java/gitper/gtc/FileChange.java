package gitper.gtc;

import org.eclipse.jgit.diff.DiffEntry;

public record FileChange(String path, DiffEntry.ChangeType changeType) {
}
