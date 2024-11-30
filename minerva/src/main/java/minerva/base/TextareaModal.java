package minerva.base;

import minerva.user.UPage;

// component
public abstract class TextareaModal extends UPage {
	protected static final String FIELD_POSTFIX = "Note";
	
	@Override
	protected void execute() {
		put("modalID", getClass().getSimpleName());
		put("title", n(getTitleKey()));
		put("action", esc(getAction()));
		put("note", esc(getContent()));
		put("fieldID",  getClass().getSimpleName() + FIELD_POSTFIX);
		put("saveID", "s" + getClass().getSimpleName());
		putInt("rows", _rows());
	}
	
	protected abstract String getTitleKey();
	
	protected abstract String getAction();
	
	protected abstract String getContent();
	
	protected int _rows() {
		return 4;
	}
	
	@Override
	protected String getPage() {
		return "TextareaModal";
	}
}
