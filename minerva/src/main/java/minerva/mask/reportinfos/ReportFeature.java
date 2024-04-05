package minerva.mask.reportinfos;

import java.util.ArrayList;
import java.util.List;

public class ReportFeature {
    private final List<String> customers = new ArrayList<>();
    private final List<ReportFeature> customerSpecificFeatures = new ArrayList<>();
    private String reportnumber;
    private String title;
    private String template;
    private String info;
    private String test;
    private String visualtest;

    public ReportFeature() {
    }
    
    public ReportFeature(ReportInfo ri) {
        getCustomers().add(ri.getKunde());
        setTitle(ri.getName());
        setInfo(ri.getInfo());
        setReportnumber(ri.getNummer());
        setTemplate(ri.getTemplate());
        setTest(ri.getUnittests());
        setVisualtest(ri.getVisualtest());
    }
    
    public List<String> getCustomers() {
        return customers;
    }

    public List<ReportFeature> getCustomerSpecificFeatures() {
        return customerSpecificFeatures;
    }

    public String getReportnumber() {
        return reportnumber;
    }

    public void setReportnumber(String reportnumber) {
        this.reportnumber = reportnumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getVisualtest() {
        return visualtest;
    }

    public void setVisualtest(String visualtest) {
        this.visualtest = visualtest;
    }
    
    public String getFinalTitle() {
        return (getReportnumber() + " " + getTitle()).trim();
    }
}
