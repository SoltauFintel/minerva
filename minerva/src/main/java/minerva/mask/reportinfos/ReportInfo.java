package minerva.mask.reportinfos;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

// ~XMinerva
public class ReportInfo {
    private String kunde;
    private String typ;
    private String nummer;
    private String name;
    private boolean aktiv;
    private String handle;
    private String info;
    private String template;
    private int tests;
    /** Number of unit tests */
    private int ut;
    /** Number of Visual Tests */
    private int vt;
    private int sheets;
    private String sheetStreaming; // Aufzählung
    private String sheetNames;     // Aufzählung
    private String unittests;      // Aufzählung
    private String visualtest;     // Aufzählung

    public ReportInfo(Row r) {
        try {
            kunde = s(r.getCell(0));
            typ = s(r.getCell(1));
            nummer = s(r.getCell(2));
            name = s(r.getCell(3));
            aktiv = "ja".equals(s(r.getCell(4)));
            handle = s(r.getCell(5));
            info = s(r.getCell(6));
            template = s(r.getCell(7));
            tests = Integer.valueOf(s(r.getCell(8)));
            ut = Integer.valueOf(s(r.getCell(9)));
            vt = Integer.valueOf(s(r.getCell(10)));
            sheets = Integer.valueOf(s(r.getCell(11)));
            sheetStreaming = s(r.getCell(12));
            sheetNames = s(r.getCell(13));
            unittests = s(r.getCell(14));
            visualtest = s(r.getCell(15));
        } catch (Exception e) {
            throw new RuntimeException("Error reading row #" + r.getRowNum() + ": " + e.getMessage(), e);
        }
    }

    private String s(Cell cell) {
        return cell == null ? "" : cell.getStringCellValue();
    }

    public String getKunde() {
        return kunde;
    }

    public String getTyp() {
        return typ;
    }

    public String getNummer() {
        return nummer;
    }

    public String getName() {
        return name;
    }

    public boolean isAktiv() {
        return aktiv;
    }

    public String getHandle() {
        return handle;
    }

    public String getInfo() {
        return info;
    }

    public String getTemplate() {
        return template;
    }

    public int getTests() {
        return tests;
    }

    public int getUt() {
        return ut;
    }

    public int getVt() {
        return vt;
    }

    public int getSheets() {
        return sheets;
    }

    public String getSheetStreaming() {
        return sheetStreaming;
    }

    public String getSheetNames() {
        return sheetNames;
    }

    public String getUnittests() {
        return unittests;
    }

    public String getVisualtest() {
        return visualtest;
    }
    
    public String getFinalTitle() {
        return (getNummer() + " " + shorten(getTemplate())).trim();
    }

    private String shorten(String template) {
        return template.substring(template.lastIndexOf("/") + 1);
    }
}
