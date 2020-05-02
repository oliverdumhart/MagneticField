package at.innolab.magneticfield;

public class CalculateDifferenceMF {
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

    private static double calculateDifferenceMF(PositionMF p, double x, double y, double z) {
        return (Math.abs(p.getX() - x)) + (Math.abs(p.getY() - y)) + (Math.abs(p.getZ() - z));
    }
}
