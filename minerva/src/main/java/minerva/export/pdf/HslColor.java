package minerva.export.pdf;

public class HslColor {
    private final int _h;
    private final int _s;
    private final int _l;

    /**
     * @param hslString e.g. "hsl(240, 75%, 60%)"
     * @return HslColor or null if hslString is invalid
     */
    public static HslColor fromHSL(String hslString) {
        if (hslString.startsWith("hsl(") && hslString.endsWith(")")) {
            String inner = hslString.substring("hsl(".length(), hslString.length() - ")".length());
            String[] w = inner.split(",");
            if (w.length == 3) {
                int h = Integer.parseInt(w[0].trim());
                int s = Integer.parseInt(w[1].trim().replace("%", ""));
                int l = Integer.parseInt(w[2].trim().replace("%", ""));
                return new HslColor(h, s, l);
            }
        }
        return null;
    }
    
    /**
     * @param h hue
     * @param s saturation % (0-100)
     * @param l lightness % (0-100)
     */
    public HslColor(int h, int s, int l) {
        if (h < 0 || h >= 360) {
            throw new IllegalArgumentException("Value for hue (is " + h + ") must be in range 0 to 359.");
        }
        if (s < 0 || s > 100 || l < 0 || l > 100) {
            throw new IllegalArgumentException(
                    "Value for saturation (is " + s + ") and lightness (is " + l + ") must be in range 0 to 100.");
        }
        _h = h;
        _s = s;
        _l = l;
    }
    
    // https://stackoverflow.com/a/66000986/3478021
    public String getHexRGBColor() {
        float r, g, b;
        float h = _h / 359.0f;
        float s = _s / 100.0f;
        float l = _l / 100.0f;
        if (s == 0f) {
            r = g = b = l; // achromatic
        } else {
            float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hueToRgb(p, q, h + 1f / 3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1f / 3f);
        }
        return "#" + hex(r) + hex(g) + hex(b);
    }

    private float hueToRgb(float p, float q, float t) {
        if (t < 0f) {
            t += 1f;
        }
        if (t > 1f) {
            t -= 1f;
        }
        if (t < 1f / 6f) {
            return p + (q - p) * 6f * t;
        } else if (t < 1f / 2f) {
            return q;
        } else if (t < 2f / 3f) {
            return p + (q - p) * (2f / 3f - t) * 6f;
        } else {
            return p;
        }
    }
    
    private String hex(float v) {
        int number = (int) Math.min(255, 256 * v);
        String ret = Integer.toHexString(number);
        return ret.length() == 1 ? "0" + ret : ret;
    }
    
    /** dev testcase */
    public static void main(String[] args) {
        String res = HslColor.fromHSL("hsl(30, 75%, 60%)").getHexRGBColor();
        System.out.println(res + " is " + ("#e6994c".equals(res) ? "ok" : "wrong"));
    }
}
