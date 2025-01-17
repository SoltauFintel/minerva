package minerva.validate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.Escaper;
import gitper.access.CommitMessage;
import minerva.MinervaWebapp;
import minerva.base.AbstractTimer;
import minerva.base.CustomErrorPage;
import minerva.base.NLS;
import minerva.base.TextService;
import minerva.base.Timer;
import minerva.config.MinervaOptions;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.StatesSO;
import minerva.model.UserSO;
import minerva.model.WorkspaceSO;
import minerva.seite.link.Link;
import minerva.seite.link.LinkService;
import minerva.validate.ValidationResult.VRLink;
import minerva.validate.ValidationResult.VRSeite;
import minerva.validate.ValidationResult.VRUnusedImageSeite;

/**
 * Check if page formatting is fine
 */
public class ValidatorService {

	public ValidationResult start(BookSO book, List<String> langs, String userGuiLanguage) {
		ValidationResult result = new ValidationResult();
		List<SeiteSO> alleSeiten = book.getAlleSeiten();
		for (String lang : langs) {
			for (SeiteSO seite : alleSeiten) {
		        List<String> msg = validate(seite, lang, userGuiLanguage);
		        if (!msg.isEmpty()) {
		            result.getSeiten().add(new VRSeite(seite, lang, msg));
		        }
		        
				extractLinks(seite, lang, result);
			}
		}
		for (int i = 0; i < alleSeiten.size(); i++) {
			SeiteSO seite1 = alleSeiten.get(i);

			unusedImageFiles(seite1, langs, result, null);

			for (int j = 0; j < i; j++) {
				SeiteSO seite2 = alleSeiten.get(j);
				for (String lang : langs) {
					String title1 = seite1.getSeite().getTitle().getString(lang);
					String title2 = seite2.getSeite().getTitle().getString(lang);
					if (title1.equals(title2)) {
						result.sameTitle(lang + ":" + title1, seite1, seite2);
					}
				}
			}
		}
		return result;
	}
	
