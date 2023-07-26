package minerva.model;

import java.util.ArrayList;
import java.util.List;

import minerva.access.CommitMessage;
import minerva.seite.tag.TagNList;

public class TagsSO {
    private final SeiteSO seiteSO;

    public TagsSO(SeiteSO seiteSO) {
        this.seiteSO = seiteSO;
    }
    
    /**
     * Add tags
     * @param tag one or more tags, comma separated
     */
    public void addTag(String tag) {
        boolean dirty = false;
        boolean invisibleTagAdded = false;
        int n = 0;
        if (tag.contains(",")) {
            for (String i : tag.split(",")) {
                String oneTag = cleanTag(i);
                if ("invisible".equals(oneTag)) {
                    invisibleTagAdded = true;
                }
                if (addOneTag(oneTag)) {
                    dirty = true;
                    n++;
                }
            }
        } else {
            String oneTag = cleanTag(tag);
            if ("invisible".equals(oneTag)) {
                invisibleTagAdded = true;
            }
            dirty = addOneTag(oneTag);
        }
        if (dirty) {
            seiteSO.saveMeta(new CommitMessage(seiteSO, "tag" + (n > 1 ? "s" : "") + " " + tag + " added"));
            if (invisibleTagAdded) {
                seiteSO.updateOnlineHelp_nowInvisible();
            }
        }
    }

    private boolean addOneTag(String tag) {
        if (!seiteSO.getSeite().getTags().contains(tag)) {
            seiteSO.getSeite().getTags().add(tag);
            return true;
        }
        return false;
    }
    
    public void addAllTags(TagNList tags) {
        seiteSO.getSeite().getTags().forEach(tag -> tags.add(tag));
        seiteSO.getSeiten().forEach(seite -> seite.tags().addAllTags(tags));
    }

    public List<SeiteSO> findTag(String tag) {
        List<SeiteSO> ret = new ArrayList<>();
        if (seiteSO.getSeite().getTags().contains(tag)) {
            ret.add(seiteSO);
        }
        for (SeiteSO sub : seiteSO.getSeiten()) {
            ret.addAll(sub.tags().findTag(tag));
        }
        return ret;
    }

    public void deleteTag(String tag) {
        boolean containsInvisibleBefore = seiteSO.getSeite().getTags().contains("invisible");
        if ("$all".equals(tag)) {
            seiteSO.getSeite().getTags().clear();
            seiteSO.saveMeta(new CommitMessage(seiteSO, "all tags deleted"));
            
            if (containsInvisibleBefore) {
                seiteSO.updateOnlineHelp_nowVisible();
            }
        } else {
            seiteSO.getSeite().getTags().remove(tag);
            seiteSO.saveMeta(new CommitMessage(seiteSO, "tag " + tag + " deleted"));
            
            if (containsInvisibleBefore && "invisible".equals(tag)) {
                seiteSO.updateOnlineHelp_nowVisible();
            }
        }
    }

    public static String cleanTag(String pTag) {
        String tag = pTag.toLowerCase().trim();
        while (tag.contains("  ")) {
            tag = tag.replace("  ", " ");
        }
        return tag.replace(" ", "-");
    }
}
