package minerva.seite;

import java.util.List;
import java.util.stream.Collectors;

import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.model.UserSO;

public class PageTree {

    // ViewSeitePage
    public String getHTML(UserSO user, List<String> langs, SeiteSO seite) {
        String html = "";
        for (String lang : langs) {
            String hidden = lang.equals(user.getPageLanguage()) ? "" : " hidden";
            String tree = getHTML(seite.getBook().getSeiten(), lang, seite.getId());
            html += "<div id=\"tree_" + lang + "\"" + hidden + ">" + tree + "</div>";
        }
        return html;
    }
    
    // BookPage
    public String getHTML(SeitenSO seiten, String lang, String currentSeiteId) {
        List<TreeItem> treeItems = seiten.getTreeItems(lang, currentSeiteId, null);
        return tree2(treeItems, "", true);
    }
    
    private String tree2(List<TreeItem> treeItems, String id, boolean expanded) {
        String ret = "<ul id=\"P_" + id + "\" class=\"pagetree\"";
        if (id.isEmpty() || expanded) {
            ret += ">";
        } else {
            ret += " style=\"display:none;\">";
        }
        for (int i = 0; i < treeItems.size(); i++) {
            TreeItem seite = treeItems.get(i);
            String aClass = getCssClass(seite);
            String icon = seite.isNoTree() ? "fa-ban" : "fa-file-o";
            String color = seite.isNoTree() ? "#999" : "#666";
            icon = "<i class=\"fa " + icon + "\" style=\"color: " + color + ";\"></i> ";
            boolean hasVisibleSubpages = false;
            for (TreeItem subpage : seite.getSubitems()) {
                if (subpage.hasContent() > 0) {
                    hasVisibleSubpages = true;
                    icon = "<a onclick=\"treeclick('P_" + seite.getId() + "')\" class=\"tci\"><i id=\"iP_" +
                            seite.getId() + "\" class=\"fa " + (seite.isExpanded() ? "fa-caret-down" : "fa-caret-right") + "\" style=\"font-size: 15pt;"
                            + (seite.isExpanded() ? "" : " padding-right: 4px;") + "\"></i></a> ";
                    break;
                }
            }
            ret += "<li><nobr>" + icon + "<a" + aClass + " href=\"" + seite.getLink() + "\">" + seite.getTitle() + "</a>";
            if (showTags(i, treeItems)) {
                ret += seite.getTags().stream().sorted().map(tag ->
                        " <span class=\"label label-tag\" style=\"padding-left: 1.4em;\"><i class=\"fa fa-tag\"></i> " + tag + "</span>")
                        .collect(Collectors.joining());
            }
            ret += "</nobr></li>\n";
            if (hasVisibleSubpages) {
                ret += tree2(seite.getSubitems(), seite.getId(), seite.isExpanded()); // recursive
            }
        }
        return ret + "</ul>\n";
    }

    private String getCssClass(TreeItem seite) {
        String aClass = "";
        if (seite.hasContent() == 2) {
            aClass = " class=\"noContent\"";
        }
        if (seite.isCurrent()) {
            if (aClass.isEmpty()) {
                aClass = " class=\"treeActivePage\"";
            } else {
                aClass = " class=\"noContent treeActivePage\"";
            }
        }
        return aClass;
    }
    
    private boolean showTags(int x, List<TreeItem> seiten) {
        String xt = seiten.get(x).getTitle();
        for (int i = 0; i < seiten.size(); i++) {
            if (i != x && seiten.get(i).getTitle().equals(xt)) {
                return true;
            }
        }
        return false;
    }
}