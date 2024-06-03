package minerva.export;

import minerva.MinervaWebapp;
import minerva.book.BAction;
import minerva.model.SeiteSO;

/**
 * Release Notes
 * 
 * temporäre Sonderaktion
 */
public class ExportCsvBookAction extends BAction {
	private String output = "";
	private String customer = "";
	private String version = "";

	@Override
	protected void execute() {
		for (String lang : MinervaWebapp.factory().getLanguages()) {
			for (SeiteSO kunde : book.getSeiten()) {
				String title = kunde.getSeite().getTitle().getString(lang);
				customer = lastWord(title) + ";" + lang;
				version = "";
				for (SeiteSO releaseGroup : kunde.getSeiten()) {
					for (SeiteSO release : releaseGroup.getSeiten()) {
						String html = release.getContent().getString(lang);
						if (release.hasContent(lang) > 0) {
							version = lastWord(release.getSeite().getTitle().getString(lang));
							doLines(html);
						}
					}
				}
			}
		}
	}

	private void doLines(String h) {
		int o = h.indexOf("<h3>");
		while (o >= 0) {
			o += "<h3>".length();
			int oo = h.indexOf("</h3>", o);
			if (oo >= 0) {
				String title = h.substring(o, oo);
				int i = title.indexOf(":");
				output += customer + ";" + version + ";" + title.substring(0, i) + "\r\n";
			}

			o = h.indexOf("<h3>", o);
		}

	}

	private String lastWord(String s) {
		int o = s.lastIndexOf(" ");
		return s.substring(o + 1);
	}

	@Override
	protected String render() {
		ctx.res.type("text/plain");
		return output;
	}
}
