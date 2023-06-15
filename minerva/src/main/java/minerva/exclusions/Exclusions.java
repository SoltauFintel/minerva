package minerva.exclusions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import minerva.base.StringService;

public class Exclusions {
    /** key: customer, value: tag list */
    private final Map<String, List<String>> customers = new HashMap<>();
    
    /**
     * @param content exclusions text file content with customer names in [...]
     * and tags for exclusions in separate lines
     */
    public Exclusions(String content) {
        List<String> exList = null;
        for (String line : content.replace("\r\n", "\n").split("\n")) {
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
    }
    
    public Set<String> getCustomers() {
        return customers.keySet();
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
