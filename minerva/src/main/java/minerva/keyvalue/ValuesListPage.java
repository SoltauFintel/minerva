package minerva.keyvalue;

import java.util.stream.Collectors;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.workspace.WPage;

public class ValuesListPage extends WPage {

    @Override
    protected void execute() {
        header(n("kvmanagevalues"));
        DataList list = list("valuesList");
        ValuesSO so = new ValuesSO(workspace);
        for (Values v : so) {
            DataMap map = list.add();
            map.put("key", esc(v.getKey()));
            map.put("title", esc(v.getTitle()));
            map.put("values", esc(v.getValues().stream().limit(12).collect(Collectors.joining(", "))
                    + (v.getValues().size() > 12 ? ", ..." : "")));
            map.putInt("n", v.getValues().size());
        }
        put("empty", list.isEmpty());
        put("hasBook", false);
    }
}
