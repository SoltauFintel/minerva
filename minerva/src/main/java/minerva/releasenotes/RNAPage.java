package minerva.releasenotes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import de.xmap.jiracloud.ReleaseNoteTicket;
import de.xmap.jiracloud.ReleaseTicket;
import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.book.BPage;
import minerva.config.MinervaOptions;

/**
 * Release Notes Analysis page
 */
public class RNAPage extends BPage {

	@Override
	protected void execute() {
		put("ergebnis", "");
		put("hasRows", false);
		put("jiraCustomer", esc(MinervaOptions.JIRA_CUSTOMER.get()));
		if (isPOST()) {
			String customer = ctx.formParam("customer");
			String releaseNr = ctx.formParam("release");
			String releaseTicketNr = ctx.formParam("releaseTicket");
			String releaseNoteTicketNr = ctx.formParam("releaseNoteTicket");
			ctx.redirect(booklink + "/rna?c=" + u(customer) + "&r=" + u(releaseNr) + "&rt=" + u(releaseTicketNr)
					+ "&rnt=" + u(releaseNoteTicketNr));
		} else {
			String customer = queryParam("c");
			String releaseNr = queryParam("r");
			String releaseTicketNr = queryParam("rt");
			String releaseNoteTicketNr = queryParam("rnt");
			Logger.info("RNA: " + customer + " | " + releaseNr + " | " + releaseTicketNr + " | " + releaseNoteTicketNr);
			String ergebnis;
			try {
				ergebnis = analyse(customer, releaseNr, releaseTicketNr, releaseNoteTicketNr, list("releases"));
			} catch (Exception e) {
				Logger.error(e);
				ergebnis = e.getMessage();
			}
			put("ergebnis", ergebnis);
			header("Release Notes Analyse");
			put("c", esc(customer));
			put("r", esc(releaseNr));
			put("rn", esc(releaseTicketNr));
			put("rnt", esc(releaseNoteTicketNr));
		}
	}
	
	private String queryParam(String key) {
		String ret = ctx.queryParam(key);
		return ret == null ? "" : ret.trim().toUpperCase();
	}
	
	private String analyse(String customer, String r, String rt, String rnt, DataList list) {
		String ret = "";

        ReleaseNotesConfig config = ReleaseNotesConfig.get(customer);
        if (config == null) {
        	if (StringService.isNullOrEmpty(customer)) {
				return "Bitte Kunde eingeben!" + getKunden();
			} else {
				return "Kunde " + customer + " nicht vorhanden!" + getKunden();
			}
        }

        ReleaseNotesContext rc = new ReleaseNotesContext(config, null, book);
        ReleaseNotesService2 sv = new ReleaseNotesService2(rc);
        
        ret = "Kunde: " + customer + ", Sprache: " + rc.getLang() + "\n\n";
        
		if (!StringService.isNullOrEmpty(r)) {
			String m = analyse1(sv, r);
			if (m != null) {
				return ret + m;
			}
		}

		List<ReleaseTicket> rlist = sv.loadReleases_raw(customer);
		ret = analyse2(customer, r, rt, rnt, ret, rc, sv, rlist);
		releases(list, sv);
		return ret;
	}
	
	private String analyse1(ReleaseNotesService2 sv, String r) {
		// Gibt es die Seite r bereits?
        List<String> existingReleasePageTitles = sv.getExistingReleasePages();
        String x = AbstractReleaseNotesService.TITLE_PREFIX + r;
        if (existingReleasePageTitles.contains(x)) {
			String id = sv.getExistingReleasePages_getSeiteId(x);
			String link = id == null ? "" : " /s/" + branch + "/" + bookFolder + "/" + id;
			return "Release " + r + " wurde bereits importiert." + " Löschen Sie die Release Seite" + link
					+ " in Minerva, um das Release erneut nach Minerva zu importieren.";
        }
        return null;
	}

	private String analyse2(String customer, String r, String rt, String rnt, String ret, ReleaseNotesContext rc,
			ReleaseNotesService2 sv, List<ReleaseTicket> rlist) {
		if (rlist.isEmpty()) {
			ret += "Es wurden keine Release Tickets für Kunde " + customer + " gefunden!";
		} else if (StringService.isNullOrEmpty(r)) {
			ret += "Es gibt diese Release Tickets zu Kunde " + customer + " in Jira:\n"
					+ rlist.stream().map(i -> "- " + i.getKey() + " | target version = " + i.getTargetVersion()
					+ " | page ID = " + i.getPageId() + " | gültig = " + (i.isRelevant() ? "ja" : "nein"))
					.collect(Collectors.joining("\n"));
		} else {
			Optional<ReleaseTicket> releaseTicket = rlist.stream().filter(i -> r.equals(i.getTargetVersion()))
					.findFirst();
			if (releaseTicket.isPresent()) {
				ReleaseTicket tt = releaseTicket.get();
				ret += "Release Ticket zu Release " + r + ": " + tt.getKey() + " | page ID = " + tt.getPageId()
						+ " | gültig = " + tt.isRelevant() + "\n";
				ret += findReleaseNoteTickets(tt, sv, rc.getLang(), rnt);
			} else {
				if (StringService.isNullOrEmpty(rt)) {
					ret += "Es gibt kein Release Ticket zu Release '" + r + "'."
							+ "\nD.h. falls es doch ein Release Ticket geben sollte, ist bspw. 'target release version' in dem Ticket nicht korrekt gesetzt."
							+ "\n\nBitte die Release Ticket Nr. für eine tiefergehende Prüfung eingeben!";
				} else {
					ret = analyse3(r, rt, ret, rlist);
				}
			}
		}
		return ret;
	}

