package at.innolab.magneticfield;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to calculate the predicted field using a slightly adapted BLE trilateration algorithm
 */
class CalculateFieldBLEDistance {
    private static final double GELB_1_M = -78.91;
    private static final double LILA_1_M = -78;
    private static final double PINK_1_M = -77.5;
    private static final double N = 1.5;
    private static final String BEACON_GELB = "gelb";
    private static final String BEACON_LILA = "lila";
    private static final String BEACON_PINK = "pink";

    /**
     * Used for getting the calculated position in a room based on fields
     *
     * @param gelbAvg average of the RSSI of the yellow Beacon
     * @param lilaAvg average of the RSSI of the lila Beacon
     * @param pinkAvg average of the RSSI of the pink Beacon
     * @return the field number of the calculated position as int or -1 if no signals were received
     */
    public static int calculateField(double gelbAvg, double lilaAvg, double pinkAvg) {
        if ((Double.isNaN(gelbAvg) && Double.isNaN(lilaAvg) && Double.isNaN(pinkAvg))) {
            return -1;
        }
        double gelbDistance = Double.isNaN(gelbAvg) ? -1 : Math.pow(10, (GELB_1_M - gelbAvg) / (10 * N));
        double lilaDistance = Double.isNaN(lilaAvg) ? -1 : Math.pow(10, (LILA_1_M - lilaAvg) / (10 * N));
        double pinkDistance = Double.isNaN(pinkAvg) ? -1 : Math.pow(10, (PINK_1_M - pinkAvg) / (10 * N));

        List<PositionBLE> gelbPossibleFields = getFields(gelbDistance, BEACON_GELB);
        List<PositionBLE> lilaPossibleFields = getFields(lilaDistance, BEACON_LILA);
        List<PositionBLE> pinkPossibleFields = getFields(pinkDistance, BEACON_PINK);

        List<PositionBLE> possiblePositions;
        if ((gelbPossibleFields.size() == 0) && (lilaPossibleFields.size() != 0) && (pinkPossibleFields.size() != 0)) {
            possiblePositions = new ArrayList<>(lilaPossibleFields);
            possiblePositions.retainAll(pinkPossibleFields);
        } else if ((gelbPossibleFields.size() == 0) && (lilaPossibleFields.size() == 0) && (pinkPossibleFields.size() != 0)) {
            possiblePositions = new ArrayList<>(pinkPossibleFields);
        } else if ((gelbPossibleFields.size() == 0) && (lilaPossibleFields.size() != 0) && (pinkPossibleFields.size() == 0)) {
            possiblePositions = new ArrayList<>(lilaPossibleFields);
        } else if ((gelbPossibleFields.size() != 0) && (lilaPossibleFields.size() != 0) && (pinkPossibleFields.size() == 0)) {
            possiblePositions = new ArrayList<>(gelbPossibleFields);
            possiblePositions.retainAll(lilaPossibleFields);
        } else if ((gelbPossibleFields.size() != 0) && (lilaPossibleFields.size() == 0) && (pinkPossibleFields.size() != 0)) {
            possiblePositions = new ArrayList<>(gelbPossibleFields);
            possiblePositions.retainAll(pinkPossibleFields);
        } else if ((gelbPossibleFields.size() != 0) && (lilaPossibleFields.size() == 0) && (pinkPossibleFields.size() == 0)) {
            possiblePositions = new ArrayList<>(gelbPossibleFields);
        } else {
            possiblePositions = new ArrayList<>(gelbPossibleFields);
            possiblePositions.retainAll(lilaPossibleFields);
            possiblePositions.retainAll(pinkPossibleFields);
        }
        double smallestDifference = 0;
        PositionBLE predictedPosition = null;
        for (PositionBLE position : possiblePositions) {

            if (predictedPosition == null) {
                predictedPosition = position;
                smallestDifference = calculateDifferenceBLEDistance(position, gelbDistance, lilaDistance, pinkDistance);
            } else if (calculateDifferenceBLEDistance(position, gelbDistance, lilaDistance, pinkDistance) < smallestDifference) {
                smallestDifference = calculateDifferenceBLEDistance(position, gelbDistance, lilaDistance, pinkDistance);
                predictedPosition = position;
            }

        }
        return predictedPosition.getField();
    }

    /**
     * Calculates the possible field, which are within the calculated distance of the Beacon
     *
     * @param calculatedDistance the calculated distance from one Beacon to the device
     * @param beaconColor        the color of the Beacon for which the fields should be calculated
     * @return a List of possible positions
     */
    private static List<PositionBLE> getFields(double calculatedDistance, String beaconColor) {
        List<PositionBLE> possibleFields = new ArrayList<>();
        for (PositionBLE position : MainActivity.positionsBLEDistance) {
            if ((beaconColor.equals("gelb") && position.getGelb() <= calculatedDistance)) {
                possibleFields.add(position);
            }
            if ((beaconColor.equals("lila") && position.getLila() <= calculatedDistance)) {
                possibleFields.add(position);
            }
            if ((beaconColor.equals("pink") && position.getPink() <= calculatedDistance)) {
                possibleFields.add(position);
            }
        }
        return possibleFields;
    }

    /**
     * Calculate the difference of the pre calculated distances from the Beacons to the field centers
     * and the actual distance from the device to the Beacons
     *
     * @param position     the position for calculation with pre measured values
     * @param gelbDistance the calculated distance from the device to the yellow Beacon
     * @param lilaDistance the calculated distance from the device to the lila Beacon
     * @param pinkDistance the calculated distance from the device to the pink Beacon
     * @return the absolute difference as double
     */
    private static double calculateDifferenceBLEDistance(PositionBLE position, double gelbDistance, double lilaDistance, double pinkDistance) {
        return (Math.abs(position.getLila() - lilaDistance)) + (Math.abs(position.getGelb() - gelbDistance)) + (Math.abs(position.getPink() - pinkDistance));
    }
}
