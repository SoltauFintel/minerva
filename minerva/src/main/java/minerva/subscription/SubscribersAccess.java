package minerva.subscription;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import com.google.gson.Gson;

import github.soltaufintel.amalia.rest.REST;
import minerva.MinervaWebapp;

public class SubscribersAccess {
    private final List<String> subscribers = new ArrayList<>();
    private final boolean hasSubscribers;

    public SubscribersAccess() {
        for (String subscriber : MinervaWebapp.factory().getConfig().getSubscribers().split(",")) {
            if (!subscriber.isBlank()) {
                subscribers.add(subscriber.trim());
            }
        }
        hasSubscribers = !subscribers.isEmpty();
    }

    public boolean hasSubscribers() {
        return hasSubscribers;
    }

    public void checkIfValid(String url) {
        if (hasSubscribers) {
            for (String subscriber : subscribers) {
                if (subscriber.equals(url)) {
                    return; // is valid
                }
            }
        }
        Logger.error("Subscriber \"" + url + "\" is not in MINERVA_SUBSCRIBERS list! " + MinervaWebapp.factory().getConfig().getSubscribers());
        throw new RuntimeException("Unknown subscriber");
    }
    
    public List<PageTitles> loadPageTitles() {
        List<PageTitles> ret = new ArrayList<>();
        for (String subscriber : subscribers) {
            if (available(subscriber)) {
                String url = subscriber + "/rest/page-titles";
                Logger.info("loading page titles from: " + url);
                PageTitles pageTitles = new REST(url).get().fromJson(PageTitles.class);
                ret.add(pageTitles);
            }
        }
        return ret;
    }
    
    public void uploadZip(File zipFile) {
        subscribers.forEach(subscriber -> uploadZip(zipFile, subscriber));
    }

    public void uploadZip(File zipFile, String subscriber) {
        if (available(subscriber)) {
            String url = subscriber + "/book6/upload?token=" + System.getenv("MINERVA_TOKEN");
            new REST(url).uploadZip(zipFile);
            Logger.info("Upload of " + zipFile.getAbsolutePath() + " to " + url + " completed.");
        }
    }

    public void put(TPage page) {
        for (String subscriber : subscribers) {
            if (available(subscriber)) {
                String url = makeUrl(page.getId(), subscriber);
                Logger.info("PUT " + url);
                new REST(url).put(new Gson().toJson(page), REST.json_utf8()).close();
            }
        }
    }
    
    public void delete(String id) {
        for (String subscriber : subscribers) {
            if (available(subscriber)) {
                String url = makeUrl(id, subscriber);
                Logger.info("DELETE " + url);
                REST.delete(url);
            }
        }
    }

	private String makeUrl(String id, String subscriber) {
		return subscriber + "/book6/page/" + id + "?token=" + System.getenv("MINERVA_TOKEN");
	}
    
    private boolean available(String subscriber) {
        String r;
        try {
            r = REST.get(subscriber + "/rest/_ping");
            if ("pong".equals(r)) {
                Logger.debug("Subscriber " + subscriber + " is available.");
                return true;
            }
        } catch (Exception e) {
            r = e.getMessage();
        }
        Logger.debug("Subscriber " + subscriber + " is not available: " + r);
        return false;
    }
}
