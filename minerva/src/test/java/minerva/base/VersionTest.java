package minerva.base;

import org.junit.Assert;
import org.junit.Test;

public class VersionTest {

    @Test
    public void text() {
        String text = "Release Notes (EN) No. abcd (22.09.2023)";
        Assert.assertEquals(text, new Version().version(text));
    }

    @Test
    public void dreiNummern_einfach() {
        Assert.assertEquals("3.26.04", new Version().version("Release Notes (EN) No. 3.26.4 (bla)"));
    }

    @Test
    public void dreiNummern_dritteNummerZweistellig() {
        Assert.assertEquals("3.26.14", new Version().version("Release Notes (EN) No. 3.26.14 (bla)"));
    }

    @Test
    public void dreiNummern_dritteNummerDreistellig() {
        String text = "Release Notes (EN) No. 3.26.142 (bla)";
        Assert.assertEquals(text, new Version().version(text));
    }

    @Test
    public void dreiNummern_Datum() {
        Assert.assertEquals("3.26.04", new Version().version("Release Notes (EN) No. 3.26.4 (22.09.2023)"));
    }

    @Test
    public void dreiNummern_DatumVorne() {
        Assert.assertEquals("3.26.04", new Version().version(" 1.10.2023 3.26.4 "));
    }

    @Test
    public void vierNummern_einfach() {
        Assert.assertEquals("3.26.14.01", new Version().version("Release Notes (EN) No. 3.26.14.1 (bla)"));
    }

    @Test
    public void drei_kurz() {
        Assert.assertEquals("1.23.04", new Version().version("1.23.4"));
        Assert.assertEquals("1.23.00", new Version().version("1.23.0"));
    }

    @Test
    public void vier_kurz() {
        Assert.assertEquals("1.23.04.05", new Version().version("1.23.4.5"));
    }

    @Test
    public void negativfaelle() {
        String text = "X 0.01.1 X"; // erste Nummer darf nicht "0" sein
        Assert.assertEquals(text, new Version().version(text));
        text = "X 3.1.1 X"; // zweite Nummer muss zweistellig sein
        Assert.assertEquals(text, new Version().version(text));
        text = "X 3.12 X"; // nur zwei Nummern
        Assert.assertEquals(text, new Version().version(text));
        text = "X 3.12.1.2.3 X"; // fünf Nummern
        Assert.assertEquals(text, new Version().version(text));
    }
    
    @Test
    public void fuenfNummern_korrektFormatierteNummern3und4() {
        String text = "X 3.12.34.56.7 X";
        Assert.assertEquals(text, new Version().version(text));
        text = "3.12.34.56.7 X";
        Assert.assertEquals(text, new Version().version(text));
    }
}
