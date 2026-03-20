package minerva.validate;

import org.junit.Assert;
import org.junit.Test;

import minerva.MinervaWebapp;

public class RemoveStyleAttributesServiceTest {

    @Test
    public void filter() {
        MinervaWebapp.bootForTest();
        var html = "<html><body>\n"
                + "<p style=\"font-weight: bold; margin-top:2em;\"></p>\n"
                + "</body></html>";
        
        String result = new RemoveStyleAttributesService().filter(html, null, null);

        Assert.assertEquals("<html>\n <head></head>\n <body>\n  <p></p>\n </body>\n</html>", result);
    }

    @Test
    public void textAlignRight3() {
        MinervaWebapp.bootForTest();
        var html = "<html><body>\n"
                + "<table style=\"background-color: blue;\"><tr><td style=\"font-weight: bold; text-align:  right; margin-top:2em;\">\n"
                + "</td></tr></table>\n"
                + "</body></html>";
        
        String result = new RemoveStyleAttributesService().filter(html, null, null);

        System.out.println("\""+result.replace("\"","\\\"").replace("\n","\\n")+"\"");
        Assert.assertEquals("<html>\n <head></head>\n <body>\n  <table>\n   <tbody>\n    <tr>\n     "
                + "<td style=\"text-align: right;\"></td>\n    </tr>\n   </tbody>\n  </table>\n </body>\n</html>", result);
    }
    
    @Test
    public void textAlignRight1() {
        MinervaWebapp.bootForTest();
        var html = "<html><body>\n"
                + "<table style=\"background-color: blue;\"><tr><td style=\"text-align:right\">\n"
                + "</td></tr></table>\n"
                + "</body></html>";
        
        String result = new RemoveStyleAttributesService().filter(html, null, null);

        System.out.println("\""+result.replace("\"","\\\"").replace("\n","\\n")+"\"");
        Assert.assertEquals("<html>\n <head></head>\n <body>\n  <table>\n   <tbody>\n    <tr>\n     "
                + "<td style=\"text-align: right;\"></td>\n    </tr>\n   </tbody>\n  </table>\n </body>\n</html>", result);
    }
}
