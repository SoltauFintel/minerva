package minerva.export.pdf;

public class Chapter {
    // immutable class!
    private final String prefix;
    private final int lastNumber;
    private final int layer;
    
    /** book level (layer 0) */
    public Chapter() {
        this("", 0, 0);
    }
    
    public static Chapter withoutChapters() {
        return new Chapter("", -1, 0);
    }
    
    private Chapter(String prefix, int layer, int lastNumber) {
        this.prefix = prefix;
        this.lastNumber = lastNumber;
        this.layer = layer;
    }

    /**
     * create chapter sibling (same layer, +1)
     */
    public Chapter inc() {
        if (layer < 0) {
            return this; // The without-chapters-mode doesn't return new chapters.
        }
        return new Chapter(prefix, layer, lastNumber + 1);
    }
    
    /**
     * create child chapter (1 layer down, starting with 1)
     */
    public Chapter child() {
        if (layer < 0) {
            return this; // The without-chapters-mode doesn't return new chapters.
        }
        return new Chapter(text(), layer + 1, 1);
    }

    /**
     * @return top level layer is 1
     */
    public int getLayer() {
        return layer;
    }

    /**
     * @return full chapter number, no dot at end
     */
    private String text() {
        return prefix + (lastNumber > 0 ? (prefix.isEmpty() ? "" : ".") + lastNumber : "");
    }
    
    /**
     * @return full chapter number, layer 1 has dot at end
     */
    @Override
    public String toString() {
        String ret = text();
        if (!ret.isEmpty()) {
            ret += (prefix.isEmpty() ? "." : "");
        }
        return ret;
    }
}
