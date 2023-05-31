package minerva.base;

import org.pmw.tinylog.Logger;

import minerva.user.UPage;

public class MessagePage extends UPage {

	@Override
	protected void execute() {
		int m = Integer.parseInt(ctx.queryParam("m"));
		
		header(n("message"));
		put("p", "");
		put("link", "/");
		put("linkTitle", n("goToHomepage"));

		switch (m) {
		case 1:
			put("h2", n("pageNotFound"));
			put("p", n("pageNotFound2"));
			break;
		default:
			Logger.error("[MessagePage] unsupported m value: " + m);
			put("h2", "Sorry, no message text for this message.");
		}
	}
}
