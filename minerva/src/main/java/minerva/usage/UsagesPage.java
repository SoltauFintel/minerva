package minerva.usage;

import java.util.ArrayList;
import java.util.List;

import github.soltaufintel.amalia.web.config.AppConfig;
import github.soltaufintel.amalia.web.table.Col;
import github.soltaufintel.amalia.web.table.Cols;
import github.soltaufintel.amalia.web.table.TableComponent;
import gitper.base.StringService;
import minerva.workspace.WPage;

public class UsagesPage extends WPage {

    @Override
    protected void execute() {
        if (isPOST()) {
            String from = ctx.formParam("usageFrom");
            String to = ctx.formParam("usageTo");
            String customer = ctx.formParam("usageCustomer");
            String host = ctx.formParam("usageHost");

            ctx.redirect("/w/" + branch + "/usages?from=" + u(from) + "&to=" + u(to) +
                "&host=" + u(host) + "&customer=" + u(customer));
        } else {
            String from = ctx.queryParam("from");
            String to = ctx.queryParam("to");
            String customer = ctx.queryParam("customer");
            String host = ctx.queryParam("host");

            display(from, to, customer, host);
        }
    }

    private void display(String from, String to, String customer, String host) {
        var sv = new UsageService();
        List<String> hosts = getHosts();
        if (!hosts.contains(host)) {
            host = hosts.get(0);
        }
        List<Usage> usages = sv.loadUsage(host, from, to, workspace);
        if (!StringService.isNullOrEmpty(customer)) {
            usages = usages.stream().filter(i -> i.getCustomer().equals(customer)).toList();
        }
        var customers = sv.getCustomers();
        
        header(n("Nutzungen"));
        put("cancellink", "/w/" + branch + "/menu");
        put("from", esc(from));
        put("to", esc(to));
        put("loadedFromHost", esc(host));
        var list = list("usages");
        for (Usage u : usages) {
            var map = list.add();
            map.put("dt", esc(u.getDateTime()));
            map.put("c", esc(u.getCustomer()));
            map.put("env", esc(u.getEnvironment()));
            map.put("lang", esc(u.getLanguage()));
            map.put("page", u.getLink() == null ? esc(u.getTitle())
                    : ("<a href=\"" + esc(u.getLink()) + "\">" + esc(u.getTitle()) + "</a>"));
            map.put("pageSort", esc(u.getTitle().toLowerCase()));
        }
        combobox("customers", new ArrayList<>(customers), customer, true);
        combobox("hosts", hosts, hosts.get(0), false);
        put("table", new TableComponent("wauto", cols(), model, "usages").sort(0).sort(0));
        putSize("n", list);
    }

    private List<String> getHosts() {
        List<String> ret = new ArrayList<>();
        for (String host : new AppConfig().get("usage.hosts", "").split(",")) {
            if (!host.isBlank()) {
                ret.add(host.trim());
            }
        }
        if (ret.isEmpty()) {
            throw new RuntimeException("Missing parameter 'usage.hosts' in AppConfig!");
        }
        return ret;
    }

    private Cols cols() {
        return Cols.of(
                Col.si(n("date"), "dt"),
                Col.si(n("customer"), "c"),
                Col.si(n("Environment"), "env"),
                Col.si(n("language"), "lang"),
                Col.i(n("page"), "page").sortable("pageSort"));
    }
}
