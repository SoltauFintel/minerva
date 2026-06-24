package minerva.usage;

import java.util.ArrayList;
import java.util.List;

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

            ctx.redirect("/w/" + branch + "/usages?from=" + u(from) + "&to=" + u(to) + "&customer=" + u(customer));
        } else {
            String from = ctx.queryParam("from");
            String to = ctx.queryParam("to");
            String customer = ctx.queryParam("customer");
            String host = "http://docker05:4590"; // TODO host param.
            
            var sv = new UsageService();
            List<Usage> usages = sv.loadUsage(host, from, to, workspace);
            if (!StringService.isNullOrEmpty(customer)) {
                usages = usages.stream().filter(i -> i.getCustomer().equals(customer)).toList();
            }
            var customers = sv.getCustomers();
            
            header(n("Nutzungen"));
            put("cancellink", "/w/master/menu");
            put("from", esc(from));
            put("to", esc(to));
            put("host", esc(host));
            List<String> selectedItems = new ArrayList<>();
            if (customer != null) {
                selectedItems.add(customer);
            }
            var list = list("usages");
            for (Usage u : usages) {
                var map = list.add();
                map.put("customer", esc(u.getCustomer()));
                map.put("env", esc(u.getEnvironment()));
                map.put("lang", esc(u.getLanguage()));
                map.put("page", u.getLink() == null ? esc(u.getTitle())
                        : ("<a href=\"" + esc(u.getLink()) + "\">" + esc(u.getTitle()) + "</a>"));
                map.put("dt", esc(u.getDateTime()));
            }
            combobox("customers", new ArrayList<>(customers), selectedItems, true);
            Cols cols = Cols.of(
                    Col.si(n("date"), "dt"),
                    Col.si(n("customer"), "customer"),
                    Col.si(n("Environment"), "env"),
                    Col.si(n("language"), "lang"),
                    Col.si(n("page"), "page"));
            put("table", new TableComponent("wauto", cols, model, "usages").sort(0).sort(0));
            putSize("n", list);
        }
    }
}
