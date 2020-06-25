package at.innolab.magneticfield;

/**
 * Used to calculate the predicted field using Magnetic Field fingerprinting algorithm
 */
public class CalculateDifferenceMF {

    /**
     * Used for getting the calculated position in a room based on fields
     *
     * @param x the x value of the magnetic field sensor
     * @param y the y value of the magnetic field sensor
     * @param z the z value of the magnetic field sensor
     * @return the field number of the calculated position as int
     */
    public static int calculateDifferencePerFieldMF(double x, double y, double z) {
        PositionMF predictedPosition = null;
        double smallestDifference = 0;

        for (PositionMF p : MainActivity.positionsMF) {
            if (predictedPosition == null) {
                predictedPosition = p;
                smallestDifference = calculateDifferenceMF(p, x, y, z);
            } else {
                if (calculateDifferenceMF(p, x, y, z) < smallestDifference) {
                    predictedPosition = p;
                    smallestDifference = calculateDifferenceMF(p, x, y, z);
                }
            }
        }
        return predictedPosition.getField();
    }

    /**
     * Calculate the difference of the pre measured magnetic field values and the actual magnetic field values
     *
     * @param p the position for calculation with pre measured values
     * @param x the x value of the magnetic field sensor
     * @param y the y value of the magnetic field sensor
     * @param z the z value of the magnetic field sensor
     * @return the absolute difference as double
     */
    private static double calculateDifferenceMF(PositionMF p, double x, double y, double z) {
        return (Math.abs(p.getX() - x)) + (Math.abs(p.getY() - y)) + (Math.abs(p.getZ() - z));
    }
}
