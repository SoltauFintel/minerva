package minerva.base;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gitper.base.StringService;
import minerva.user.UAction;

public class TableComponent extends UAction {
	private final String tableCSS;
	private final Map<String, String> global;
	private final List<Col> cols;
	private final List<Map<String, String>> list;
	private String html;
	private int sortedColumn = -1;
	private boolean asc = false;
	
	public TableComponent(String tableCSS, Map<String, String> global, List<Col> cols, List<Map<String, String>> list) {
		this.tableCSS = tableCSS;
		this.global = global;
		this.cols = cols;
		this.list = list;
	}

	@Override
	protected void execute() {
		if (sortedColumn >= 0) {
			Col col = cols.get(sortedColumn);
			String sortkey = col.getSortkey();
			if (StringService.isNullOrEmpty(sortkey)) {
				sortkey = "__sort";
				for (Map<String, String> map : list) {
					map.put(sortkey, StringService.umlaute(ins(ins(col.getRowHTML(), global), map)));
				}
			}
			list.sort(getComparator(sortkey, asc));
		}
		
		String headers = "", rows = "";
		String sortlink = TableSortAction.register(this);
		String sid = sortlink.replace("/", "");
		for (int i = 0; i < cols.size(); i++) {
			Col col = cols.get(i);
			if (ColSort.NONE.equals(col.getSort())) {
				headers += "\n<th class=\"" + col.getHeaderCSS() + "\">";
			} else {
				headers += "\n<th><a href=\"#\" class=\"sortlink " + col.getHeaderCSS() + "\" hx-get=\"" + sortlink + i
						+ "\" hx-target=\"." + sid + "\" hx-swap=\"outerHTML\">";
			}
			headers += ins(col.getHeaderHTML(), global);
			String sort = "";
			if (!ColSort.NONE.equals(col.getSort()) && i == sortedColumn) {
				sort = asc ? "fa-arrow-down" : "fa-arrow-up";
			}
			headers += "<i class=\"fa fa-fw " + sort + " sortarrow\"></i>";
			if (!ColSort.NONE.equals(col.getSort())) {
				headers += "</a>";
			}
			headers += "</th>";
		}
		for (Map<String, String> map : list) {
			rows += "\n<tr>";
			for (Col col : cols) {
				String c = ins(ins(col.getRowHTML(), global), map);
				rows += "\n<td class=\"" + col.getRowCSS() + "\">" + c + "</td>";
			}
			rows += "\n</tr>";
		}
		html = "<table class=\"table table-striped table-hover mt2 " + tableCSS + " " + sid + "\">\n"
				+ "<tr>" + headers + "\n</tr>"
				+ rows
				+ "\n</table>\n";
	}

	private String ins(String c, Map<String, String> map) {
		for (Entry<String, String> e : map.entrySet()) {
			c = c.replace("{{" + e.getKey() + "}}", e.getValue());
		}
		return c;
	}
	
	@Override
	protected String render() {
		return html;
	}
	
	public static class Col {
		/** null or empty: sort by column content (but be aware if there's HTML as content!) */
		private final String sortkey;
		private final ColSort sort;
		private final String headerCSS;
		private final String headerHTML;
		private final String rowCSS;
		private final String rowHTML;
		
		public Col(String headerCSS, String headerHTML, String rowCSS, String rowHTML) {
			this(null, ColSort.NONE, headerCSS, headerHTML, rowCSS, rowHTML);
		}
		
		private Col(String sortkey, ColSort sort, String headerCSS, String headerHTML, String rowCSS, String rowHTML) {
			this.sortkey = sortkey;
			this.sort = sort;
			this.headerCSS = headerCSS;
			this.headerHTML = headerHTML;
			this.rowCSS = rowCSS;
			this.rowHTML = rowHTML;
		}

		public Col(String headerHTML, String rowHTML) {
			this(null, ColSort.NONE, "", headerHTML, "", rowHTML);
		}

		public Col(String headerHTML, String rowCSS, String rowHTML) {
			this(null, ColSort.NONE, "", headerHTML, rowCSS, rowHTML);
		}

		public Col sortable(String sortkey) {
			return new Col(sortkey, ColSort.ASC_DESC, headerCSS, headerHTML, rowCSS, rowHTML);
		}

		public Col asc(String sortkey) {
			return new Col(sortkey, ColSort.ASC, headerCSS, headerHTML, rowCSS, rowHTML);
		}

		public Col desc(String sortkey) {
			return new Col(sortkey, ColSort.DESC, headerCSS, headerHTML, rowCSS, rowHTML);
		}

		public String getSortkey() {
			return sortkey;
		}

		public ColSort getSort() {
			return sort;
		}

		public String getHeaderCSS() {
			return headerCSS;
		}

		public String getHeaderHTML() {
			return headerHTML;
		}

		public String getRowCSS() {
			return rowCSS;
		}

		public String getRowHTML() {
			return rowHTML;
		}
	}
	
	/**
	 * Sort order for column
	 */
	public enum ColSort {
		NONE,
		ASC,
		DESC,
		ASC_DESC;
	}

	public TableComponent sort(int col) {
		if (col >= 0 && col < cols.size()) {
			switch (cols.get(col).getSort()) {
				case NONE:
					sortedColumn = -1;
					return this;
				case DESC:
					asc = false;
					break;
				case ASC:
					asc = true;
					break;
				case ASC_DESC:
					if (sortedColumn == col) { // ASC <-> DESC
						asc = !asc;
					} else {
						asc = true;
					}
					break;
			}
			sortedColumn = col;
		} else if (col == -1) {
			sortedColumn = col;
		}
		return this;
	}
	
	protected Comparator<Map<String, String>> getComparator(String sortkey, boolean asc) {
		return (a, b) -> (asc ? 1 : -1) * a.get(sortkey).compareToIgnoreCase(b.get(sortkey));
	}
}
