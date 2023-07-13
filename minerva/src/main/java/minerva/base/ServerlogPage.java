package minerva.base;

import minerva.user.UPage;

public class ServerlogPage extends UPage {

    @Override
    protected void execute() {
        user.onlyAdmin();
        user.log("-- Server log called.");
        
        String c = user.getServerlog();
        if (c != null) {
            String[] lines = c.split("\n");
            StringBuilder sb = new StringBuilder();
            for (int i = lines.length - 1; i >= 0; i--) {
                String line = lines[i];
                sb.append(line);
                if (i > 0) {
                    sb.append("\n");
                }
            }
            c = sb.toString();
        }
        header(n("serverlog"));
        put("serverlog", esc(c));
    }
}
