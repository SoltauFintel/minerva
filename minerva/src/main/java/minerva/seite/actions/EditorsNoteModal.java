package minerva.seite.actions;

import minerva.base.TextareaModal;
import minerva.model.SeiteSO;

// component
public class EditorsNoteModal extends TextareaModal {
	public static final String ID = EditorsNoteModal.class.getSimpleName() + FIELD_POSTFIX;
	private final SeiteSO seite;
	
	public EditorsNoteModal(SeiteSO seite) {
		this.seite = seite;
	}
	
	@Override
	protected String getTitleKey() {
		return "editorsNote";
	}

	@Override
	protected String getAction() {
		return seite.viewlink() + "/editorsnote"; // SaveEditorsNoteAction
	}

	@Override
	protected String getContent() {
		return seite.getSeite().getEditorsNote();
	}
}
