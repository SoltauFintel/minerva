package minerva.model;

import java.util.ArrayList;
import java.util.List;

import minerva.git.CommitMessage;
import minerva.seite.tag.TagNList;

public class TagsSO {
    private final SeiteSO seiteSO;

    public TagsSO(SeiteSO seiteSO) {
        this.seiteSO = seiteSO;
    }
    
    public void addTag(String tag) {
        boolean dirty = false;
        if (tag.contains(",")) {
            for (String aTag : tag.split(",")) {
                if (addTag2(aTag)) {
                    dirty = true;
                }
            }
        } else {
            dirty = addTag2(tag);
        }
        if (dirty) {
            seiteSO.saveMeta(new CommitMessage(seiteSO, "tag " + tag + " added"));
        }
    }

    private boolean addTag2(String pTag) {
        String tag = cleanTag(pTag);
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
        if ("$all".equals(tag)) {
            seiteSO.getSeite().getTags().clear();
            seiteSO.saveMeta(new CommitMessage(seiteSO, "all tags deleted"));
        } else {
            seiteSO.getSeite().getTags().remove(tag);
            seiteSO.saveMeta(new CommitMessage(seiteSO, "tag " + tag + " deleted"));
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
