package at.innolab.magneticfield;

/**
 * Used to calculate the predicted field using BLE fingerprinting algorithm
 */
public class CalculateDifferenceBLE {

    /**
     * Used for getting the calculated position in a room based on fields
     *
     * @param gelb average of the RSSI of the yellow Beacon
     * @param lila average of the RSSI of the lila Beacon
     * @param pink average of the RSSI of the pink Beacon
     * @return the field number of the calculated position as int
     */
    public static int calculateDifferencePerFieldBLE(double gelb, double lila, double pink) {
        PositionBLE predictedPosition = null;
        double smallestDifference = 0;

        for (PositionBLE p : MainActivity.positionsBLE) {
            if (predictedPosition == null) {
                predictedPosition = p;
                smallestDifference = calculateDifferenceBLE(p, gelb, lila, pink);
            } else {
                if (calculateDifferenceBLE(p, gelb, lila, pink) < smallestDifference) {
                    predictedPosition = p;
                    smallestDifference = calculateDifferenceBLE(p, gelb, lila, pink);
                }
            }
        }
        return predictedPosition.getField();
    }

    /**
     * Calculate the difference of the pre measured RSSI and the actual RSSI
     *
     * @param p    the position for calculation with pre measured values
     * @param gelb the actual average RSSI of the yellow Beacon
     * @param lila the actual average RSSI of the lila Beacon
     * @param pink the actual average RSSI of the pink Beacon
     * @return the absolute difference as double
     */
    private static double calculateDifferenceBLE(PositionBLE p, double gelb, double lila, double pink) {
        return (Math.abs(p.getLila() - lila)) + (Math.abs(p.getGelb() - gelb)) + (Math.abs(p.getPink() - pink));
    }
}
