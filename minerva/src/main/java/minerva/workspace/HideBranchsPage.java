package minerva.workspace;

import java.util.Set;

import github.soltaufintel.amalia.web.table.Col;
import github.soltaufintel.amalia.web.table.Cols;
import github.soltaufintel.amalia.web.table.TableComponent;
import gitper.base.StringService;

public class HideBranchsPage extends WPage {

    @Override
    protected void execute() {
        String hide = ctx.queryParam("h");
        String name = ctx.queryParam("n");

        if (("1".equals(hide) || "0".equals(hide)) && !StringService.isNullOrEmpty(name)) {
            if ("1".equals(hide)) {
                user.addHiddenBranch(name);
            } else {
                user.removeHiddenBranch(name);
            }
            ctx.redirect("/w/" + branch + "/hide-branch");
        } else {
            display();
        }
    }

    private void display() {
        header(n("hideBranches"));
        var branchNames = user.getBranchNames();
        branchNames.remove("master");
        branchNames = MenuPage.branchNamesFilter.filter(branchNames, user.getLogin());
        Set<String> hiddenBranches = user.getHiddenBranches();
        var list = list("branches");
        String a = "/w/" + esc(branch) + "/hide-branch?h=";
        String a0 = a + "1&n=";
        String a1 = a + "0&n=";
        for (String name : branchNames) {
            var map = list.add();
            map.put("name", esc(name));
            var hidden = hiddenBranches.contains(name);
            map.put("hidden", hidden);
            if (hidden) {
                map.put("a", a1 + u(name));
                map.put("b", n("Einblenden"));
                map.put("i", "fa-times error");
            } else {
                map.put("a", a0 + u(name));
                map.put("b", n("Ausblenden"));
                map.put("i", "fa-check greenbook");
            }
        }
        Cols cols = Cols.of( //
                new Col("", "<i class=\"fa {{i.i}}\"></i>"),
                Col.i("Branch", "name"), //
                new Col("", "<a href=\"{{i.a}}\" class=\"btn btn-xs btn-default\">{{i.b}}</a>") //
        );
        put("table", new TableComponent("wauto", cols, model, "branches"));
    }
}
