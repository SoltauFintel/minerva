package minerva.migration;

import gitper.access.CommitMessage;

public class MigrationCommitMessage extends CommitMessage {

    public MigrationCommitMessage(String msg) {
        super("(Migration) " + msg);
    }
}
