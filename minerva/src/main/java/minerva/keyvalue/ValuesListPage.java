package minerva.keyvalue;

import java.util.List;
import java.util.stream.Collectors;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;
import com.google.common.base.Strings;

import github.soltaufintel.amalia.web.table.Col;
import github.soltaufintel.amalia.web.table.TableComponent;
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
            map.put("c3", Strings.padStart("" + v.getValues().size(), 4, '0'));
        }
        List<Col> cols = List.of(
                new Col(n("title"), "<a href=\"/values/{{branch}}/edit/{{i.key}}\">{{i.title}}</a>").sortable("title"),
                new Col(n("kvvalues"), "{{i.values}}").sortable("values"),
                new Col(n("anzahl"), "{{i.n}}").sortable("c3").right(),
                new Col(n("kvtype"), "{{i.key}}").sortable("key"),
                new Col("",
                """
                <a href="/values/{{branch}}/delete/{{i.key}}" onclick="return loeschen('{{i.title}}');"
                   class="btn btn-danger btn-xs" title="{{N.kvdeletekey}}"><i class="fa fa-trash-o"></i><i
                   id="wait_{{i.title}}" class="fa fa-delicious fa-spin" style="display: none;"></i></a>
                """).right());
        put("table1", new TableComponent(cols, model, "valuesList"));
        put("empty", list.isEmpty());
        put("hasBook", false);
    }
}
