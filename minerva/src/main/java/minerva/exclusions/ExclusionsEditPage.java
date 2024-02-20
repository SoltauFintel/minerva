package minerva.exclusions;

import java.util.Set;
import java.util.TreeSet;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.MinervaWebapp;
import minerva.model.ExclusionsSO;
import minerva.user.UPage;

public class ExclusionsEditPage extends UPage {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        
        user.onlyAdmin();
        
        if (isPOST()) {
            String exclusions = ctx.formParam("exclusions");
            ex(branch).set(exclusions);
            Logger.info(user.getLogin() + " | " + branch + " | Exclusions saved.");
            user.log("Exclusions saved");
            ctx.redirect("/w/" + esc(branch) + "/exclusions/edit");
        } else {
            MinervaWebapp.factory().getBackendService().uptodatecheck(user.getWorkspace(branch), () -> {});
            String exStr = ex(branch).get();
            Exclusions ex = new Exclusions(exStr);

            header(n("exclusions"));
            put("exclusions", esc(exStr));
            
            // table
            
            Set<String> tags = new TreeSet<>();
            Set<String> customers = ex.getCustomers();
            DataList list = list("customers");
            for (String customer : customers) {
                list.add().put("customer", esc(customer));
                for (String tag : ex.getTags(customer)) {
                    tags.add(tag.startsWith("+") || tag.startsWith("-") ? tag.substring(1) : tag);
                }
            }
            DataList list2 = list("rows");
            addTableRows(ex, tags, customers, list2, true);
            addTableRows(ex, tags, customers, list2, false);
        }
    }

    private void addTableRows(Exclusions ex, Set<String> tags, Set<String> customers, DataList list2, boolean inCustomers) {
        for (String tag : tags) {
            String shortTag = tag.startsWith("+") || tag.startsWith("-") ? tag.substring(1) : tag;
            if (inCustomers == customers.contains(shortTag)) {
                DataMap row = list2.add();
                row.put("tag", esc(tag));
                DataList list3 = row.list("customers");
                for (String customer : customers) {
                    DataMap map3 = list3.add();
                    boolean plus = ex.getTags(customer).contains("+" + tag);
                    map3.put("plus", plus);
                    boolean minus = !plus && ex.getTags(customer).contains("-" + tag);
                    map3.put("minus", minus);
                    map3.put("x", !plus & !minus && ex.getTags(customer).contains(tag));
                }
            }
        }
    }

    private ExclusionsSO ex(String branch) {
        return user.getWorkspace(branch).getExclusions();
    }
}
