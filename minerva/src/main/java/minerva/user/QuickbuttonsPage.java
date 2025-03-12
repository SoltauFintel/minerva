package minerva.user;

import github.soltaufintel.amalia.web.table.TableComponent;
import github.soltaufintel.amalia.web.table.TableComponent.Col;
import github.soltaufintel.amalia.web.table.TableComponent.Cols;

public class QuickbuttonsPage extends UPage {

	@Override
	protected void execute() {
		header("Schnell Button Leiste");
		Cols cols = Cols.of(new Col("Label", "{{i.label}}"),
				new Col("", "<a href=\"/q/delete?i={{i.nr}}\" class=\"btn btn-danger btn-xs\" onclick=\"return confirm('Sicher?');\">"
						+ "<i class=\"fa fa-trash-o\"></i></a>"
						+ " <a href=\"/q/move?i={{i.nr}}&d=-1\" class=\"btn btn-default btn-xs\"><i class=\"fa fa-arrow-up\"></i></a>"
						+ " <a href=\"/q/move?i={{i.nr}}&d=1\" class=\"btn btn-default btn-xs\"><i class=\"fa fa-arrow-down\"></i></a>"));
		put("table1", new TableComponent("wauto", cols, model, "quickbuttons"));
	}
}
