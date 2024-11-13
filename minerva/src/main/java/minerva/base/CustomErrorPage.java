package minerva.base;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.spark.Context;
import gitper.base.StringService;
import minerva.user.UPage;

public class CustomErrorPage extends UPage {
	private static final Map<String, CustomerErrorMessage> errors = new ConcurrentHashMap<>();
	
	public static void showErrorPage(String msg, String continueLink, Context ctx) {
		String id = IdGenerator.createId6();
		CustomerErrorMessage error = new CustomerErrorMessage(msg, continueLink);
		errors.put(id, error);
		ctx.redirect("/error?id=" + id);
	}
	
	public static void clear() {
		if (!errors.isEmpty()) {
			errors.clear();
			Logger.debug("All custom error messages were deleted.");
		}
	}
	
	@Override
	protected void execute() {
		String id = ctx.queryParam("id");
		
		CustomerErrorMessage error = errors.get(id);
		String msg = error == null ? "Can't show error message because error ID " + id + " is unknown." : error.getMsg();
		Logger.error(msg);
		
		header(n("message"));
		put("msg", esc(msg).replace("\n", "\n<br/>"));
		put("continueLink", esc(error == null || StringService.isNullOrEmpty(error.getContinueLink()) ? "/" : error.getContinueLink()));
	}
	
	private static class CustomerErrorMessage {
		private final String msg;
		private final String continueLink;

		CustomerErrorMessage(String msg, String continueLink) {
			this.msg = msg;
			this.continueLink = continueLink;
		}

		public String getMsg() {
			return msg;
		}

		public String getContinueLink() {
			return continueLink;
		}
	}
}
