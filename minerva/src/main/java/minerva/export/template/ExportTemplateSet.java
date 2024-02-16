package minerva.export.template;

public class ExportTemplateSet {
    private String id;
    private String name;
    private String customer = "";
    // HTML----
    private String books;
    private String book;
    private String page;
    private String template;
    private String styles;
    // PDF----
    private String pdfToc; // cover and table of contents pages
    private String pdfStyles;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getBooks() {
        return books;
    }

    public void setBooks(String books) {
        this.books = books;
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getStyles() {
        return styles;
    }

    public void setStyles(String styles) {
        this.styles = styles;
    }

    public String getPdfToc() {
        return pdfToc;
    }

    public void setPdfToc(String pdfToc) {
        this.pdfToc = pdfToc;
    }

    public String getPdfStyles() {
        return pdfStyles;
    }

    public void setPdfStyles(String pdfStyles) {
        this.pdfStyles = pdfStyles;
    }
}
