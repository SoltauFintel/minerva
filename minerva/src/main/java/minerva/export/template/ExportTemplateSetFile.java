package minerva.export.template;

// No JSON, because HTML is not good readable in encoded JSON format.
public class ExportTemplateSetFile {
	private static final String PREFIX = "~~~=== [";
	private static final String POSTFIX = "]:";
	private StringBuilder sb;

	public String serialize(ExportTemplateSet set) {
		sb = new StringBuilder();
		write("id", set.getId());
		write("name", set.getName());
		write("customer", set.getCustomer());
		write("books", set.getBooks());
		write("book", set.getBook());
		write("page", set.getPage());
		write("template", set.getTemplate());
		write("styles", set.getStyles());
		write("pdf-styles", set.getPdfStyles());
		String ret = sb.toString();
		sb = null;
		return ret;
	}

	private void write(String fieldname, String value) {
		sb.append(PREFIX);
		sb.append(fieldname);
		sb.append(POSTFIX);
		sb.append("\n");
		sb.append(value);
		sb.append("\n");
	}

	public ExportTemplateSet deserialize(String data) {
		ExportTemplateSet set = new ExportTemplateSet();
		String fieldname = "?";
		String value = "";
		int nfields = 0;
		for (String line : data.split("\n")) {
			if (line.startsWith(PREFIX) && line.endsWith(POSTFIX)) {
				if (!"?".equals(fieldname)) {
					set(fieldname, value, set);
					nfields++;
				}
				fieldname = line.substring(PREFIX.length(), line.length() - POSTFIX.length());
				value = "";
			} else {
				value += line + "\n";
			}
		}
		set(fieldname, value, set);
		nfields++;
		if (nfields != 9) {
			throw new RuntimeException("Error parsing export template set file. Not enough fields!");
		}
		return set;
	}

	private void set(String fieldname, String value, ExportTemplateSet set) {
		if (value.endsWith("\n")) {
			value = value.substring(0, value.length() - 1);
		}
		switch (fieldname) {
		case "id":
			set.setId(value);
			break;
		case "name":
			set.setName(value);
			break;
		case "customer":
			set.setCustomer(value);
			break;
		case "books":
			set.setBooks(value);
			break;
		case "book":
			set.setBook(value);
			break;
		case "page":
			set.setPage(value);
			break;
		case "template":
			set.setTemplate(value);
			break;
		case "styles":
			set.setStyles(value);
			break;
		case "pdf-styles":
			set.setPdfStyles(value);
			break;
		default:
			throw new RuntimeException("Unknown field: " + fieldname);
		}
	}
}
