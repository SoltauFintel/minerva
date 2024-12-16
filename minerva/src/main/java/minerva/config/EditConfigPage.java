package minerva.config;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.MinervaWebapp;
import minerva.user.UPage;

public class EditConfigPage extends UPage {
    private int n;

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
		boolean customerVersion = MinervaWebapp.factory().isCustomerVersion();
		header("Configuration");
		DataList list = list("categories");
		n = 1;
		MinervaOptions.options.getCategories().stream().sorted((a, b) -> a.sort().compareTo(b.sort())).forEach(cat -> {
			DataMap map = list.add();
			map.put("id", "cat" + n++);
			map.put("label", esc(cat.getLabel()));
			map.put("color", cat.getColor());
			
			DataList list2 = map.list("options");
			for (Option o : cat.getOptions()) {
				if (!customerVersion || !o.isNotForCustomerVersion()) {
					DataMap m = list2.add();
					m.put("key", esc(o.getKey()));
					m.put("value", esc(o.get()));
					m.put("label", esc(o.getLabel()));
					m.putHas("hint", o.getHint());
					m.put("hint", esc(o.getHint()));
					m.put("isTextarea", OptionType.TEXTAREA.equals(o.getType()));
					m.put("isSecret", OptionType.SECRET.equals(o.getType()));
				}
			}
			if (list2.isEmpty()) {
				list.remove(list.size() - 1);
			}
		});
	}

	private void save() {
		boolean customerVersion = MinervaWebapp.factory().isCustomerVersion();
		MinervaOptions options = MinervaOptions.options;
		for (OptionCategory cat : options.getCategories()) {
			for (Option o : cat.getOptions()) {
				if (!customerVersion || !o.isNotForCustomerVersion()) {
					o.set(ctx.formParam(o.getKey()));
				}
			}
		}
		options.save();

		if (user.getCurrentWorkspace() == null) {
			ctx.redirect("/");
		} else {
			ctx.redirect("/w/" + user.getCurrentWorkspace().getBranch() + "/menu");
		}
	}
}
