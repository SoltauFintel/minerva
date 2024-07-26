package minerva.jiracloud;

import java.util.List;

import de.xmap.jiracloud.JiraCloudAccess;
import de.xmap.jiracloud.ReleaseNoteTicket;
import de.xmap.jiracloud.ReleaseTicket;

public class JiraCloudTest {

    public static void main(String[] args) {
        JiraCloudAccess jira = new JiraCloudAccess(System.getenv("MAIL"), System.getenv("TOKEN"), System.getenv("CUSTOMER"));
        //jira.setDebugMode(true);
        List<ReleaseTicket> releaseTickets = ReleaseTicket.load(jira, System.getenv("PROJECT"));
        System.out.println(System.getenv("PROJECT") + ": " + releaseTickets.size());
        for (ReleaseTicket rt : releaseTickets) {
            if (rt.isRelevant()) {
                System.out.println(rt.getKey() + " / " + rt.getTargetVersion() + " / " + rt.getPageId());
                
                List<ReleaseNoteTicket> releaseNoteTickets = ReleaseNoteTicket.load(jira, rt.getPageId());
                for (ReleaseNoteTicket rnt : releaseNoteTickets) {
                    System.out.println("\t" + rnt.getKey() + ": " + rnt.getRNT("en"));
                    System.out.println("\t\t" + rnt.getRNS().get("en").getText());
                    System.out.println("\t\t" + rnt.getRND().get("en").getText());
                    System.out.println("\t\t- images: " + rnt.getRND().get("en").getImages().size());
                    String rf = rnt.getReleaseFor();
                    System.out.println("\t\t- released for: " + rf);
                    rnt.loadCustomerTicketNumberAndType(System.getenv("PROJECT"), jira);
                    System.out.println("\t\t- released for - issue type: " + rnt.getReleaseFor_issueType());
                    System.out.println("\t\t- customer ticket number: " + rnt.getCustomerTicketNumber());
                }
            }
        }
    }
}
