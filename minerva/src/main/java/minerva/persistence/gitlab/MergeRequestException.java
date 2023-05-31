package minerva.persistence.gitlab;

public class MergeRequestException extends RuntimeException {

    public MergeRequestException(String message) {
        super(message);
    }
}
