import java.awt.*;

public class HSB {
    private float hue, bri, sat;

    public HSB(float hue, float sat, float bri) {
        this.hue = hue;
        this.bri = bri;
        this.sat = sat;
    }

    public float getHue() {
        return hue;
    }

    public float getBri() {
        return bri;
    }

    public float getSat() {
        return sat;
    }

    public void setHue(float hue) {
        this.hue = hue;
    }

    public void setBri(float bri) {
        this.bri = bri;
    }

    public void setSat(float sat) {
        this.sat = Math.min(1, sat);
    }

    public Color encodeColor() {
        return Color.getHSBColor(this.hue, this.sat, this.bri);
    }

    public static HSB decodeColor(Color color) {
        float[] hsb = Color.RGBtoHSB((int)(color.getRed() * 0.85f), (int)(color.getGreen()*0.85f), color.getBlue(), null);
        HSB returnHSB = new HSB(hsb[0], hsb[1], hsb[2]);
        if (returnHSB.getBri() < 0.02) {
            returnHSB.setBri(0);
            returnHSB.setSat(0);
        }
        return returnHSB;
    }

    @Override
    public String toString() {
        return "HSB{" +
                "hue=" + hue*359 +
                ", bri=" + bri*100 +
                ", sat=" + sat*100 +
                '}';
    }
}
