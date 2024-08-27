package minerva.book;

import java.io.File;

import org.pmw.tinylog.Logger;

import minerva.base.FileService;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;

// XXX temp.
public class MenuStructureAction extends BAction {
    
    @Override
    protected void execute() {
        String mode = ctx.queryParam("m");
        
        if ("input".equals(mode)) {
            Logger.info("Menu... input");
            
        } else { // output
            Logger.info("Menu... output");
            
            StringBuilder sb = new StringBuilder();
            add(book.getSeiten(), sb, "");
            
            File out = new File("export/menu.txt");
            FileService.savePlainTextFile(out, sb.toString());
            Logger.info(out.getAbsolutePath());
        }
    }
    
    private void add(SeitenSO seiten, StringBuilder sb, String indent) {
        for (SeiteSO seite : seiten) {
            sb.append(indent + "- " + seite.getTitle() + "\n");
            add(seite.getSeiten(), sb, indent + "  "); // recursive
        }
    }
}
