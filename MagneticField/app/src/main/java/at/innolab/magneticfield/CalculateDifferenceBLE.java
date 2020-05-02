package at.innolab.magneticfield;

public class CalculateDifferenceBLE {
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

    private static double calculateDifferenceBLE(PositionBLE p, double gelb, double lila, double pink) {
        return (Math.abs(p.getLila() - lila)) + (Math.abs(p.getGelb() - gelb)) + (Math.abs(p.getPink() - pink));
    }
}
