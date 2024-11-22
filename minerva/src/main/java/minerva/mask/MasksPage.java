package minerva.mask;

import java.util.List;
import java.util.stream.Collectors;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.workspace.WPage;

public class MasksPage extends WPage {

    @Override
    protected void execute() {
        List<Mask> masks = new MasksService(workspace).getMasks();
        
        header(n("masks"));
        DataList list = list("masks");
        for (Mask mask : masks) {
            DataMap map = list.add();
            map.put("tag", mask.getTag());
            map.put("fields", mask.getFields().stream().map(i -> i.getLabel()).collect(Collectors.joining(", ")));
            map.putSize("n", mask.getFields());
        }
        putSize("n", masks);
        put("empty", list.isEmpty());
        masksMenu(model, branch, n("masks"));
    }
    
    public static void masksMenu(DataMap model, String branch, String title) {
        model.put("hasBook", false);
        model.put("hasMenuItems", true);
        DataList list2 = model.list("menuItems");
        DataMap map = list2.add();
        map.put("link", "/mask/" + branch);
        map.put("title", title);
    }
}
