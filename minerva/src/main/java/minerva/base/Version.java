package minerva.base;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version {
    private static final Pattern pattern3 = Pattern.compile(".*[^0-9\\.]([1-9]\\.[0-9][0-9]\\.)([0-9][0-9]?)[^0-9\\.].*");
    private static final Pattern pattern4 = Pattern.compile(".*[^0-9\\.]([1-9]\\.[0-9][0-9]\\.)([0-9][0-9]?)\\.([0-9][0-9]?)[^0-9\\.].*");
    public static Version version = new Version();

    /**
     * @param text -
     * @return formatted version number if a 3 or 4 number version number is contained in text
     */
    public String version(final String text) {
        Matcher r = pattern4.matcher(" " + text + " "); // test pattern4 before pattern3!
        if (r.matches()) {
            String drei = r.group(2);
            while (drei.length() < 2) {
                drei = "0" + drei;
            }
            String vier = r.group(3);
            while (vier.length() < 2) {
                vier = "0" + vier;
            }
            return r.group(1) + drei + "." + vier;
        }
        r = pattern3.matcher(" " + text + " ");
        if (r.matches()) {
            String drei = r.group(2);
            while (drei.length() < 2) {
                drei = "0" + drei;
            }
            return r.group(1) + drei;
        }
        return text;
    }
}
