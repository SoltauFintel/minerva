package minerva.mask;

import java.util.Arrays;
import java.util.stream.Collectors;

import minerva.book.BookPage;
import minerva.mask.field.MaskField;
import minerva.mask.field.MaskFieldType;
import minerva.seite.SPage;

public class EditFeatureFieldsPage extends SPage {

    @Override
    protected void execute() {
        MaskAndDataFields mad = new MaskAndDataFields(seite);
        if (isPOST()) {
            for (MaskField maskField : mad.getMaskFields()) {
                if (!maskField.isImportField()) {
                    mad.getDataFields().set(maskField.getId(), getValue(maskField));
                }
            }
            mad.save();
            
            ctx.redirect(viewlink);
        } else {
            BookPage.oneLang(model, book);
            header(seite.getTitle() + " (" + n("editFeatureFields") + ")");
            put("titel", esc(seite.getTitle()));
            put("featureFields", new FeatureFieldsHtml(seite, true).html());
            mad.customersMultiselect(model);
        }
    }

    private String getValue(MaskField maskField) {
        if (MaskFieldType.CUSTOMERS.equals(maskField.getType())) {
            String[] a = ctx.req.queryParamsValues(maskField.getId());
            return a == null ? "" : Arrays.asList(a).stream().collect(Collectors.joining(","));
        }
        String value = ctx.formParam(maskField.getId());
        if (value != null) {
            value = value.trim();
        }
        if (MaskFieldType.BOOL.equals(maskField.getType())) {
            value = "on".equals(value) ? "true" : "false";
        } else if (MaskFieldType.INTEGER.equals(maskField.getType())) {
            value = toInteger(value);
        }
        return value;
    }

    private String toInteger(String value) {
        String num = "";
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c >= '0' && c <= '9') {
                num += c;
            } else {
                break;
            }
        }
        return num;
    }
}
