package minerva.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import gitper.base.FileService;
import minerva.seite.Seite;

public class Export4KI {
	private final Map<String, Seite> seiten = new HashMap<>();

	public static void main(String[] args) {
		new Export4KI().start("C:\\projects\\git-repos\\manual");
		System.out.println("fertig");
	}

	void start(String dir) {
		File[] files = new File(dir).listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.getName().equals("featuretree")) {
					continue;
				}
				File de = new File(file, "de");
				if (de.isDirectory()) {
					export(de);
				}
				File en = new File(file, "en");
				if (en.isDirectory()) {
					export(en);
				}
			}
		}
	}

	private void export(File dir) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.getName().endsWith(".html")) {
					export2(dir, file);
				}
			}
		}
	}

	private void export2(File dir, File file) {
		File meta = new File(file.getParentFile().getParentFile(), file.getName().replace(".html", ".meta"));
		Seite seite = seiten.get(meta.getName());
		if (seite == null) {
			seite = FileService.loadJsonFile(meta, Seite.class);
			seiten.put(file.getName(), seite);
		}
		String title = seite == null ? "" : seite.getTitle().getString(dir.getName());

		String html = FileService.loadPlainTextFile(file);
		HtmlParser h = new HtmlParser(html, true, false);

		export3(title, h.getText(), file, dir.getName());
	}

	private void export3(String title, String text, File file, String lang) {
		if (text.isBlank()) {
			return;
		}
		File f = new File("outki/" + lang + "/" + file.getName().replace(".html", ".txt"));
		f.getParentFile().mkdirs();
		FileService.savePlainTextFile(f, title + "\r\n" + text + "\r\n");

		f = new File("outki/" + lang + ".txt");
		try (FileWriter fos = new FileWriter(f, true)) {
			fos.write(title);
			fos.write("\r\n");
			fos.write(text);
			fos.write("\r\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
