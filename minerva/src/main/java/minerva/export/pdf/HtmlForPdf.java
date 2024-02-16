package minerva.export.pdf;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.pmw.tinylog.Logger;

import minerva.seite.link.Link;
import minerva.seite.link.LinkService;

public class HtmlForPdf {
    public static boolean check = false;

    private HtmlForPdf() {
    }
    
    public static String processHtml(String html, String doctype, String info, String imageBaseDir, List<String> errorMessages) {
        while (html.startsWith("\n") || html.startsWith(" ")) {
            html = html.substring(1);
        }
        while (html.endsWith("\n") || html.endsWith(" ")) {
            html = html.substring(0, html.length() - 1);
        }
        if ("<p>.</p>".equals(html)) { // suppress nearly-empty release note page
            html = "";
        }
        html = toXHTML(html);
        if (!checkHtml(doctype, html, info)) {
            return null;
        }
        html = modifyLinks(html, info, errorMessages);
        html = images(html, info, imageBaseDir, errorMessages);
        return html;
    }
    
    private static String toXHTML(String html) {
        html = html.replace("<col>\n", "<col/>\n");
        html = closeTag(html, "<img");
        html = closeTag(html, "<br");
        html = closeTag(html, "<col ");
        return html;
    }

    private static String closeTag(String html, String tag) {
        int o = html.indexOf(tag);
        while (o >= 0) {
            int oo = html.indexOf(">", o);
            if (oo > o) {
                html = html.substring(0, oo) + "/" + html.substring(oo);
            }

            o = html.indexOf(tag, o + tag.length() + 1);
        }
        return html;
    }

    /**
     * Die einzelnen Seiten separat prüfen, um herauszufinden wo es noch broken HTML gibt.
     * Das HTML muss dann in toXHTML() korrigiert werden.
     */
    private static boolean checkHtml(String doctype, String html, String info) {
        if (!check) {
            return true;
        }
        try {
            File file = Files.createTempFile("Minerva-PDF-", ".pdf").toFile();
            new PdfWriter().writePDF(doctype + html, false, file);
            return true;
        } catch (Exception e) {
            Logger.error(info + " will be excluded from PDF export because of error: " + e.getMessage());
            return false;
        }
    }
    
    private static String modifyLinks(String html, String info, List<String> errorMessages) { // similar to MultiPageHtmlExportService.addDotHtml()
        List<Link> links = LinkService.extractLinks(html, false);
        for (Link link : links) {
            String href = link.getHref();
            if (!href.startsWith("http://") && !href.startsWith("https://")) {
                int o = href.lastIndexOf("#");
                if (o >= 0) {
                    errorMessages.add(info + ": anchor not supported. Just link to page. link: " + href);
                    href = href.substring(0, o);
                }
                html = html.replace("<a href=\"" + href + "\">", "<a href=\"#" + href + "\">");
            }
        }
        return html;
    }

    /**
     * Replace img src to absolute paths.
     */
    private static String images(String html, String info, String imageBaseDir, List<String> errorMessages) {
        int o = html.indexOf("<img");
        while (o >= 0) {
            int diff = 0;
            int o1 = html.indexOf("src=\"", o);
            if (o1 > o) {
                o1 += "src=\"".length();
                int o2 = html.indexOf("\"", o1);
                if (o2 > o1) {
                    String src = html.substring(o1, o2);
                    if (!src.startsWith("http")) {
                        File f = new File(imageBaseDir, src);
                        String newSrc = "file:///" + f.getAbsolutePath().replace("\\", "/");
                        html = html.substring(0, o1) + newSrc + html.substring(o2);
                        diff = newSrc.length() - src.length();
                    } else {
                        errorMessages.add(info + " has an image with http URL: " + src);
                    }
                }
            }

            o = html.indexOf("<img", o + "<img".length() + diff);
        }
        return html;
    }
}
