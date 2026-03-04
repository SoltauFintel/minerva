package minerva.workspace;

import java.util.Set;

import gitper.base.StringService;
import minerva.model.UserSO;

/**
 * User can hide branches. Admin can hide branches for all users.
 */
public class HideBranchsPage extends WPage {

    @Override
    protected void execute() {
        String hide = ctx.queryParam("h");
        String name = ctx.queryParam("n");
        boolean forAll = "a".equals(ctx.queryParam("u")) && UserSO.isAdmin(ctx);

        if (("1".equals(hide) || "0".equals(hide)) && !StringService.isNullOrEmpty(name)) {
            render = false;
            if ("1".equals(hide)) {
                user.addHiddenBranch(name, forAll);
            } else {
                user.removeHiddenBranch(name, forAll);
            }
            ctx.redirect("/w/" + branch + "/hide-branch");
        } else {
            display();
        }
    }

    private void display() {
        header(n("hideBranches"));
        put("admin", UserSO.isAdmin(ctx));
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
            map.put("a", (hidden ? a1 : a0) + u(name));
            map.put("b", n(hidden ? "Einblenden" : "Ausblenden"));
            map.put("c", hidden ? "#f88" : "#6f6");
            map.put("i", hidden ? "fa-times error" : "fa-check greenbook");
        }
    }
}
