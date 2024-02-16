package minerva.image;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.pmw.tinylog.Logger;

import minerva.base.FileService;
import minerva.base.NlsString;
import minerva.model.BookSO;
import minerva.model.SeiteSO;

public class FixHttpImage {

    /**
     * Look if HTML contains http images. Then fix that by downloading the images,
     * uploading them and edit the HTML.
     */
    public void process(NlsString html, List<String> langs, List<String> seiteImages, BookSO book, String seiteId) {
        String hardErrors = "";
        try {
            Set<String> images = getAbsoluteUrlImages(html, langs, false);
            if (!images.isEmpty()) {
                Logger.debug(seiteId + " | detected http images: " + images.size());
            }
            for (String url : images) {
                int o = url.lastIndexOf("/");
                if (o >= 0) {
                    String dn = "img/" + seiteId + "/" + FileService.getSafeName(url.substring(o + 1));
                    File file = new File(book.getFolder(), dn);
                    try {
                        FileUtils.copyURLToFile(new URL(url), file, 5 * 1000, 5 * 1000);
                        Logger.debug(seiteId + " | downloaded image: " + url + " to file " + file.getAbsolutePath());
                    } catch (IOException e) {
                        Logger.warn(seiteId + " | Download of http image \"" + url + "\" failed! Skip image. Error message: " + e.getMessage());
                        continue; // skip file
                    }
                    if (file.length() > 1024l * 1024 * 10) {
                        Logger.warn("image too large: " + file.length());
                        file.delete();
                        hardErrors += "Image " + url + " is too large. Must be less than 10 MB. Please fix page #" + seiteId + "!\n"; // throw after loop so other images will be processed
                        continue; // skip
                    }
                    for (String lang : langs) {
                        html.setString(lang, html.getString(lang).replace(url, dn));
                    }
                    Logger.info(seiteId + " | changed image src from \"" + url + "\" to \"" + dn + "\"");
                    seiteImages.add(dn);
                } // else: just skip
            }
        } catch (Exception e) { // Saving html should not be cancelled if an unexpected error occur.
            Logger.warn(e);
        }
        if (!hardErrors.isEmpty()) {
            book.getUser().getJournal().save(book.getWorkspace().getBranch(), seiteId, null, html);
            throw new RuntimeException(hardErrors);
            // The user should remove image from page because we would always run again into this error.
        }
    }
    
    public boolean hasAbsoluteUrlImage(SeiteSO seite, List<String> langs) {
        return !getAbsoluteUrlImages(seite.getContent(), langs, true).isEmpty();
    }

    private Set<String> getAbsoluteUrlImages(NlsString content, List<String> langs, boolean returnOne) {
        Set<String> ret = new HashSet<>();
        for (String lang : langs) {
            String html = content.getString(lang);
            int o = html.indexOf("<img");
            while (o >= 0) {
                o += "<img".length();
                int oo = html.indexOf(">", o);
                if (oo >= 0) {
                    int z = html.indexOf("src=\"", o);
                    if (z >= o && z < oo) {
                        z += "src=\"".length();
                        int zz = html.indexOf("\"", z);
                        if (zz >= z && zz < oo) {
                            String src = html.substring(z, zz);
                            if (src.startsWith("http://") || src.startsWith("https://")) {
                                ret.add(src);
                                if (returnOne) {
                                    return ret;
                                }
                            }
                        }
                    }
                }
                o = html.indexOf("<img", o);
            }
        }
        return ret;
    }
}
