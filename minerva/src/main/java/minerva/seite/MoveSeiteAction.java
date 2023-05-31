package minerva.seite;

public class MoveSeiteAction extends SAction {

    @Override
    protected void execute() {
        String parentId = ctx.queryParam("parentid");
        
        seite.move(parentId);

        ctx.redirect(viewlink);
    }
}
