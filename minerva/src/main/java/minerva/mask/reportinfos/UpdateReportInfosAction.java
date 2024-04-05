package minerva.mask.reportinfos;

import minerva.book.BAction;

public class UpdateReportInfosAction extends BAction {

    @Override
    protected void execute() {
        String m = ctx.queryParam("m");
        if ("import".equals(m)) {
            new ReportInfosService().firstImport(book, false);
        } else if ("force-import".equals(m)) {
            new ReportInfosService().firstImport(book, true);
        } else if ("update".equals(m)) {
            new ReportInfosService().update(book);
        } else {
            throw new RuntimeException("Unknown mode " + m);
        }
        
        ctx.redirect("/w/" + branch + "/menu");
    }
}
