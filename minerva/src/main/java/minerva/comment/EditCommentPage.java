package minerva.comment;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.web.action.Page;
import gitper.base.StringService;
import minerva.base.NLS;
import minerva.model.StatesSO;
import minerva.postcontents.PostContentsService;
import minerva.user.UPage;
import minerva.user.UserAccess;

/**
 * Edit or add comment
 */
public class EditCommentPage extends Page {

	@Override
	protected void execute() {
		String id = ctx.queryParam("id"); // GET: id is missing id add case, POST: id is always set
		String parentId = ctx.queryParam("parent");
		CommentService sv = CommentService.service(ctx);

		boolean add = "add".equals(ctx.queryParam("m"));
		if (isPOST()) {
			save(id, add, parentId, sv);
			ctx.redirect(sv.getCommentsPagePath());
		} else {
			put("ckeditor", true); put("jstree", true); // ckeditor()
			display(id, parentId, sv, add);
		}
	}

	private void display(String id, String parentId, CommentService sv, boolean add) {
		boolean hasParentId = !StringService.isNullOrEmpty(parentId);
		Comment c;
		if (add /* true in case of server restart */ && !StringService.isNullOrEmpty(id)) {
			c = new Comment();
			c.setId(id);
		} else {
			add = StringService.isNullOrEmpty(id);
			if (add) {
				c = new Comment();
				c.setId(IdGenerator.createId6());
			} else {
				c = sv.get(id);
			}
		}
		Comment parent = null;
		boolean checked = true;
		if (hasParentId) {
			parent = sv.get(parentId);
			if (add && parent != null) {
				if (parent.getUser().equals(StatesSO.get(ctx).getUser().getLogin())) { // It's me?
					checked = false;
				} else {
					c.setPerson(parent.getUser());
				}
			}
		}
		
		put("id", c.getId());
		putInt("version", c.getVersion());
		put("content", c.getText());
		combobox("persons", UserAccess.getUserNames(), UserAccess.login2RealName(c.getPerson()), true);

		put("add", add);
		put("checked", checked);
		put("backlink", sv.getCommentsPagePath());
		put("lang", sv.getLanguage());
		String teil2 = hasParentId ? "&parent=" + parentId : "";
		String teil3 = add ? "&m=add" : "";
		String action = ctx.path() + "?id=" + c.getId() + teil2 + teil3;
		put("action", action);
		put("hasParent", hasParentId);
		// editor component >>
		put("postExtra", "person: document.getElementById('person').value,");
		put("postFailExtra", "");
		put("errorName", "comment");
		put("onloadExtra", "");
		// <<
		sv.initModel(model);

		String headerId = add ? "addComment" : "editComment";
		if (parent != null) {
			headerId = "answerComment";
			put("parentText", "<div class=\"reply\">" //
					+ "<p>" + NLS.get(sv.getLanguage(), "comment-by") + " <b>" + UserAccess.login2RealName(parent.getUser()) + "</b>:</p>" //
					+ parent.getText() + "</div>");
		} else {
			put("parentText", "");
		}
		String header = NLS.get(sv.getLanguage(), headerId);
		put("header", header);
		put("title", header + " - " + model.get("parentEntityTitle").toString() // after initModel!
				+ UPage.TITLE_POSTFIX);
	}

	private void save(String id, boolean add, String parentId, CommentService sv) {
		int version = Integer.parseInt(ctx.formParam("version"));
		CommentPCD data = (CommentPCD) new PostContentsService().waitForContents(sv.getKey(id), version);

		saveComment(id, add, parentId, sv, data);
	}

	private void saveComment(String id, boolean add, String parentId, CommentService sv, CommentPCD data) {
		Comment c;
		if (add) {
			c = new Comment();
			c.setId(id);
		} else {
			c = sv.get(id);
		}
		String newPerson = UserAccess.realName2Login(data.getPerson());
		boolean changed = add || !newPerson.equals(c.getPerson()) || !data.getText().equals(c.getText());
		c.setPerson(newPerson);
		c.setText(data.getText());
		if (add) {
			c.setUser(sv.getLogin());
			c.setCreated(StringService.now());
			c.setParentId(parentId);
			sv.save(c, "comment added", add, changed, "on".equals(ctx.formParam("done")) ? parentId : null);
		} else {
			c.setChanged(StringService.now());
			c.setVersion(c.getVersion() + 1);
			sv.save(c, "comment modified", add, changed, null);
		}
	}
}
