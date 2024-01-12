package minerva.subscription;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.pmw.tinylog.Logger;

import com.google.gson.Gson;

import github.soltaufintel.amalia.rest.REST;
import github.soltaufintel.amalia.rest.RestResponse;
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
            String url = subscriber + "/upload";
            new REST(url).uploadZip(zipFile);
            Logger.info("Upload of " + zipFile.getAbsolutePath() + " to " + url + " completed.");
        }
    }

    public void put(TPage page) {
        for (String subscriber : subscribers) {
            if (available(subscriber)) {
                String url = subscriber + "/book6/page/" + page.getId();
                Logger.info("PUT " + url);
                new REST(url) {
                    @Override
                    protected RestResponse request(HttpEntityEnclosingRequestBase request, Object data) {
                        try {
                            return request(request, new Gson().toJson(data), "application/json; charset=cp1252");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }.put(page).close();
            }
        }
    }
    
    public void delete(String id) {
        for (String subscriber : subscribers) {
            if (available(subscriber)) {
                String url = subscriber + "/book6/page/" + id;
                Logger.info("DELETE " + url);
                new REST(url).delete().close();
            }
        }
    }
    
    private boolean available(String subscriber) {
        String r;
        try {
            r = new REST(subscriber + "/rest/_ping").get().response();
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
