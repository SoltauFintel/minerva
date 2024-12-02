package minerva.base;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.github.template72.compiler.CompiledTemplate;
import com.github.template72.compiler.TemplateCompiler;
import com.github.template72.compiler.TemplateCompilerBuilder;
import com.github.template72.data.DataMap;
import com.github.template72.data.IDataMap;

import gitper.base.StringService;
import minerva.user.UAction;

public class TableComponent extends UAction {
	private final TemplateCompiler compiler = new TemplateCompilerBuilder().build();
	private final String tableCSS;
	private final List<Col> cols;
	private final DataMap model;
	private final String listName;
	private final String sortlink;
	private final String sid;
	private String runVarName = "i";
	private int sortedColumn = -1;
	private boolean asc = false;
	private String html;

	public TableComponent(List<Col> cols, DataMap model, String listName) {
		this("", cols, model, listName);
	}

	public TableComponent(String tableCSS, List<Col> cols, DataMap model, String listName) {
		this.tableCSS = tableCSS;
		this.cols = cols;
		this.model = model;
		this.listName = listName;
		sortlink = TableSortAction.register(this);
		sid = sortlink.replace("/", "");
		cols.forEach(col -> col.template = compiler.compile(col.getRowHTML()));
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
				headers.append("\n<th class=\"" + col.getHeaderCSS() + (ColAlign.RIGHT.equals(col.getAlign()) ? " tar" : "") + "\">");
			} else {
				headers.append("\n<th" + (ColAlign.RIGHT.equals(col.getAlign()) ? " class=\"tar\"" : "")
						+ "><a href=\"#\" class=\"sortlink " + col.getHeaderCSS() + "\" hx-get=\"" + sortlink
						+ i + "\" hx-target=\"." + sid + "\" hx-swap=\"outerHTML\">");
			}
			String content = compiler.compile(col.getHeaderHTML()).render(model);
			headers.append(content);
			String sortIcon = "";
			if (!ColSort.NONE.equals(col.getSort()) && i == sortedColumn) {
				sortIcon = asc ? "fa-arrow-down" : "fa-arrow-up";
			}
			headers.append("<i class=\"fa " + (ColAlign.RIGHT.equals(col.getAlign()) ? "" : "fa-fw ") + sortIcon
					+ " sortarrow\"></i>");
			if (!ColSort.NONE.equals(col.getSort())) {
				headers.append("</a>");
			}
			headers.append("</th>");
		}
		return headers;
	}

	private void makeRows(StringBuilder rows) {
		Comparator<IDataMap> comparator = comparator();
		Stream<IDataMap> stream = model.getList(listName).stream();
		if (comparator != null) {
			stream = stream.sorted(comparator);
		}
		stream.forEach(map -> {
			rows.append("\n\t<tr>");
			model.put(runVarName, map);
			cols.forEach(col -> {
				String content = col.template.render(model);
				rows.append("\n\t\t<td class=\"" + col.getRowCSS() + col.getHeaderCSS()
						+ (ColAlign.RIGHT.equals(col.getAlign()) ? " tar" : "") + "\">" + content + "</td>");
			});
			rows.append("\n\t</tr>");
		});
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
		private final ColAlign align;
		CompiledTemplate template;
		
		public Col(String headerCSS, String headerHTML, String rowCSS, String rowHTML) {
			this(null, ColSort.NONE, headerCSS, headerHTML, rowCSS, rowHTML, ColAlign.LEFT);
		}
		
		private Col(String sortkey, ColSort sort, String headerCSS, String headerHTML, String rowCSS, String rowHTML, ColAlign align) {
			this.sortkey = sortkey;
			this.sort = sort;
			this.headerCSS = headerCSS;
			this.headerHTML = headerHTML;
			this.rowCSS = rowCSS;
			this.rowHTML = rowHTML;
			this.align = align;
		}

		public Col(String headerHTML, String rowHTML) {
			this(null, ColSort.NONE, "", headerHTML, "", rowHTML, ColAlign.LEFT);
		}

		public Col(String headerHTML, String rowCSS, String rowHTML) {
			this(null, ColSort.NONE, "", headerHTML, rowCSS, rowHTML, ColAlign.LEFT);
		}

		public Col sortable(String sortkey) {
			return new Col(sortkey, ColSort.ASC_DESC, headerCSS, headerHTML, rowCSS, rowHTML, align);
		}

		public Col asc(String sortkey) {
			return new Col(sortkey, ColSort.ASC, headerCSS, headerHTML, rowCSS, rowHTML, align);
		}

		public Col desc(String sortkey) {
			return new Col(sortkey, ColSort.DESC, headerCSS, headerHTML, rowCSS, rowHTML, align);
		}

		public Col right() {
			return new Col(sortkey, sort, headerCSS, headerHTML, rowCSS, rowHTML, ColAlign.RIGHT);
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

		public ColAlign getAlign() {
			return align;
		}
	}
	
	public enum ColAlign {
		LEFT, // default
		// CENTER,
		RIGHT;
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
	
	private Comparator<IDataMap> comparator() {
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
		model.getList(listName).forEach(map -> { 
			model.put(runVarName, map);
			String content = col.template.render(model);
			model.put(sortkey, StringService.umlaute(content));
		});
	}

	protected Comparator<IDataMap> getComparator(String sortkey, boolean asc) {
		return (a, b) -> (asc ? 1 : -1) * a.get(sortkey).toString().compareToIgnoreCase(b.get(sortkey).toString());
	}

	public String getRunVarName() {
		return runVarName;
	}

	public TableComponent setRunVarName(String runVarName) {
		this.runVarName = runVarName;
		return this;
	}
}
