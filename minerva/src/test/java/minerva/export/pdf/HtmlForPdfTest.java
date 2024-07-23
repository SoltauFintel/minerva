package minerva.export.pdf;

import org.junit.Assert;
import org.junit.Test;

public class HtmlForPdfTest {

    @Test
    public void hsl() {
        String html = "<span style=\"color: hsl(60, 50%, 40%); border: 1px solid black\"></span>";
        Assert.assertEquals("<span style=\"color:#999933; border: 1px solid black;\"></span>", HtmlForPdf.colors(html));
    }

    @Test
    public void empty() {
        String html = "<div><span style=\"\">hey</span>123</div>";
        Assert.assertEquals("<div><span style=\"\">hey</span>123</div>", html = HtmlForPdf.colors(html));
        // Vor dem Fix kam ";<;d;i;v;>;<;s;p;a;n; ;s;t;y;l;e;=;";";>;h;e;y;<;/;s;p;a;n;>;1;2;3;<;/;d;i;v;>;" raus und dann konnte kein PDF erstellt werden.
    }
}
