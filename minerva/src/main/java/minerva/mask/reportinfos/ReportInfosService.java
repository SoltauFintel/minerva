package minerva.mask.reportinfos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.rest.REST;
import github.soltaufintel.amalia.rest.RestResponse;
import minerva.MinervaWebapp;
import minerva.access.CommitMessage;
import minerva.base.StringService;
import minerva.book.BookType;
import minerva.mask.FeatureFields;
import minerva.mask.FeatureFieldsService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.seite.CreateSeiteUnderTag;

// ~XMinerva
public class ReportInfosService {
    private final List<String> reportTypes=new ArrayList<>();
    private int ncreated;
    private int nupdated;
    
    public ReportInfosService () {
        reportTypes.add("JR");
        reportTypes.add("JRSQL");
        reportTypes.add("XR");
    }
    
    public void firstImport(BookSO book, boolean force) {
        if (!BookType.FEATURE_TREE.equals(book.getBook().getType())) {
            throw new RuntimeException("Book must be of type 'Feature Tree'");
        }
        int subpages = 0;
        for (String rt : reportTypes) {
            subpages += checkForParentFeature(book, "ri_" + rt.toLowerCase());
        }
        if (subpages > 0 && !force) {
            throw new RuntimeException("There are already sub features. Use force option to continue.");
        }
        Logger.info("First import of report infos");
        Logger.info("- downloading report infos...");
        List<ReportInfo> reportInfos = download();
        for (String rt : reportTypes) {
            Logger.info("- importing report type " + rt + " ...");
            ncreated = 0;
            doFirstImport(book, rt, reportInfos.stream().filter(i -> i.getTyp().equals(rt)));
            Logger.info("  saved features: " + ncreated);
        }
    }
    
    public void update(BookSO book) {
        if (!BookType.FEATURE_TREE.equals(book.getBook().getType())) {
            throw new RuntimeException("Book must be of type 'Feature Tree'");
        }
        for (String rt : reportTypes) {
            checkForParentFeature(book, "ri_" + rt.toLowerCase());
        }
        Logger.info("Updating report features");
        Logger.info("- downloading report infos...");
        List<ReportInfo> reportInfos = download();
        for (String rt : reportTypes) {
            Logger.info("- updating report type " + rt + " ...");
            ncreated = 0;
            nupdated = 0;
            doUpdate(book, rt, reportInfos.stream().filter(i -> i.getTyp().equals(rt)));
            Logger.info("  updated features: " + nupdated + ", created features: " + ncreated);
        }
    }

    private int checkForParentFeature(BookSO book, String tag) {
        List<SeiteSO> seiten = book.findTag(tag);
        if (seiten.size() > 1) {
            throw new RuntimeException("There should be only one feature with tag " + tag + ". Please fix that!");
        } else if (seiten.isEmpty()) {
            throw new RuntimeException("There should be one feature with tag " + tag + ". Please create that page!");
        }
        return seiten.get(0).getSeiten().size();
    }

    private void doFirstImport(BookSO book, String reportType, Stream<ReportInfo> reportInfos) {
        // Step 1: delete all features
        SeiteSO parent = book.findTag("ri_" + reportType.toLowerCase()).get(0);
        deleteOldFeatures(parent);

        // Step 2: collect features
        Map<String, ReportFeature> reportFeatures = collectReportFeatures(reportInfos);
        
        // Step 3: create features
        Map<String, String> files = new HashMap<>();
        for (ReportFeature rf : reportFeatures.values()) {
            createFeature(rf, !rf.getCustomerSpecificFeatures().isEmpty(), parent, files, true);
            rf.getCustomerSpecificFeatures().forEach(f -> createFeature(f, true, parent, files, true));
        }
        
        // Step 4: save features
        book.dao().saveFiles(files, new CommitMessage("Import report infos as report features"), book.getWorkspace());
    }
    
    private void doUpdate(BookSO book, String reportType, Stream<ReportInfo> reportInfos) {
        SeiteSO parent = book.findTag("ri_" + reportType.toLowerCase()).get(0);
        
        // Step 2: collect features
        Map<String, ReportFeature> reportFeatures = collectReportFeatures(reportInfos);
        
        // Step 3: create features
        Map<String, String> files = new HashMap<>();
        for (ReportFeature rf : reportFeatures.values()) {
            createFeature(rf, !rf.getCustomerSpecificFeatures().isEmpty(), parent, files, false);
            rf.getCustomerSpecificFeatures().forEach(f -> createFeature(f, true, parent, files, false));
        }
        
        // Step 4: save features
        if (!files.isEmpty()) {
            book.dao().saveFiles(files, new CommitMessage("Update report features"), book.getWorkspace());
        }
    }

    private void deleteOldFeatures(SeiteSO parent) {
        Set<String> filenames = new HashSet<>();
        SeitenSO seiten = parent.getSeiten();
        if (!seiten.isEmpty()) {
            for (SeiteSO seite : seiten) {
                filenames.add(seite.filenameMeta());
                filenames.add(seite.filenameHtml("de"));
                filenames.add(FeatureFieldsService.dn(seite));
            }
            List<String> cantBeDeleted = new ArrayList<>();
            parent.getBook().dao().deleteFiles(filenames, new CommitMessage("Delete features before importing report features"),
                    parent.getBook().getWorkspace(), cantBeDeleted);
            if (!cantBeDeleted.isEmpty()) {
                throw new RuntimeException("These features can't be deleted:\n" + cantBeDeleted.toString());
            }
        }
    }
    
