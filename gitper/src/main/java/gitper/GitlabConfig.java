package gitper;

public interface GitlabConfig {
	
	String getGitlabProject();
	
	String getGitlabUrl();
	
	String getGitlabAppId();
	
	String getGitlabSecret();
	
	String getGitlabAuthCallback();
	
	String getGitlabCommitPath();
	
	String getGitlabMergeRequestPath();
}
