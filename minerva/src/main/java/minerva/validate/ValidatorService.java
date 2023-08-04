package minerva.validate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.base.NLS;
import minerva.model.SeiteSO;

/**
 * Check if page formatting is fine
 */
public class ValidatorService {

    public List<String> validate(SeiteSO seite, String pageLang, String guiLang) {
        List<String> msg = new ArrayList<>();
        String html = seite.getContent().getString(pageLang);
        if (html == null || html.isBlank()) {
            msg.add("v._emptyHTML");
        } else {
            Document doc = Jsoup.parse(html);
            if (doc.select("body").isEmpty()) {
                msg.add("v.noBody");
            } else {
                Element body = doc.select("body").get(0);
                emptyLinesAtBegin(body, msg);
                emptyLinesAtEnd(body, msg);
                doubleEmptyLines(body, msg);
                headings(body, msg);
            }
        }
        return msg.stream().map(key -> translate(key, guiLang)).collect(Collectors.toList());
    }

    private void emptyLinesAtBegin(Element body, List<String> msg) {
        int emptyLines = 0;
        for (int i = 0; i < body.childrenSize(); i++) {
            if (body.child(i).tagName().equals("p")) {
                if (blank(body.child(i))) {
                    emptyLines++;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        if (emptyLines == 1) {
            msg.add("v.emptyLinesAtBegin1;1");
        } else if (emptyLines > 0) {
            msg.add("v.emptyLinesAtBegin;" + emptyLines);
        }
    }

    private void emptyLinesAtEnd(Element body, List<String> msg) {
        int emptyLines = 0;
        for (int i = body.childrenSize() - 1; i >= 0; i--) {
            if (body.child(i).tagName().equals("p")) {
                if (blank(body.child(i))) {
                    emptyLines++;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        if (emptyLines == 1) {
            msg.add("v.emptyLinesAtEnd1;1");
        } else if (emptyLines > 0) {
            msg.add("v.emptyLinesAtEnd;" + emptyLines);
        }
    }

    private void doubleEmptyLines(Element body, List<String> msg) {
        int emptyLines = 0;
        List<Boolean> para = new ArrayList<>();
        for (int i = 0; i < body.childrenSize(); i++) {
            if (body.child(i).tagName().equals("p")) {
                para.add(blank(body.child(i)));
            }
        }
        int i = para.size() - 1;
        while (i >= 0 && para.get(i).booleanValue()) {
            i--;
        }
        para = para.subList(0, i + 1);
        boolean start = false;
        for (i = 0; i < para.size(); i++) {
            boolean blank = para.get(i).booleanValue();
            if (!start && !blank) {
                start = true;
            } else if (start && i > 0) {
                if (para.get(i - 1).booleanValue() && blank) {
                    emptyLines++;
                }
            }
        }
        if (emptyLines == 1) {
            msg.add("v.doubleEmptyLines1;1");
        } else if (emptyLines > 0) {
            msg.add("v.doubleEmptyLines;" + emptyLines);
        }
    }
    
    private boolean blank(Element p) {
        return p.select("img").isEmpty() ? p.text().isBlank() : false;
    }

    private void headings(Element body, List<String> msg) {
        int prev = -1;
        for (int i = 0; i < body.childrenSize(); i++) {
            Element ci = body.child(i);
            String tag = ci.tagName();
            if (tag.startsWith("h") && !"hr".equals(tag)) {
                int ebene = Integer.parseInt(tag.substring(1));
                if (ebene < 2 || ebene > 6) {
                    msg.add("v.illegalHeading;" + tag);
                } else {
                    boolean hasFormatting = false;
                    for (int j = 0; j < ci.childrenSize(); j++) {
                        Element cj = ci.child(j);
                        if (cj.tagName().equals("span")) {
                            if (cj.attributesSize() == 0) {
                                // ok
                            } else if (cj.attr("class") != null
                                    && cj.attr("class").contains("confluence-anchor-link")) {
                                // ok
                            /*} else {
                                hasFormatting = true;
                                break;*/
                            }
                        } else {
                            hasFormatting = true;
                            break;
                        }
                    }
                    if (hasFormatting && !ci.html().contains("<img")) {
                        msg.add("v.illegalTagsInHeading;" + ci.html().replace(";", ","));
                    }
                    if (prev == -1) {
                        if (ebene != 2) {
                            msg.add("v.firstHeadingMustBe1");
                        }
                    } else {
                        if (prev < ebene - 1) {
                            msg.add("v.headingGap;" + prev + ";" + ebene);
                        }
                    }
                    prev = ebene;
                }
            }
        }
    }

    private String translate(String key, String lang) {
        String ret;
        int o = key.indexOf(";");
        if (o >= 0) {
            String[] params = key.substring(o + 1).split(";");
            key = key.substring(0, o);
            ret = NLS.get(lang, key);
            for (int i = 0; i < params.length; i++) {
                ret = ret.replace("$" + i, Escaper.esc(params[i]));
            }
        } else {
            ret = NLS.get(lang, key);
        }
        return ret;
    }
}
