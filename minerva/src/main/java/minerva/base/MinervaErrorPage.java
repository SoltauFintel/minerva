package minerva.base;

import org.pmw.tinylog.Logger;

import com.google.common.base.Strings;

import github.soltaufintel.amalia.web.action.ErrorPage;
import github.soltaufintel.amalia.web.action.Page;

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
        if (exception instanceof ErrorMessageHolder e) {
            msg = e.getErrorMessage(); // no esc
        } else {
            msg = esc(msg); // for subclasses
        }
        put("msg", msg == null ? "(no error message)" : msg.replace("\n", "<br/>").replace("\\n", "<br/>"));
        put("p", "");
        put("title", "Minerva error");
        put("header", exception instanceof UserMessage ? "Message" : "Sorry, this should not happen!");
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
