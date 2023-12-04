package minerva.export;

public class ExportUserSettings {
    private String item;
    private String customer;
    private String lang;
    private String format;
    private String template;
    private boolean cover = true;
    private boolean toc = true;
    private boolean chapters = true;
    
    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

    public boolean isCover() {
        return cover;
    }

    public void setCover(boolean cover) {
        this.cover = cover;
    }

    public boolean isToc() {
        return toc;
    }

    public void setToc(boolean toc) {
        this.toc = toc;
    }

    public boolean isChapters() {
        return chapters;
    }

    public void setChapters(boolean chapters) {
        this.chapters = chapters;
    }
}
