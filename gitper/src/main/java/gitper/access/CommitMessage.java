package gitper.access;

import gitper.base.StringService;

/**
 * I use a class for the commit message for better tracking its use in the sources.
 */
public class CommitMessage {
    private final String text;
    private boolean bigCommit = false;
    
    public CommitMessage(String commitMessage) {
        if (StringService.isNullOrEmpty(commitMessage)) {
            throw new IllegalArgumentException("commitMessage must not be empty!");
        }
        this.text = commitMessage;
    }

    /** many files */
    public void bigCommit() {
        bigCommit = true;
    }
    
    public boolean isBigCommit() {
        return bigCommit;
    }

    @Override
    public String toString() {
        return text;
    }
}
