package gitper.jenkins;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import de.mwvb.base.xml.XMLDocument;
import de.mwvb.base.xml.XMLElement;
import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.web.action.Escaper;
import gitper.base.StringService;

public abstract class JenkinsBuild {
    private final Jenkins jenkins;
    private final String jobName;

    private String runId;
    private String number = "";
    private String status = "";
    
    public JenkinsBuild(Jenkins jenkins, String jobName) {
        this.jenkins = jenkins;
        this.jobName = jobName;
    }

    public String run(Map<String, String> arguments) {
        Map<String, String> args = new HashMap<>(arguments);
        runId = IdGenerator.createId25();
        number = "";
        status = "";
        args.put(getRunIdName(), runId);
        Logger.info("args: " + args);
        String argsString = args.entrySet().stream()
                .map(kv -> "&" + kv.getKey() + "=" + Escaper.urlEncode(kv.getValue(), ""))
                .collect(Collectors.joining());

        String r = jenkins.get("/job/" + jobName + "/buildWithParameters?token=" + getJobSecret() + argsString);
        if (!r.isEmpty()) {
            Logger.error(r);
            throw new RuntimeException("Jenkins job could not be started! (See log)");
        }
        return runId;
    }
    
    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getNumber() {
        if (!number.isEmpty()) {
            return number;
        } else if (StringService.isNullOrEmpty(runId)) {
            throw new RuntimeException("Call setRunId() before!");
        }
        String xml = jenkins.get("/job/" + jobName + "/api/xml");
        try (XMLDocument allBuildsDok = new XMLDocument(xml)) {
            for (XMLElement e : allBuildsDok.getElement().selectNodes("build")) {
                String number = e.selectSingleNode("number").getText();

                xml = jenkins.get(e.selectSingleNode("url").getText() + "api/xml");
                if (xml.contains("<value>" + runId + "</value>")) {
                    this.number = number;
                    return number;
                }
            }
            return ""; // nicht gefunden
        }
    }
    
    public String getStatus() {
        if (!status.isEmpty()) {
            return status;
        } else if (StringService.isNullOrEmpty(runId)) {
            throw new RuntimeException("Call setRunId() before!");
        }
        String xml = jenkins.get("/job/" + jobName + "/" + number + "/api/xml");
        try (XMLDocument dok = new XMLDocument(xml)) {
            XMLElement e = dok.getElement().selectSingleNode("result");
            if (e != null) {
                status = e.getText();
                return status;
            }
        }
        return "";
    }
    
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }

    // Jenkins Job Authentifizierungstoken
    public abstract String getJobSecret();
    
    public abstract String getRunIdName();
}
