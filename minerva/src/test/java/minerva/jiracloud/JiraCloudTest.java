package minerva.jiracloud;

import java.util.List;

import de.xmap.jiracloud.JiraCloudAccess;
import de.xmap.jiracloud.ReleaseNoteTicket;
import de.xmap.jiracloud.ReleaseTicket;

public class JiraCloudTest {

    public static void main(String[] args) {
        JiraCloudAccess jira = new JiraCloudAccess(System.getenv("MAIL"), System.getenv("TOKEN"),
                System.getenv("CUSTOMER"));
        List<ReleaseTicket> releaseTickets = ReleaseTicket.load(jira, System.getenv("PROJECT"));
        for (ReleaseTicket rt : releaseTickets) {
            if (rt.isRelevant()) {
                System.out.println(rt.getKey() + " / " + rt.getTargetVersion() + " / " + rt.getPageId());
                
                List<ReleaseNoteTicket> releaseNoteTickets = ReleaseNoteTicket.load(jira, rt.getPageId());
                for (ReleaseNoteTicket rnt : releaseNoteTickets) {
                    System.out.println("\t" + rnt.getKey() + ": " + rnt.getRNT_de());
                    System.out.println("\t\t" + rnt.getRNS_de());
                    System.out.println("\t\t" + rnt.getRND_de());
                    System.out.println("\t\t- images: " + rnt.getRND_de_images().size());
                }
            }
        }
    }
}
