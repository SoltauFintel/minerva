package minerva.export;

import org.pmw.tinylog.Logger;

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
				title = title.replace("DEVK ", "DEVK");
				customer = lastWord(title) + ";" + lang;
				version = "";
				for (SeiteSO releaseGroup : kunde.getSeiten()) {
					for (SeiteSO release : releaseGroup.getSeiten()) {
						String html = release.getContent().getString(lang);
						if (release.hasContent(lang) > 0) {
							String q = release.getSeite().getTitle().getString(lang);
							int qq = q.lastIndexOf(" (");
							if (qq < 0) {
								if (q.startsWith("Release Notes ")) {
									version = q.substring("Release Notes ".length());
									Logger.error("version not found in: \"" + q + "\" -> try with this: \"" + version + "\"");
								} else {
									Logger.error("version not found in: \"" + q + "\"");
								}
							} else {
								version = lastWord(q.substring(0, qq));
							}
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