    private List<String> validate(SeiteSO seite, String pageLang, String guiLang) {
        List<String> msg = new ArrayList<>();
        String html = seite.getContent().getString(pageLang);
        if (html == null || html.isBlank()) {
            if (!seite.isFeatureTree()) {
                msg.add("v._emptyHTML");
            }
        } else {
            Document doc = Jsoup.parse(html);
            if (doc.select("body").isEmpty()) {
                if (!seite.isFeatureTree()) {
                    msg.add("v.noBody");
                }
            } else {
                Element body = doc.select("body").get(0);
                emptyLinesAtBegin(body, msg);
                emptyLinesAtEnd(body, msg);
                doubleEmptyLines(body, msg);
                headings(body, msg);
                missingImageFiles(seite, html, msg);
                brokenLocalAnchors(body, msg);
                colors(body, msg);
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

    private void missingImageFiles(SeiteSO seite, String html, List<String> msg) {
        String bookFolder = seite.getBook().getFolder();
        Set<String> imgSources = TextService.findHtmlTags(html, "img", "src");
        for (String src : imgSources) {
            if (src.startsWith("http://") || src.startsWith("https://")) {
                msg.add("v.hasAbsoluteUrlImage;" + src);
            } else {
                File file = new File(bookFolder, src);
                if (!file.isFile()) {
                    Logger.debug("Missing image file: " + file.getAbsolutePath());
                    msg.add("v.missingImageFile;" + src);
                }
            }
        }
    }

    private void brokenLocalAnchors(Element body, List<String> msg) {
        for (Element a : body.select("a")) {
            String href = a.attr("href");
            if (!"#".equals(href.trim()) && href.startsWith("#")) {
                String target = href.substring(1).trim();
                if (!findHeading(target, body)) {
                    msg.add("v.brokenLocalAnchor;" + target.replace(";", ",") + ";" + a.wholeOwnText().trim().replace(";", ","));
                }
            }
        }
    }
    
    private boolean findHeading(String heading, Element body) {
        for (Element h2 : body.select("h2,h3,h4,h5,h6")) {
            if (h2.wholeOwnText().trim().equals(heading)) {
                return true;
            }
        }
        return false;
    }

	private void colors(Element body, List<String> msg) {
		Set<String> colors = new TreeSet<>();
		List<String> allowedColors = List.of("rgb(0,0,255)", "rgb(153,204,0)");
		Elements elementsWithStyle = body.select("[style]");
		for (Element element : elementsWithStyle) {
			String style = element.attr("style");
			if (style.contains("-webkit")) {
				msg.add("v.webkit");
				return;
			} else {
				String sl = style.toLowerCase();
				if (sl.contains("color") || style.contains("#") || sl.contains("rgb") || sl.contains("hsl")) {
					for (String pair : style.split(";")) {
						if (pair.startsWith("color:") || pair.startsWith("background-color:")) {
							String[] keyAndValue = pair.split(":");
							if (keyAndValue.length == 2) {
								String color = keyAndValue[1].replace(" ", "");
								if (!allowedColors.contains(color)) {
									colors.add(color);
								}
							}
						}
					}
				}
			}
		}
		for (String color : colors) {
			msg.add("v.colors;" + color);
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

    public String removeEmptyLinesAtEnd(String html) {
        Document doc = Jsoup.parse(html);
        Element body = doc.select("body").get(0); // There's always a body!
        boolean dirty = false;
        for (int i = body.childrenSize() - 1; i >= 0; i--) {
            if (body.child(i).tagName().equals("p")) {
                if (blank(body.child(i))) {
                    body.child(i).remove();
                    dirty = true;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return dirty ? doc.toString() : html;
    }

	public void unusedImageFiles(SeiteSO seite, List<String> langs, ValidationResult result, Set<String> filesToBeDeleted) {
		String folder = seite.getBook().getFolder() + "/img/" + seite.getId();
		Set<String> filenames = seite.getBook().dao().getFilenames(folder);
		if (filenames != null) {
			boolean first = true;
			List<String> unusedImages = null;
			for (String dn : filenames) {
				if (!hasImage(seite, langs, dn)) {
					if (result == null) { // timer mode
						filesToBeDeleted.add(folder + "/" + dn);
					} else { // GUI mode
						if (first) {
							first = false;
							VRUnusedImageSeite uis = new VRUnusedImageSeite(seite);
							result.getUnusedImages().add(uis);
							unusedImages = uis.getUnusedImages();
						}
						unusedImages.add(dn);
					}
				}
			}
		}
	}
	
	private boolean hasImage(SeiteSO seite, List<String> langs, String dn) {
		for (String lang : langs) {
			String html = seite.getContent().getString(lang);
			// old -> if (html.contains("\"img/" + seite.getId() + "/" + dn + "\"")) {
			if (html.contains(dn)) {
				return true;
			}
		}
		return false;
	}
    
    /**
     * Delete unused images e.g. every day 23:00
     */
    public static class UnusedImagesTimer extends AbstractTimer {
        
    	/**
    	 * @return null if timer should not be started, otherwise cron expression
    	 */
        public static String cron() {
        	boolean start = false;
        	if (MinervaWebapp.factory().isCustomerVersion()) {
        		start = MinervaOptions.CLEANUP_CRON.isSet();
        	} else if (Timer.checkIfTimersAreActive(UnusedImagesTimer.class)) {
    			start = MinervaOptions.CLEANUP_LOGIN.isSet()
    					&& MinervaOptions.CLEANUP_PASSWORD.isSet()
    					&& MinervaOptions.CLEANUP_BRANCHES.isSet()
    					&& MinervaOptions.CLEANUP_CRON.isSet();
    			if (!start) {
    				Logger.info("No UnusedImagesTimer started because 'Cleanup service' options are not set.");
    			}
        	}
        	return start ? MinervaOptions.CLEANUP_CRON.get() : null;
        }
        
        @Override
        protected void timerEvent() {
            CustomErrorPage.clear();
            DeleteUnusedImages.start();
            RemoveStyleAttributesService.start();
        }
    }
    
    public static class DeleteUnusedImages {
        
        public static void start() {
			UserSO userSO = StatesSO.login(); // TODO Das funktioniert nicht für customerVersion!
            List<String> langs = MinervaWebapp.factory().getLanguages();
            Set<String> filesToBeDeleted = new TreeSet<>();
            for (String branch : MinervaOptions.CLEANUP_BRANCHES.get().split(",")) {
                branch = branch.trim();
                Logger.debug("- branch: " + branch);
                WorkspaceSO workspace = userSO.getWorkspace(branch);
                if (workspace.getBooks() == null) {
                    Logger.error("workspace.getBooks() is null");
                    throw new RuntimeException("Can not delete unused images! Branch '" + branch + "' does not exist. Please check cleanup configuration!");
                }
                for (BookSO book : workspace.getBooks()) {
                    Logger.debug("-- book folder: " + book.getBook().getFolder());
                    for (SeiteSO seite : book.getAlleSeiten()) {
                        new ValidatorService().unusedImageFiles(seite, langs, null, filesToBeDeleted);
                    }
                }
                if (filesToBeDeleted.isEmpty()) {
                    Logger.info(branch + " | No unused images found.");
                } else {
                    for (String dn : filesToBeDeleted) {
                        Logger.debug(branch + " | image to be deleted: " + dn);
                    }
                    List<String> cantBeDeleted = new ArrayList<>();
                    userSO.dao().deleteFiles(filesToBeDeleted, new CommitMessage("Delete unused images"), workspace, cantBeDeleted);
                    if (!cantBeDeleted.isEmpty()) {
                        Logger.error(branch + " | Error deleting files: " + cantBeDeleted);
                    } else {
                        Logger.info(branch + " | Deleted unused images: " + filesToBeDeleted.size());
                    }
                }
            }
            Logger.debug("UnusedImagesTimer | end");
        }
    }

	private void extractLinks(SeiteSO seite, String lang, ValidationResult result) {
        String html = seite.getContent().getString(lang);
        List<Link> xlinks = LinkService.extractLinks(html, true);
        for (Link link : xlinks) {
            if (link.getHref().startsWith("http://") || link.getHref().startsWith("https://")) {
            	result.getLinks().add(new VRLink(lang, link, seite));
            }
        }
    }
}