	private String analyse3(String r, String rt, String ret, List<ReleaseTicket> rlist) {
		Optional<ReleaseTicket> t = rlist.stream().filter(i -> i.getKey().equals(rt)).findFirst();
		if (t.isPresent()) {
			ReleaseTicket tt = t.get();
			ret += "Es gibt kein zugeordnetes Release Ticket. Release Ticket " + rt
					+ " ist aber vorhanden.";
			ret += " target version: " + tt.getTargetVersion() + " | page ID = " + tt.getPageId() + "\n";
			ret += "gültig: " + (tt.isRelevant() ? "ja" : "nein") + "\n";
			if (tt.getPageId() == null) {
				ret += "Page ID ist leer. Muss Format haben wie bspw. '2024-03-11T15:13:11.3+0000'.\n";
			} else if ("2024-03-11T15:13:11.3+0000".length() != tt.getPageId().length()) {
				ret += "Page ID muss das Format '2024-03-11T15:13:11.3+0000' haben und ein 'T' enthalten.\n";
			}
			if (StringService.isNullOrEmpty(tt.getTargetVersion())) {
				ret += "'Target release version' ist nicht belegt.\n";
			} else if (r.equals(tt.getTargetVersion())) {
				ret += "'Target release version' hat falschen Wert im Ticket. SOLL=" + r + ", IST="
						+ tt.getTargetVersion() + "\n";
			}
		} else {
			ret += "Release Ticket " + rt + " ist nicht vorhanden!"
					+ "\nFalls es doch ein Jira Ticket mit dieser Nummer geben sollte: ist der Tickettyp 'Release'?";
		}
		return ret;
	}

	private void releases(DataList list, ReleaseNotesService2 sv) {
		sv.loadAllReleases_raw().stream().sorted((a, b) -> b.getKey().compareTo(a.getKey())).forEach(i -> {
			DataMap map = list.add();
			map.put("ticketnr", esc(i.getKey()));
			map.put("title", esc(i.getTitle()));
			map.put("release", esc(i.getTargetVersion()));
			map.put("pageID", esc(i.getPageId()));
			map.put("relevant", i.isRelevant() ? "ja" : "nein");
			map.put("imported", "");
		});
		put("hasRows", list.size() > 0);
		putInt("rows", list.size());
	}

	private String findReleaseNoteTickets(ReleaseTicket y, ReleaseNotesService2 sv, String lang, String rnt) {
		List<ReleaseNoteTicket> list = sv.loadReleaseNoteTickets(y.getPageId());
		Logger.info("findReleaseNoteTickets pageId=" + y.getPageId() + " size=" + list.size());
		String ret = getInfo(y.isRelevant(), list.isEmpty()) + list.stream()
			.map(r -> "- " + r.getKey() + ": " + r.getRNT(lang) + "\n")
			.collect(Collectors.joining());
		String msg = "";
		if (!StringService.isNullOrEmpty(rnt)) {
			boolean vorh = list.stream().anyMatch(i -> i.getKey().equals(rnt));
			msg = "Release Note Ticket '" + rnt + "' ist in Bezug auf das Release " + (vorh ? "" : "nicht ") + "vorhanden.\n";
			if (!vorh) {
				msg += "Bitte prüfen, ob das Ticket " + rnt + " existiert und ob die Page ID im Feld 'Release notes page Ids' eingetragen ist.\n";
			}
		}
		return msg + ret;
	}

	private String getInfo(boolean relevant, boolean empty) {
		if (empty) {
			if (relevant) {
				return "Release Ticket ist in Ordnung, aber es gibt keine Release Note Tickets.\n";
			} else {
				return "Release Ticket ist nicht gültig und es gibt auch keine Release Note Tickets.\n";
			}
		} else {
			if (relevant) {
				return "Release Notes Import ist möglich.\n";
			} else {
				return "Release Ticket ist nicht gültig. Es gibt Release Note Tickets.\n";
			}
		}
	}

	private String getKunden() {
		List<ReleaseNotesConfig> rnConfigs = MinervaWebapp.factory().getConfig().loadReleaseNotesConfigs();
		String kundenliste = rnConfigs.stream().map(i -> i.getTicketPrefix()).collect(Collectors.joining(", "));
		return " Es gibt diese Kunden: " + kundenliste;
	}
}
