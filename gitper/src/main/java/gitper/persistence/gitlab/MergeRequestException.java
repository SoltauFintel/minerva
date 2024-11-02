package gitper.persistence.gitlab;

import java.util.Map;

import gitper.base.ErrorMessageHolder;

public class MergeRequestException extends RuntimeException implements ErrorMessageHolder {
    private final Long id;
    private final String targetBranch;
    private final String mergeRequestPath;
    
    public MergeRequestException(Long mergeRequestId, String targetBranch, String mergeRequestPath) {
        super("Merge Request " + mergeRequestId + " can not be merged!");
        id = mergeRequestId;
        this.targetBranch = targetBranch;
        this.mergeRequestPath = mergeRequestPath;
    }
    
    public Long getId() {
    	return id;
    }
    
    public String getTargetBranch() {
    	return targetBranch;
    }

	@Override
	public String getKey() {
		return "mergeRequestCantBeMerged";
	}

	@Override
	public Map<String, String> getParameters() {
		return Map.of(
				"$url", mergeRequestPath + id,
				"$id", "" + id,
				"$tb", targetBranch
				);
	}
}