    private Map<String, ReportFeature> collectReportFeatures(Stream<ReportInfo> reportInfos) {
        Map<String, ReportFeature> reportFeatures = new TreeMap<>();
        reportInfos.filter(ri -> ri.isAktiv() && !ri.getTemplate().isBlank()).forEach(ri -> {
            String title = ri.getFinalTitle();
            ReportFeature rf = reportFeatures.get(title);
            if (rf == null) {
                reportFeatures.put(title, new ReportFeature(ri));
            } else if (compare(rf, ri)) {
                rf.getCustomers().add(ri.getKunde());
            } else {
                boolean found = false;
                for (ReportFeature f : rf.getCustomerSpecificFeatures()) {
                    if (compare(f, ri)) {
                        f.getCustomers().add(ri.getKunde());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    rf.getCustomerSpecificFeatures().add(new ReportFeature(ri));
                }
            }
        });
        return reportFeatures;
    }
    
    private boolean compare(ReportFeature rf, ReportInfo ri) {
        return rf.getInfo().equals(ri.getInfo()) //
                && rf.getReportnumber().equals(ri.getNummer()) //
                && rf.getTemplate().equals(ri.getTemplate()) //
                && rf.getTest().equals(ri.getUnittests()) //
                && rf.getVisualtest().equals(ri.getVisualtest());
    }
    
    private void createFeature(ReportFeature rf, boolean addCustomersToTitle, SeiteSO parent, Map<String,String> files, boolean firstImport) {
        BookSO book = parent.getBook();
        String title = rf.getFinalTitle();
        if (addCustomersToTitle) {
            title += " (" + rf.getCustomers().stream().collect(Collectors.joining(", ")) + ")";
        }
        
        SeiteSO feature = parent.getSeiten()._byTitle(title, "de");
        boolean newMode = false;
        if (feature == null) {
            newMode = true;
            ncreated++;
            String id = parent.getSeiten().createSeite(parent, book, book.dao());
            feature = book._seiteById(id);
            if (!firstImport) {
                Logger.info("  created report feature: #" + id + ": " + title);
            }
        } else {
            nupdated++;
        }
        FeatureFields ff;
        String customers = rf.getCustomers().stream().collect(Collectors.joining(","));
        if (firstImport || newMode) {
            feature.getSeite().getTitle().setString("de", title);
            feature.saveMetaTo(files);
            feature.getContent().setString("de", CreateSeiteUnderTag.body("::"));
            feature.saveHtmlTo(files, List.of("de"));
            
            ff = FeatureFields.create(feature);
            ff.set("featurenumber", rf.getReportnumber()); // TO-DO uniqueness check
        } else {
            
            ff = new FeatureFieldsService().get(feature);
            // optimization: only save when really needed
            if (ff.get("title").equals(rf.getTitle()) && ff.get("customers").equals(customers)
                    && ff.get("reportnumber").equals(rf.getReportnumber())
                    && ff.get("template").equals(rf.getTemplate()) && ff.get("info").equals(rf.getInfo())
                    && ff.get("test").equals(rf.getTest()) && ff.get("visualtest").equals(rf.getVisualtest())) {
                nupdated--;
                return;
            }
        }
        ff.set("title", rf.getTitle());
        ff.set("customers", customers);
        ff.set("reportnumber", rf.getReportnumber());
        ff.set("template", rf.getTemplate());
        ff.set("info", rf.getInfo());
        ff.set("test", rf.getTest());
        ff.set("visualtest", rf.getVisualtest());
        files.put(FeatureFieldsService.dn(feature), StringService.prettyJSON(ff));
    }
    
    private List<ReportInfo> download() {
        final String url = MinervaWebapp.factory().getConfig().getReportInfosDownloadUrl();
        try {
            File temp = Files.createTempFile("Minerva-ReportInfos-", ".xlsx").toFile();
            RestResponse response = new REST(url).get();
            response.getHttpResponse().getEntity().writeTo(new FileOutputStream(temp));
            try {
                return read(temp);
            } finally {
                response.close();
                temp.delete();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error downloading report infos from " + url + ": " + e.getMessage(), e);
        }
    }
    
    private List<ReportInfo> read(File file) {
        try {
            List<ReportInfo> ret = new ArrayList<>();
            Workbook w = WorkbookFactory.create(file);
            Sheet sheet = w.getSheet("Berichte");
            Iterator<Row> iter = sheet.rowIterator();
            iter.next(); // omit header row
            while (iter.hasNext()) {
                Row row = iter.next();

                ReportInfo ri = new ReportInfo(row);
                if (!StringService.isNullOrEmpty(ri.getKunde())
                        && ("JR".equals(ri.getTyp()) || "JRSQL".equals(ri.getTyp()) || "XR".equals(ri.getTyp()))) {
                    ret.add(ri);
                }
            }
            w.close();
            return ret;
        } catch (EncryptedDocumentException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
