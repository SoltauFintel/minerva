package minerva.user;

public class TakeQuickbuttonAction extends UAction {
	
	@Override
	protected void execute() {
		String path = ctx.queryParam("p");
		String title = ctx.queryParam("t");

		Quickbutton qb = new Quickbutton();
		qb.setLabel(title);
		qb.setLink(path);
		user.getQuickbuttons().add(qb);
		user.saveQuickbuttons();
		
		ctx.redirect("/q/config");
	}
}
