package minerva.migration;

import minerva.persistence.gitlab.git.CommitMessage;

public class MigrationCommitMessage extends CommitMessage {

    public MigrationCommitMessage(String msg) {
        super("(Migration) " + msg);
    }
}
