package minerva.base;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import gitper.base.StringService;
import minerva.user.UAction;

public class TableComponent extends UAction {
	private final String tableCSS;
	private final Map<String, String> global;
	private final List<Col> cols;
	private final List<Map<String, String>> list;
	private final String sortlink;
	private final String sid;
	private int sortedColumn = -1;
	private boolean asc = false;
	private String html;
	
	public TableComponent(String tableCSS, Map<String, String> global, List<Col> cols, List<Map<String, String>> list) {
		this.tableCSS = tableCSS;
		this.global = global;
		this.cols = cols;
		this.list = list;
		sortlink = TableSortAction.register(this);
		sid = sortlink.replace("/", "");
	}

	@Override
	protected void execute() {
		StringBuilder sb = new StringBuilder();
		sb.append("<table class=\"table table-striped table-hover mt2 " + tableCSS + " " + sid + "\">\n<tr>");
		sb.append(makeHeader());
		sb.append("\n</tr>");
		makeRows(sb);
		sb.append("\n</table>\n");
		html = sb.toString();
	}

	private StringBuilder makeHeader() {
		StringBuilder headers = new StringBuilder();
		for (int i = 0; i < cols.size(); i++) {
			Col col = cols.get(i);
			if (ColSort.NONE.equals(col.getSort())) {
				headers.append("\n<th class=\"" + col.getHeaderCSS() + "\">");
			} else {
				headers.append("\n<th><a href=\"#\" class=\"sortlink " + col.getHeaderCSS() + "\" hx-get=\"" + sortlink
						+ i + "\" hx-target=\"." + sid + "\" hx-swap=\"outerHTML\">");
			}
			headers.append(replaceVars(col.getHeaderHTML(), global));
			String sortIcon = "";
			if (!ColSort.NONE.equals(col.getSort()) && i == sortedColumn) {
				sortIcon = asc ? "fa-arrow-down" : "fa-arrow-up";
			}
			headers.append("<i class=\"fa fa-fw " + sortIcon + " sortarrow\"></i>");
			if (!ColSort.NONE.equals(col.getSort())) {
				headers.append("</a>");
			}
			headers.append("</th>");
		}
		return headers;
	}

	private void makeRows(StringBuilder rows) {
		Comparator<Map<String, String>> comparator = comparator();
		Stream<Map<String, String>> stream = list.stream();
		if (comparator != null) {
			stream = stream.sorted(comparator);
		}
		stream.forEach(map -> {
			rows.append("\n\t<tr>");
			cols.forEach(col -> {
				String content = replaceVars(col.getRowHTML(), global);
				content = replaceVars(content, map);
				rows.append("\n\t\t<td class=\"" + col.getRowCSS() + "\">" + content + "</td>");
			});
			rows.append("\n\t</tr>");
		});
	}

	private String replaceVars(String content, Map<String, String> vars) {
		for (Entry<String, String> e : vars.entrySet()) {
			content = content.replace("{{" + e.getKey() + "}}", e.getValue());
		}
		return content;
	}
	
	@Override
	protected String render() {
		String ret = html;
		html = null;
		return ret;
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
	
	private Comparator<Map<String, String>> comparator() {
		if (sortedColumn < 0) {
			return null;
		}
		Col col = cols.get(sortedColumn);
		String sortkey = col.getSortkey();
		if (StringService.isNullOrEmpty(sortkey)) {
			sortkey = "__sort";
			makeSortValues(col, sortkey);
		}
		return getComparator(sortkey, asc);
	}
	
	private void makeSortValues(Col col, String sortkey) {
		list.forEach(map -> map.put(sortkey,
				StringService.umlaute(replaceVars(replaceVars(col.getRowHTML(), global), map))));
	}

	protected Comparator<Map<String, String>> getComparator(String sortkey, boolean asc) {
		return (a, b) -> (asc ? 1 : -1) * a.get(sortkey).compareToIgnoreCase(b.get(sortkey));
	}
}
