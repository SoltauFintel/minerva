package minerva.migration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import minerva.base.FileService;
import minerva.confluence.ConfluencePage;

public class MappingTableCreator {
    private ConfluencePage root_de;
    private ConfluencePage root_en;
    private String text = "";
    
    public static void main(String[] args) throws IOException {
        new MappingTableCreator().start();
    }
    
    public void start() throws IOException {
        readHtmlFiles(new File("D:\\dat\\online-help\\html"));
        print(root_de, 0, "de;");
        print(root_en, 0, "en;");
        File file = new File("D:\\dat\\online-help\\mapping-tabelle.csv");
        try (FileWriter w = new FileWriter(file, Charset.forName("windows-1252"))) {
            w.write(text);
        }
        System.out.println("finished. saved to " + file.toString());
    }
    
    private void readHtmlFiles(File confluenceHtmlDir) {
        ConfluencePage root = FileService.loadJsonFile(new File(confluenceHtmlDir, "data.json"), ConfluencePage.class);
        root_de = root.getSubpages().get(0);
        root_en = root.getSubpages().get(1);
    }
    
    private void print(ConfluencePage parent, int ebene, String prefix) {
        text += prefix + ebene + ";" + parent.getId() + ";" + parent.getTitle() + "\r\n";
        for (ConfluencePage sub : parent.getSubpages()) {
            print(sub, ebene + 1, prefix);
        }
    }
}
