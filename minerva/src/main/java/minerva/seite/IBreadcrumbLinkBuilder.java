package minerva.seite;

public interface IBreadcrumbLinkBuilder {

    /**
     * @param branch -
     * @param folder book folder
     * @param id null: it's a book, otherwise it's a page
     * @return breadcrumb link
     */
    String build(String branch, String folder, String id);
}
