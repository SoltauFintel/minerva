package minerva.releasenotes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import de.xmap.jiracloud.ReleaseTicket;
import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.book.BPage;

/**
 * Release Notes Analysis page
 */
public class RNAPage extends BPage {

	@Override
	protected void execute() {
		put("ergebnis", "1\n2");
		if (isPOST()) {
			String customer = ctx.formParam("customer");
			String releaseNr = ctx.formParam("release");
			String releaseTicketNr = ctx.formParam("releaseTicket");
			String releaseNoteTicketNr = ctx.formParam("releaseNoteTicket");
			Logger.info("RNA: " + releaseNr + " | " + releaseTicketNr + " | " + releaseNoteTicketNr);
			put("ergebnis", analyse(customer.trim().toUpperCase(), releaseNr.trim(), releaseTicketNr.trim(), releaseNoteTicketNr.trim()));
		}
	}
	
	private String analyse(String customer, String r, String rt, String rnt) {
		String ret = "3\n4\n";

        ReleaseNotesConfig config = MinervaWebapp.factory().getConfig().loadReleaseNotesConfigs().stream()
                .filter(c -> c.getTicketPrefix().equals(customer)).findFirst().orElse(null);
        if (config == null) {
			return "Kunde " + customer + " nicht vorhanden! Es gibt diese Kunden: "
					+ MinervaWebapp.factory().getConfig().loadReleaseNotesConfigs().stream().map(i -> i.getCustomer())
							.collect(Collectors.joining(", "));
        }

        ReleaseNotesContext rc = new ReleaseNotesContext(config, null, book);
        ReleaseNotesService2 sv = new ReleaseNotesService2(rc);
        
        ret = "Kunde: " + customer + ", Sprache: " + rc.getLang() + "\n";
        
		if (!StringService.isNullOrEmpty(r)) {
			// Gibt es die Seite r bereits?
	        List<String> existingReleasePageTitles = sv.getExistingReleasePages();
	        String x = AbstractReleaseNotesService.TITLE_PREFIX + r;
	        if (existingReleasePageTitles.contains(x)) {
				String id = sv.getExistingReleasePages_getSeiteId(x);
				String link = id == null ? "" : " /s/" + branch + "/" + bookFolder + "/" + id;
				return ret + "Release " + r + " wurde bereits importiert." + " Löschen Sie die Release Seite" + link
						+ " in Minerva, um das Release erneut nach Minerva zu importieren.";
	        }
		}

		List<ReleaseTicket> rlist = sv.loadReleases_raw(customer);
		
		if (rlist.isEmpty()) {
			ret += "Es wurden keine Release Tickets für Kunde " + customer + " gefunden!";
		} else if (StringService.isNullOrEmpty(r)) {
			ret += "Es gibt diese Release Tickets zu Kunde " + customer + " in Jira:\n"
					+ rlist.stream().map(i -> "- " + i.getKey() + " | target version = " + i.getTargetVersion()
							+ " | page ID = " + i.getPageId()).collect(Collectors.joining("\n"));
		} else {
			Optional<ReleaseTicket> releaseTicket = rlist.stream().filter(i -> i.getTargetVersion().equals(r))
					.findFirst();
			if (releaseTicket.isPresent()) {
				ret += "Release Ticket zu Release " + r + ": " + releaseTicket.get().getKey() + " | page ID = "
						+ releaseTicket.get().getPageId();
			} else {
				if (StringService.isNullOrEmpty(rt)) {
					ret += "Es gibt kein Release Ticket zu Release '" + r + "'."
							+ "\nD.h. falls es doch ein Release Ticket geben sollte, ist bspw. 'target release version' in dem Ticket nicht korrekt gesetzt.\n"
							+ "Bitte die Release Ticket Nr. für eine tiefergehende Prüfung eingeben!";
				} else {
					Optional<ReleaseTicket> t = rlist.stream().filter(i -> i.getKey().equals(rt)).findFirst();
					if (t.isPresent()) {
						ReleaseTicket tt = t.get();
						ret += "Es gibt kein zugeordnetes Release Ticket. Release Ticket " + rt
								+ " ist aber vorhanden. target version: " + tt.getTargetVersion() + " | page ID = "
								+ tt.getPageId() + "\n";
						ret += "relevant check: " + (tt.isRelevant() ? "ok" : "nicht ok") + "\n";
						if (tt.getPageId() == null) {
							ret += "Page ID ist leer. Muss Format haben wie bspw. '2024-03-11T15:13:11.3+0000'.\n";
						} else if ("2024-03-11T15:13:11.3+0000".length() != tt.getPageId().length()) {
							ret += "Page ID muss das Format '2024-03-11T15:13:11.3+0000' haben und ein 'T' enthalten.\n";
						}
						if (StringService.isNullOrEmpty(tt.getTargetVersion())) {
							ret += "'Target release version' ist nicht belegt.\n";
						}
					}
				}
			}
		}
		
		return ret;
	}
}
