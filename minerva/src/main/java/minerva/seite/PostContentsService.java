package minerva.seite;

public class PostContentsService {
    private static final String handle = "handle";
    public static PostContentsData last;

    private PostContentsService() {
    }
    
    public static void set(PostContentsData data) {
        synchronized (handle) {
            data.setParent(last);
            last = data;
        }
    }
    
    public static PostContentsData get(String branch, String bookFolder, String id, int version) {
        synchronized (handle) {
            PostContentsData pick = last;
            while (pick != null) {
                if (pick.getBranch().equals(branch) && pick.getBookFolder().equals(bookFolder)
                        && pick.getId().equals(id) && pick.getVersion() == version) {
                    return pick;
                }
                pick = pick.getParent();
            }
        }
        return null;
    }
}
