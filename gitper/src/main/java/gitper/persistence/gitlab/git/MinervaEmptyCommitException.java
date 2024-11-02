package gitper.persistence.gitlab.git;

public class MinervaEmptyCommitException extends RuntimeException {

    public MinervaEmptyCommitException(String msg) {
        super(msg);
    }
}
