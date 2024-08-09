package minerva.exclusions;

/**
 * has content: EMPTY_BUT_HAS_NONEMPTY_SUBPAGES or NOT_EMPTY or ERROR
 * has no content: EMPTY
 */
public enum HasContentEnum {

    EMPTY, // old number: 0
    
    EMPTY_BUT_HAS_NONEMPTY_SUBPAGES, // old number: 2
    
    NOT_EMPTY, // old number: 1
    
    /** should be treated as NOT_EMPTY */
    ERROR // old number: 3
    // TODO Prüfen, ob ERROR mode raus kann (zu gunsten NOT_EMPTY). Wird irgendwo ERROR genutzt?
}
