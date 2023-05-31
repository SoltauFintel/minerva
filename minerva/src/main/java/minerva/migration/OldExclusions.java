package minerva.migration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minerva.base.StringService;

/**
 * Use: setCustomer() and call contains() for every label to be checked for
 */
public class OldExclusions {
    private static final OldExclusions INSTANCE = create();
    /** key: customer, value: tag list */
    private final Map<String, List<String>> customers = new HashMap<>();
    
    public static OldExclusions getInstance() {
        return INSTANCE;
    }
    
    private static OldExclusions create() {
        Path path = Paths.get("exclusions/exclusions.txt");
        try {
            return new OldExclusions(Files.newInputStream(path));
        } catch (IOException e) {
            throw new RuntimeException("Can not read file: " + path.toFile().getAbsolutePath() + "\n" + e.getMessage(), e);
        }
    }
    
    /**
     * @param exclusions exclusions text file with customer names in [...] and tags for exclusions in separate lines
     */
    public OldExclusions(InputStream exclusions) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exclusions))) {
            List<String> exList = null;
            String line;
            while ((line = reader.readLine()) != null) {
                int o = line.indexOf("//");
                if (o >= 0) {
                    line = line.substring(0, o);
                }
                line = line.trim();
                if (line.startsWith("[") && line.endsWith("]")) {
                    exList = new ArrayList<>();
                    customers.put(line.substring(1, line.length() - 1).trim().toLowerCase(), exList);
                } else if (!line.isEmpty()) {
                    exList.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public enum LabelClass {
        /** normal exclusion label */
        NORMAL,
        /** negative list. if this exclusion label occurs then always return "page is not accessible" */
        OFF,
        /** positive list. if this label occurs then override NORMAL LabelClass but not OFF LabelClass */
        ON,
        /** does not contain */
        NOT_IN;
    }
    
    public LabelClass contains(String tag, String customer) {
        if (StringService.isNullOrEmpty(customer)) {
            return LabelClass.NORMAL; // wrong customer -> don't deliver help page!
        }
        List<String> list = customers.get(customer.toLowerCase());
        if (list != null) {
            for (String i : list) {
                if (i.equalsIgnoreCase("+" + tag)) {
                    return LabelClass.ON;
                } else if (i.equalsIgnoreCase("-" + tag)) {
                    return LabelClass.OFF;
                } else if (i.equalsIgnoreCase(tag)) {
                    return LabelClass.NORMAL;
                }
            }
        }
        return LabelClass.NOT_IN;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Map.Entry<String, List<String>> e : customers.entrySet()) {
            s.append("\r\n");
            s.append(e.getKey());
            s.append(":\r\n");
            for (String i : e.getValue()) {
                s.append("- ");
                s.append(i);
                s.append("\r\n");
            }
        }
        return s.toString();
    }
}
