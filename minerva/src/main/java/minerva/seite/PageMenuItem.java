package minerva.seite;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.model.BookSO;

public class PageMenuItem {
	private final boolean visible;
	private final String link;
	private final String icon;
	private final String label;
	private final boolean checked;

	public PageMenuItem(String link, String icon, String label) {
		this(true, link, icon, label, false);
	}

	public PageMenuItem(boolean visible, String link, String icon, String label) {
		this(visible, link, icon, label, false);
	}

	public PageMenuItem(String link, String icon, String label, boolean checked) {
		this(true, link, icon, label, checked);
	}

	private PageMenuItem(boolean visible, String link, String icon, String label, boolean checked) {
		this.visible = visible;
		this.link = link;
		this.icon = icon;
		this.label = label;
		this.checked = checked;
	}
	
	public void add(PageMenuContext ctx, DataList menuitems) {
		if (!visible) {
			return;
		}
        DataMap map = menuitems.add();
		BookSO book = ctx.getSeite().getBook();
		map.put("link", link.replace("{viewlink}", ctx.getSeite().viewlink())
						.replace("{branch}", book.getWorkspace().getBranch())
						.replace("{bookFolder}", book.getBook().getFolder())
						.replace("{id}", ctx.getSeite().getId())
						.replace("{duplicatelink}", ctx.get("duplicatelink"))
						.replace("{movelink}", ctx.get("movelink"))
						.replace("{deletelink}", ctx.get("deletelink")));
        map.put("icon", icon);
        map.put("label", getLabel(ctx));
        map.put("line", "-".equals(label));
        map.put("attrs", "");
        map.put("liArgs", "");
        if (link.startsWith(" ")) {
            map.put("link", "#");
            map.put("attrs", link);
        } else if (link.contains("/delete")) {
            map.put("attrs", " style=\"color: #900;\"");
        } else if (link.contains("editorsnote")) {
            map.put("liArgs", " style=\"background-color: #ff9;\"");
        }
	}

	private String getLabel(PageMenuContext ctx) {
		String caption = label;
        if (caption.startsWith("N.")) {
        	String key = caption.substring("N.".length());
        	int o = key.indexOf("|");
        	if (o >= 0) {
        		caption = ctx.n(key.substring(0, o)) + key.substring(o + 1);
        	} else {
        		caption = ctx.n(key);
        	}
        }
        if (checked) {
        	caption += " <i class=\"fa fa-check greenbook\"></i>";
        }
		return caption;
	}
}
