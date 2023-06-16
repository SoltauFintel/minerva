package minerva.seite;

public class ViewAreaBreadcrumbLinkBuilder implements IBreadcrumbLinkBuilder {

    @Override
    public String build(String branch, String folder, String id) {
        if (id == null) {
            return "/b/" + branch + "/" + folder;
        } else {
            return "/s/" + branch + "/" + folder + "/" + id;
        }
    }
}
