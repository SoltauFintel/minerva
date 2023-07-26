package minerva.access;

import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.model.SeiteSO;

/**
 * I use a class for the commit message for better tracking its use in the sources.
 */
public class CommitMessage {
    private final String text;

    public CommitMessage(String commitMessage) {
        if (StringService.isNullOrEmpty(commitMessage)) {
            throw new IllegalArgumentException("commitMessage must not be empty!");
        }
        this.text = commitMessage;
    }

    /**
     * @param seite page for getting title (with 1st system language)
     * @param comment not null, can be empty
     */
    public CommitMessage(SeiteSO seite, String comment) {
        this(makeTitle(seite, comment));
    }
    
    private static String makeTitle(SeiteSO seite, String comment) {
        String title = seite.getSeite().getTitle().getString(MinervaWebapp.factory().getLanguages().get(0));
        if (title.equals(comment) || comment.isEmpty()) {
            return title;
        }
        return title + ": " + comment;
    }

    @Override
    public String toString() {
        return text;
    }
}
