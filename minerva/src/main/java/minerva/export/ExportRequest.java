package minerva.export;

import github.soltaufintel.amalia.spark.Context;
import minerva.model.WorkspaceSO;

public class ExportRequest {
    private final WorkspaceSO workspace;
    private final String customer;
    private final String language;
    private final String templateId;
    private final boolean withCover;
    private final boolean withTOC; // table of contents
    private final boolean withChapters;
    private final Context context;

    public ExportRequest(WorkspaceSO workspace, Context ctx) {
        this(workspace, ctx.queryParam("customer"), ctx.queryParam("lang"), ctx.queryParam("template"), //
                o("c", ctx), o("i", ctx), o("k", ctx), //
                ctx);
    }
    
    private static boolean o(String pChar, Context ctx) {
        return ctx.queryParam("o") == null ? false : ctx.queryParam("o").contains(pChar);
    }

    // internal and for test
    ExportRequest(WorkspaceSO workspace, String customer, String language, String templateId, //
            boolean withCover, boolean withTOC, boolean withChapters, Context context) {
        this.workspace = workspace;
        this.customer = customer;
        this.language = language;
        this.templateId = templateId;
        this.withCover = withCover;
        this.withTOC = withTOC;
        this.withChapters = withChapters;
        this.context = context;
    }

    public WorkspaceSO getWorkspace() {
        return workspace;
    }

    public String getCustomer() {
        return customer;
    }

    public String getLanguage() {
        return language;
    }

    public String getTemplateId() {
        return templateId;
    }

    public boolean withCover() {
        return withCover;
    }

    public boolean withTOC() {
        return withTOC;
    }

    public boolean withChapters() {
        return withChapters;
    }

    public Context getContext() {
        return context;
    }
}
