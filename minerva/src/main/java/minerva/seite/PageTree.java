package minerva.seite;

import java.util.List;
import java.util.Set;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.model.SeitenSO;

public class PageTree {

    public String getHTML(SeitenSO seiten, List<String> langs, String currentSeiteId, String pageLanguage) {
        String html = "";
        for (String lang : langs) {
            String t = "<div id=\"tree_{lang}\"{hidden}>{tree}</div>";
            html += t.replace("{lang}", lang)
                    .replace("{hidden}", lang.equals(pageLanguage) ? "" : " hidden")
                    .replace("{tree}", getHTML(seiten, lang, currentSeiteId));
        }
        return html;
    }
    
    private String getHTML(SeitenSO seiten, String lang, String currentSeiteId) {
        List<TreeItem> treeItems = seiten.getTreeItems(lang, currentSeiteId, null);
        return makeHTML(treeItems, "", true);
    }
    
    private String makeHTML(List<TreeItem> treeItems, String id, boolean expanded) {
        String ret = "";
        for (int i = 0; i < treeItems.size(); i++) {
            ret += makeItemHTML(i, treeItems);
        }
        return "<ul>" + ret + "</ul>\n";
    }

    private String makeItemHTML(int i, List<TreeItem> treeItems) {
        TreeItem seite = treeItems.get(i);
        String title = Escaper.esc(seite.getTitle());
        if (seite.isCurrent()) {
            title = "<b>" + title + "</b>";
        }
        boolean hasVisibleSubpages = !seite.getSubitems().isEmpty();
        String sub = "";
        if (hasVisibleSubpages) {
            sub = makeHTML(seite.getSubitems(), seite.getId(), seite.isExpanded()); // recursive
        }
        
        String t = "<li id='iP_{id}' data-jstree='{\"icon\":\"fa {icon}\"}' {open}>{title}{tags}{sub}</li>\n";
        return t.replace("{id}", seite.getId())
                .replace("{icon}", getIcon(seite))
                .replace("{open}", seite.isExpanded() ? " class='jstree-open'" : "")
                .replace("{title}", title)
                .replace("{tags}", showTags(i, treeItems) ? getTagsHtml(seite.getTags()) : "")
                .replace("{sub}", sub);
    }

    private String getIcon(TreeItem seite) {
        if (seite.isNoTree()) {
            return "fa-ban pagetreeIconColorNoTree";
        } else if (seite.hasSubpages()) { // page is empty but has non-empty subpages
            return "fa-file-o pagetreeIconColorNoContent";
        } else {
            return "fa-file-text-o pagetreeIconColor";
        }
    }
    
    // Show tags only if title occurs twice in same layer.
    private boolean showTags(int x, List<TreeItem> seiten) {
        String xt = seiten.get(x).getTitle();
        for (int i = 0; i < seiten.size(); i++) {
            if (i != x && seiten.get(i).getTitle().equals(xt)) {
                return true;
            }
        }
        return false;
    }

    private String getTagsHtml(Set<String> tags) {
        String ret = "";
        for (String tag : tags) {
            ret += "<span class=\"label label-tag ml5px\"><i class=\"fa fa-tag\"></i> " + Escaper.esc(tag) + "</span>";
        }
        return ret;
    }
}