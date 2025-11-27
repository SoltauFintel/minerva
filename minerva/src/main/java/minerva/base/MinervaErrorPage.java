package minerva.base;

import java.util.Map;

import org.pmw.tinylog.Logger;

import com.google.common.base.Strings;

import github.soltaufintel.amalia.web.action.ErrorPage;
import github.soltaufintel.amalia.web.action.Page;
import gitper.base.ErrorMessageHolder;

public class MinervaErrorPage extends Page implements ErrorPage {
    protected Exception exception;
    protected String msg;

    @Override
    public void setException(Exception exception) {
        this.exception = exception;
        if (exception != null) {
            if (Strings.isNullOrEmpty(exception.getMessage())) {
                msg = exception.getClass().getName();
            } else {
                msg = exception.getMessage();
                if (msg.contains("Jira.access") && exception instanceof NullPointerException) {
                    msg = "Jira access is not available. Please check configuration.";
                }
            }
        }
    }
    
    @Override
    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    protected void execute() {
        Logger.error("Error rendering path \"" + ctx.path() + "\":");
        if (exception == null) {
            Logger.error(msg);
        } else {
            Logger.error(exception);
        }
        ctx.status(500);
        if (exception instanceof ErrorMessageHolder ex) {
            msg = NLS.get(language(), ex.getKey());
            for (Map.Entry<String, String> entry : ex.getParameters().entrySet()) {
                msg = msg.replace(entry.getKey(), entry.getValue());
            }
            // no esc
        } else {
            msg = esc(msg); // for subclasses
        }
        put("msg", msg == null ? "(no error message)" : msg.replace("\n", "<br/>").replace("\\n", "<br/>"));
        put("p", "");
        put("title", "Minerva error");
        put("header", exception instanceof UserMessage ? "Message" : "Sorry, this should not happen!");
        MinervaMetrics.ERRORPAGE.inc();
    }
    
    private String language() {
        String al = ctx.req.headers("Accept-Language");
        if (al != null) {
            int o = al.indexOf(";");
            if (o >= 0) {
                al = al.substring(0, o);
                if (al.toLowerCase().startsWith("de")) {
                    return "de";
                }
            }
        }
        return "en";
    }
    
    @Override
    protected String render() {
        try {
            return templates.render(MinervaErrorPage.class.getSimpleName(), model);
        } catch (Exception e) {
            Logger.error(e);
            return "Error while displaying error. See log.";
        }
    }
}
