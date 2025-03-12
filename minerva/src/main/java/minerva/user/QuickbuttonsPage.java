package minerva.user;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.table.TableComponent;
import github.soltaufintel.amalia.web.table.TableComponent.Col;
import github.soltaufintel.amalia.web.table.TableComponent.Cols;

public class QuickbuttonsPage extends UPage {
	// TODO Web URL's hinzufügen (wobei man Quickbuttons als privat kennzeichnen kann)

	@Override
	protected void execute() {
		header(n("schnellButtonLeiste"));
		Cols cols = Cols.of(
				new Col("<i class=\"fa fa-fw\"></i> " + n("label"), "<i class=\"fa fa-fw {{i.icon}}\"></i> {{i.label}}"),
				new Col("", ""
						+ " <a href=\"/q/move?i={{i.nr}}&d=-1\" class=\"btn btn-default btn-xs{{if i.disabled1}} disabled{{/if}}\">"
						+ "<i class=\"fa fa-arrow-up\"></i></a>"
						
						+ " <a href=\"/q/move?i={{i.nr}}&d=1\" class=\"btn btn-default btn-xs{{if i.disabled2}} disabled{{/if}}\">"
						+ "<i class=\"fa fa-arrow-down\"></i></a>"

						+ " <a href=\"/q/delete?i={{i.nr}}\" class=\"btn btn-danger btn-xs\" onclick=\"return confirm('Sicher?');\">"
						+ "<i class=\"fa fa-trash-o\"></i></a>"
						));
		put("table1", new TableComponent("wauto", cols, model, "quickbuttons"));
		
		DataList list = list("other");
		for (Quickbutton qb : user.getQuickbuttonsFromOtherUsers()) {
			DataMap map = list.add();
			map.put("link", "/q/take?p=" + u(qb.getLink()) + "&t=" + u(qb.getLabel()));
			map.put("label", esc(qb.getLabel()));
		}
		put("haveOtherButtons", !list.isEmpty());
	}
}
