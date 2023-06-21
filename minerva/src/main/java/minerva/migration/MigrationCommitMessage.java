package minerva.migration;

import minerva.git.CommitMessage;

public class MigrationCommitMessage extends CommitMessage {

    public MigrationCommitMessage(String msg) {
        super("(Migration) " + msg);
    }
}
