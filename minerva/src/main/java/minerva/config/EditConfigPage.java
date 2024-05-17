package minerva.config;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.user.UPage;

public class EditConfigPage extends UPage {

	@Override
	protected void execute() {
		if (!isAdmin) {
			throw new RuntimeException("Config page only accessible by admin");
		}
		if (isPOST()) {
			save();
		} else {
			Logger.info(user.getLogin() + " | Config page");
			display();
		}
	}

	private void display() {
		header("Configuration");
		DataList list = list("categories");
		for (OptionCategory cat : MinervaOptions.options.getCategories()) {
			DataMap map = list.add();
			map.put("label", esc(cat.getLabel()));
			DataList list2 = map.list("options");
			for (Option o : cat.getOptions()) {
				DataMap m = list2.add();
				m.put("key", esc(o.getKey()));
				m.put("value", esc(o.get()));
				m.put("label", esc(o.getLabel()));
				m.putHas("hint", o.getHint());
				m.put("hint", esc(o.getHint()));
				m.put("isTextarea", OptionType.TEXTAREA.equals(o.getType()));
			}
		}
	}

	private void save() {
		MinervaOptions options = MinervaOptions.options;
		for (OptionCategory cat : options.getCategories()) {
			for (Option o : cat.getOptions()) {
				String backup = o.get();
				o.set(ctx.formParam(o.getKey()));
				try {
					o.validate();
				} catch (Exception up) {
					o.set(backup); // restore
					throw up;
				}
			}
		}
		options.save();
		ctx.redirect("/");
	}
}
