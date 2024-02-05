package minerva.seite.note;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.base.Strings; // guava 21

public class Frequency2 {
    private final Map<String, FrequencyItem> items = new TreeMap<>();
    
    public void add(String key, String member) {
        FrequencyItem list = items.get(key);
        if (list == null) {
            list = new FrequencyItem(key);
            items.put(key, list);
        }
        list.getMembers().add(member);
    }
    
    public Set<String> keySet() {
        return items.keySet();
    }

    public Map<String, FrequencyItem> getItems() {
        return items;
    }

    public List<FrequencyItem> getList() {
        List<FrequencyItem> list = new ArrayList<>(items.values());
        list.sort((a,b) -> b.getMembers().size() - a.getMembers().size());
        return list;
    }
    
    public void print() {
        for (FrequencyItem i : getList()) {
            System.out.println(Strings.padStart("" + i.getMembers().size(), 6, ' ') + "x " + i.toString());
            for (int j = 0; j < 20 && j < i.getMembers().size(); j++) {
                System.out.println("\t    " + i.getMembers().get(j));
            }
        }
    }
    
    public static class FrequencyItem {
        private final String key;
        private final List<String> members = new ArrayList<>();
        
        private FrequencyItem(String key) {
            this.key = key;
        }

        public List<String> getMembers() {
            return members;
        }
        
        @Override
        public String toString() {
            return key;
        }
        
        @Override
        public boolean equals(Object obj) {
            return key.equals(((FrequencyItem) obj).key);
        }
        
        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }

    /** Test */
    public static void main(String[] args) {
        Frequency2 f = new Frequency2();
        f.add("A", "a-1");
        f.add("B", "_b-1");
        f.add("B", "_b-2");
        f.add("B", "_b-3");
        f.add("A", "a-2");
        f.print();
    }
}