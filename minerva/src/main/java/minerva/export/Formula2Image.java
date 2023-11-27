package minerva.export;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.model.SeiteSO;

public class Formula2Image {
    private int counter = 0;
    private String path;

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String processHTML(String html, String prefix, String postfix,
            File outputFolder, SeiteSO seite, String prefixHtml, String postfixHtml, String title, TransformPath tp) {
        Map<String, String> replaceMap = new HashMap<>();
        int o = html.indexOf(prefix);
        while (o >= 0) {
            int oo = html.indexOf(postfix, o + prefix.length());
            if (oo > o) {
                String key = html.substring(o, oo + postfix.length());
                String expression = html.substring(o + prefix.length(), oo);
                
                File file = expression2ImageFile(expression, outputFolder, title);
                
                String img = "<img src=\"" + tp.transform(path, file) + "\" class=\"math\"/>";
				replaceMap.put(key, prefixHtml + img + postfixHtml);
            }

            o = html.indexOf(prefix, oo + postfix.length());
        }
        for (Entry<String, String> e : replaceMap.entrySet()) {
            html = html.replace(e.getKey(), e.getValue());
        }
        return html;
    }
    
    public File expression2ImageFile(String expression, File outputFolder, String title) {
        File file = new File(outputFolder, path + "formula-" + ++counter + ".png");
        file.getParentFile().mkdirs();

        // Cache
        String key = Base64.getEncoder().encodeToString(expression.getBytes()); // expression to safe name
        File cacheFile = new File(MinervaWebapp.factory().getWorkFolder("export/formula-image-cache"), key + ".png");
        if (cacheFile.isFile()) {
            try {
                Files.copy(cacheFile.toPath(), file.toPath());
                Logger.debug("using math formula image from cache: " + cacheFile.getAbsolutePath()
                    + " for expression: " + expression);
                return file;
            } catch (IOException e) {
                Logger.error(e);
                // continue: create image...
            }
        }
        
        // Create image
        String url = MinervaWebapp.factory().getConfig().getMathJaxConverterURL(expression);
        Logger.debug("downloading math formula image from " + url);
        try {
            FileUtils.copyURLToFile(new URL(url), file);
            Logger.debug(file.toString());
        } catch (IOException e) {
            throw new RuntimeException("Error creating math formula image:\n" + expression + "\npage: " + title, e);
        }
        
        // save image to cache
        try {
            cacheFile.getParentFile().mkdirs();
            Files.copy(file.toPath(), cacheFile.toPath());
        } catch (IOException e) {
            Logger.error(e);
        }
        return file;
    }
    
    public interface TransformPath {
    	String transform(String path, File file);
    }
}
