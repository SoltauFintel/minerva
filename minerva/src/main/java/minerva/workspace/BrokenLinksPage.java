package minerva.workspace;

import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

public class BrokenLinksPage extends WPage {

    @Override
    protected void execute() {
        String host = "http://192.168.40.77:4490";

        BrokenLinksService sv = new BrokenLinksService();
        BrokenLinksSite site = sv.examine(host);
        
        header("Broken Links");
        put("host", esc(host));
        String a = "";
        for (BrokenLink bl : site.getBrokenLinks()) {
            a += bl.getCustomer() + "\n" + bl.getErrorType() + "\n" + bl.getUrl() + "\n"
                    + bl.getCallers().stream().map(i -> "- " + i).collect(Collectors.joining("\n")) + "\n\n";
        }
        put("a", esc(a));
        Logger.info(user.getLogin() + " | " + site.getBrokenLinks().size() + " Broken Links | " + host);
    }
}
