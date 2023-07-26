package minerva.migration;

import minerva.access.CommitMessage;

public class MigrationCommitMessage extends CommitMessage {

    public MigrationCommitMessage(String msg) {
        super("(Migration) " + msg);
    }
}
