package minerva.featuretree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import github.soltaufintel.amalia.web.action.Page;

// MindMap study
public class MindMapPage extends Page {
    private String json;

    @Override
    protected void execute() {
        List<MME> list = new ArrayList<>();
        list.add(new MME("cars", "type-b")); // root element (center of mind map)
        MME a, b, c, d, a_2, c_1, e, ee;
        list.add(a = new MME(1, null, "Audi"));
        list.add(b = new MME(2, null, "Lamborghini"));
        list.add(c = new MME(3, null, "Aston Martin"));
        list.add(new MME(5, null, "Peugeot"));
        list.add(new MME(6, null, "Subaru"));
        list.add(e = new MME(7, null, "Toyota"));
        list.add(new MME(8, null, "Porsche"));
        list.add(d = new MME(4, null, "Mercedes"));
        list.add(new MME(9, null, "Volkswagen"));

        list.add(new MME(41, d, "S/CL class", "type-c"));
        list.add(new MME(42, d, "SLK/SLC class", "type-c"));
        list.add(new MME(20, b, "Aventador", "type-c"));
        list.add(new MME(21, b, "Huracan", "type-c"));
        list.add(new MME(22, b, "Urus", "type-c"));
        list.add(c_1 = new MME(31, c, "DB class"));
        list.add(new MME(32, c, "Vantage", "type-c"));
        list.add(new MME(11, a, "A3 Sportback", "type-c"));
        list.add(a_2 = new MME(12, a, "A5", "type-c"));
        list.add(new MME(311, c_1, "DB12", "type-c"));
        list.add(new MME(312, c_1, "DBX707", "type-c"));
        list.add(new MME(121, a_2, "S5", "type-c"));
        list.add(new MME(511, e, "Corolla", "type-c"));
        list.add(new MME(512, e, "Camry", "type-c"));
        list.add(ee = new MME(513, e, "electric class"));
        list.add(new MME(5133, ee, "bZ4X", "type-c"));
        list.add(new MME(5132, ee, "Prius", "type-c"));
        list.add(new MME(5131, ee, "Mirai", "type-c"));

        json = "";
        
        add(list, i -> i.id == null, false);
        add(list, i -> i.id != null && i.parentId == null, true);
        
        System.out.println(json);
        put("data", json);
    }
    
    private void add(List<MME> list, Predicate<MME> func, boolean addSub) {
        for (MME i : list) {
            if (func.test(i)) {
                json += "{id: " + i.id + ", parentId: " + i.parentId + ", text: \"" + i.text + "\"" + (i.type == null ? "" : (", type: \"" + i.type + "\""))
                        + "},\n";
                if (addSub) {
                    add(list, j -> j.parentId == i.id, true);
                }
            }
        }
    }
    
    public static class MME {
        private Integer id;
        private Integer parentId;
        private String text;
        private String type;

        public MME(int id, MME parent, String text, String type) {
            this.id = Integer.valueOf(id);
            this.parentId = parent == null ? null : parent.id;
            this.text = text;
            this.type = type;
        }

        public MME(int id, MME parent, String text) {
            this(id, parent, text, null);
        }

        /** root */
        public MME(String text, String type) {
            id = null;
            parentId = null;
            this.text = text;
            this.type = type;
        }
    }
}
