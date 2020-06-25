package at.innolab.magneticfield;

/**
 * Representation of one field in the test-room
 * Contains either
 *      the pre measured RSSI values for the 3 Beacons and the field number
 *      or
 *      the pre calculated distance from the field center to the 3 Beacons and the field number
 */
public class PositionBLE {
    private double gelb;
    private double lila;
    private double pink;
    private int field;

    public double getGelb() {
        return gelb;
    }

    public void setGelb(double gelb) {
        this.gelb = gelb;
    }

    public double getLila() {
        return lila;
    }

    public void setLila(double lila) {
        this.lila = lila;
    }

    public double getPink() {
        return pink;
    }

    public void setPink(double pink) {
        this.pink = pink;
    }

    public int getField() {
        return field;
    }

    public void setField(int field) {
        this.field = field;
    }
}
