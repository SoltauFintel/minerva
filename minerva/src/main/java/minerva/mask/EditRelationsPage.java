package minerva.mask;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.base.StringService;
import minerva.base.UserMessage;
import minerva.mask.FeatureRelationsService.Relation;
import minerva.seite.SPage;

/**
 * Create and delete relations
 */
public class EditRelationsPage extends SPage {

    @Override
    protected void execute() {
        FeatureFieldsService sv = new FeatureFieldsService();
        FeatureFields ff = sv.get(seite);
        List<Relation> seiten = new FeatureRelationsService().getRelations(seite, ff); 
        if (isPOST()) {
            save(sv, ff, seiten);
        } else {
            header(n("createRelations"));
            put("featureTitle", esc(seite.getTitle()));
            DataList list = list("seiten");
            for (Relation s : seiten) {
                DataMap map = list.add();
                map.put("id", s.getId());
                map.put("icon", s.getIcon());
                map.put("title", esc(s.getTitle()));
            }
            put("hasSeiten", !seiten.isEmpty());
        }
    }

    private void save(FeatureFieldsService sv, FeatureFields ff, List<Relation> seiten) {
        List<String> pages = get("pages");
        List<String> tickets = get("tickets");
        List<String> links = get("links");
        
        boolean dirty = !pages.isEmpty() || !tickets.isEmpty() || !links.isEmpty();
        for (Relation s : seiten) {
            if ("on".equals(ctx.formParam(s.getId()))) {
                s.deleteFrom(ff);
                dirty = true;
            }
        }
        
        if (dirty) {
            validate(pages, links);
            ff.getPages().addAll(pages);
            ff.getTickets().addAll(tickets);
            ff.getLinks().addAll(links);
            sv.set(seite, ff);
        }
        
        ctx.redirect(viewlink);
    }

    private List<String> get(String name) {
        String w = ctx.formParam(name);
        if (StringService.isNullOrEmpty(w)) {
            return List.of();
        }
        return Arrays.asList(w.split("\n")).stream().filter(i -> !i.isBlank()).map(i -> i.trim()).collect(Collectors.toList());
    }

    private void validate(List<String> pages, List<String> links) {
        for (String id : pages) {
            if (!findPage(id)) {
                throw new UserMessage("pageNotFound3", workspace, s -> s.replace("$id", id));
            }
        }
        for (String link : links) {
            if (!link.startsWith("http://") && !link.startsWith("https://")) {
                throw new UserMessage("linkMustStartWithHttp", workspace);
            }
        }
    }
    
    private boolean findPage(String id) {
        return workspace.findPage(id) != null;
    }
}
