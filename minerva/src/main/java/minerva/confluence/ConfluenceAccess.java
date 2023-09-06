package minerva.confluence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.rest.REST;
import github.soltaufintel.amalia.rest.RestResponse;

public class ConfluenceAccess {
    private final String baseUrl;
    private final String token;
    private final String spaceKey;
    private final File imagesFolder;
    private final String linkBegin; // protocol + host + port
    
    public ConfluenceAccess(String baseUrl, String token, String spaceKey, File imagesFolder, String linkBegin) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.spaceKey = spaceKey;
        this.imagesFolder = imagesFolder;
        this.linkBegin = linkBegin;
    }

    public List<ConfluencePage2> searchPages() throws IOException {
        List<ConfluencePage2> ret = new ArrayList<>();
        int start = 0;
        final int limit = 500;
        int n;
        do {
            ConfluenceSearchResults p = request("/rest/api/content/search?expand=ancestors&cql=space=" + spaceKey + "+and+type=page+order+by+id&limit=" + limit + "&start=" + start)
                    .fromJson(ConfluenceSearchResults.class);
            n = p.getResults().size();
            for (ConfluenceResult r : p.getResults()) {
                ConfluencePage2 page = new ConfluencePage2();
                page.setId(r.getId());
                page.setTitle(r.getTitle());
                if (r.getAncestors() != null && !r.getAncestors().isEmpty()) {
                    page.setParentId(r.getAncestors().get(r.getAncestors().size() - 1).getId());
                }
                ret.add(page);
            }
            start += n;
        } while (n > 0);
        return ret;
    }
    
    public ConfluencePage2 byTitle(String title, List<ConfluencePage2> pages) {
        ConfluencePage2 root = pages.stream().filter(i -> i.getTitle().equalsIgnoreCase(title)).findFirst().orElse(null);
        if (root == null) {
            throw new RuntimeException("Page with title \"" + title + "\" does not exist!");
        }
        addSubpages(root, pages);
        root.getSubpages().sort((b, a) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
        return root;
    }
    
    public void addSubpages(ConfluencePage2 parent, List<ConfluencePage2> pages) {
        for (ConfluencePage2 page : pages) {
            if (page.getParentId() != null && page.getParentId().equals(parent.getId())) {
                parent.getSubpages().add(page);
                addSubpages(page, pages); // recursive
            }
        }
    }
    
    /**
     * Loads position and HTML. Also loads images and modifies links.
     * @param page -
     * @throws IOException
     */
    public void loadPage(ConfluencePage2 page) throws IOException {
        ConfluenceResult result = request("/rest/api/content/" + page.getId() + "?expand=body.export_view").fromJson(ConfluenceResult.class);
        try {
            page.setPosition(Integer.valueOf(result.getExtensions().getPosition()));
        } catch (NumberFormatException e) {
            page.setPosition(Integer.valueOf(0));
        }
        page.setHtml(result.getBody().getExport_view().getValue());
        processImages(page);
        page.setHtml(replaceLinks(page.getHtml(), page.getId()));
    }

    private void processImages(ConfluencePage2 page) {
        String html = page.getHtml();
        Set<String> images = extract(html, "img", "src");
        for (String img : images) {
            String dn = getShortImageFilename(img);
            if (dn == null) {
                error("[SE-4] Can not extract filename: " + img);
                continue;
            } else {
                downloadImage(img, page.getId(), dn);
                html = html.replace(img, "img/" + dn);
            }
        }
        page.setHtml(html);
    }

    private Set<String> extract(String html, final String tag, final String attr) {
        final String x1 = "<" + tag;
        final String x2 = attr + "=\"";
        Set<String> ret = new HashSet<>();
        int o = html.indexOf(x1);
        while (o >= 0) {
            int oo = html.indexOf(">", o);
            if (oo > o) {
                String img = html.substring(o, oo);
                int p = img.indexOf(x2);
                if (p >= 0) {
                    p += x2.length();
                    int pp = img.indexOf("\"", p);
                    if (pp > p) {
                        String url = img.substring(p, pp);
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            ret.add(url);
                        }
                    }
                }
            }

            o = html.indexOf(x1, o + x1.length());
        }
        return ret;
    }

    private String getShortImageFilename(String img) {
        String dn = img.substring(img.lastIndexOf("/") + 1);
        int o = dn.indexOf("?");
        if (o >= 0) {
            dn = dn.substring(0, o);
        }
        return dn;
    }

    private void downloadImage(String img, String pageId, String shortFilename) {
        File imgFile =  new File(imagesFolder, pageId + "/" + shortFilename);
        imgFile.getParentFile().mkdirs();
        if (imgFile.isFile()) {
            return;
        }
        
        String url = fixBrokenUrl(pageId, img);
        try (FileOutputStream fos = new FileOutputStream(imgFile)) {
            RestResponse req = request(url);
            req.getHttpResponse().getEntity().writeTo(fos);
            req.close();
        } catch (Exception e) {
            error("[SE-3] Can not download file: " + img + " | " + e.getMessage() + " | " + pageId);
        }
    }
    
    // Workaround for broken URL
    private String fixBrokenUrl(String pageId, String url) {
        if (url.startsWith(baseUrl + "/download/attachments/embedded-page/" + spaceKey + "/"/* ... */)) { // wrong URL
            int lastSlash = url.lastIndexOf("/");
            if (lastSlash >= 0) {
                String dn = url.substring(lastSlash);
                url = baseUrl + "/download/attachments/" + pageId + dn;
            }
        }
        return url;
    }

    private String replaceLinks(String html, String pageId) {
        Set<String> refs = extract(html, "a", "href");
        final String x = linkBegin + "/pages/viewpage.action?pageId=";
        final String x2 = linkBegin + "/display/";
        for (String href : refs) {
            if (href.startsWith(x)) {
                String aPageId = href.substring(x.length());
                html = html.replace(href, "/html/" + aPageId + ".html");
            } else if (href.startsWith(x2)) {
                html = html.replace(href, "#"); // clear that link (Datenschutz)
                System.out.println("hyperlink " + href + " was changed to \"#\" | " + pageId);
            } else {
                System.out.println("hyperlink " + href + " was not replaced | " + pageId);
            }
        }
        return html;
    }
    
    private RestResponse request(String url) {
        return new REST(baseUrl + url)
                .withAuthorization("Bearer " + token)
                .get();
    }
 
    private void error(String msg) {
        Logger.error(msg);
    }
}
// DOKU:
// https://docs.atlassian.com/ConfluenceServer/rest/6.13.0/#api/content-getContent
// https://community.atlassian.com/t5/Confluence-questions/how-to-get-full-content-body-html-using-confluence-REST-API/qaq-p/710532
// https://stackoverflow.com/a/34363386/19904503
