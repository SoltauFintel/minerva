package minerva.model;

import minerva.exclusions.ExclusionsService;

public class SeiteVisible {
    /** see hasContent and isSeiteVisible */
    private final int c;
    private final ExclusionsService exclusionsService;

    public SeiteVisible(int c, ExclusionsService exclusionsService) {
        this.c = c;
        this.exclusionsService = exclusionsService;
    }
    
    public boolean isVisible() {
        return c > 0;
    }
    
    public boolean isEmpty() {
        return c == 0;
    }
    
    public boolean hasSubpages() {
        return c == 2;
    }
    
    public boolean isInvisible() {
        return c < 0;
    }

    public ExclusionsService getExclusionsService() {
        return exclusionsService;
    }
}
