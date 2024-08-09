package minerva.exclusions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.pmw.tinylog.Logger;

// TODO move to oh-html
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
                if (exList == null) {
                    Logger.error("Syntax error in exclusions. Ignoring line because a [section] is missing: " + line);
                } else {
                    exList.add(line);
                }
            }
        }
    }
    
    public TreeSet<String> getCustomers() {
        return new TreeSet<>(customers.keySet());
    }
    
    public List<String> getTags(String customer) {
        return customers.get(customer);
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
