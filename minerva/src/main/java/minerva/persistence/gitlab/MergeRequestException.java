package minerva.persistence.gitlab;

import static github.soltaufintel.amalia.web.action.Escaper.esc;

import minerva.MinervaWebapp;
import minerva.base.ErrorMessageHolder;
import minerva.base.NLS;

public class MergeRequestException extends RuntimeException implements ErrorMessageHolder {
    private final Long id;
    private final String targetBranch;
    private final String lang;
    
    public MergeRequestException(Long mergeRequestId, String targetBranch, String userGuiLanguage) {
        super("Merge Request " + mergeRequestId + " can not be merged!");
        id = mergeRequestId;
        this.targetBranch = targetBranch;
        lang = userGuiLanguage;
    }

    @Override
    public String getErrorMessage() {
        return NLS.get(lang, "mergeRequestCantBeMerged")
                .replace("$url", MinervaWebapp.factory().getGitlabRepository().getProjectUrl() + "/-/merge_requests/" + id)
                .replace("$id", "" + id)
                .replace("$tb", esc(targetBranch));
    }
}
