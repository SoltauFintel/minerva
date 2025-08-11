package minerva.user.quickbuttons;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;
import com.github.template72.data.IDataList;

import github.soltaufintel.amalia.web.table.Col;
import github.soltaufintel.amalia.web.table.Cols;
import github.soltaufintel.amalia.web.table.TableComponent;
import gitper.base.StringService;
import minerva.base.UserMessage;
import minerva.base.WebpageTitleService;
import minerva.user.UPage;

public class QuickbuttonsPage extends UPage {
	public static ChangeLink changeLink = link -> link;

	@Override
	protected void execute() {
		if (isPOST()) {
			save();
		} else {
			display();
		}
	}

	private void display() {
		header(n("schnellButtonLeiste"));
		Cols cols = Cols.of(
				new Col("<i class=\"fa fa-fw\"></i> " + n("label"), "<i class=\"fa fa-fw {{i.icon}}\"></i> {{i.label}}"),
				new Col("", ""
						+ " <a href=\"/q/edit?id={{i.id}}\" class=\"btn btn-default btn-xs\">"
						+ "<i class=\"fa fa-pencil\"></i></a>"

						+ " <a href=\"/q/only-me?id={{i.id}}\" class=\"btn btn-default btn-xs\" title=\"{{if i.onlyMe}}"
						+ n("qCantSee") + "{{else}}" + n("qOthersCanTake") + "{{/if}}\">"
						+ "<i class=\"fa fa-fw {{if i.onlyMe}}fa-lock error{{else}}fa-unlock{{/if}}\"></i></a>"

						+ " <a href=\"/q/delete?id={{i.id}}\" class=\"btn btn-danger btn-xs\" title=\"Löschen\""
						+ " onclick=\"return confirm('" + n("delete") + "?');\"><i class=\"fa fa-trash-o\"></i></a>"));
		if (((IDataList) model.get("quickbuttons")).isEmpty()) {
			put("table1", "");
		} else {
			var table1 = new TableComponent("wauto", cols, model, "quickbuttons") {
				@Override
				public void saveSortedRows(List<String> newOrder, Map<String, Integer> indexMap) {
					user.getUser().getQuickbuttons().sort(Comparator.comparingInt(i -> indexMap.getOrDefault(i.getId(), -1)));
					user.saveQuickbuttons();
				}
			};
			put("table1", table1.withRowDragDrop());
			put("sortableJS", true);
		}
		
		DataList list = list("other");
		for (Quickbutton qb : user.getQuickbuttonsFromOtherUsers()) {
			DataMap map = list.add();
			map.put("link", "/q/take?p=" + u(qb.getLink()) + "&t=" + u(qb.getLabel()));
			map.put("label", esc(qb.getLabel()));
		}
		put("haveOtherButtons", !list.isEmpty());
	}
	
	private void save() {
		String link = ctx.formParam("qburl");
		String label = ctx.formParam("qbtitle");
		
		if (StringService.isNullOrEmpty(link)) {
			throw new UserMessage("pleaseEnterURL", user);
		}
		link = link.trim();
		link = changeLink.changeLink(link); // Enter ticket number as link and transform to a valid ticket system URL.
		final var jira = "atlassian.net/browse/";
		int o = link.indexOf(jira);
		if (o >= 0) {
			label = link.substring(o + jira.length());
		}
		boolean empty = StringService.isNullOrEmpty(label);
		if (empty) {
			label = link.replace("https://", "").replace("http://", ""); // temporary label
		}
		Quickbutton qb = user.addQuickbutton(label, link);
		Logger.info(user.getLogin() + " | add URL based quick button \"" + qb.getLabel() + "\", " + qb.getLink());
		if (empty) {
			new Thread(() -> {
				String label2 = WebpageTitleService.webpageTitleService.getTitle(qb.getLink()) // expensive
						.replace("https://", "").replace("http://", "");
				user.getUser().getQuickbuttons().forEach(i -> {
					if (i.getLink().equals(qb.getLink())) {
						i.setLabel(label2);
						Logger.info(user.getLogin() + " | saved label: \"" + label2 + "\"");
					}
				});
				user.saveQuickbuttons();
			}).start();
		}
		
		ctx.redirect("/q/config");
	}
	
	public interface ChangeLink {
		
		String changeLink(String link);
	}
}
