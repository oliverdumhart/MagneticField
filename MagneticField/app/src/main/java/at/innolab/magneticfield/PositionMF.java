package at.innolab.magneticfield;

/**
 * Representation of one field in the test-room
 * Contains the values for the 3 axes of the magnetic field sensor and the field number
 */
public class PositionMF {
    private double x;
    private double y;
    private double z;
    private int field;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public int getField() {
        return field;
    }

    public void setField(int field) {
        this.field = field;
    }
}
