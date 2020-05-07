package at.innolab.magneticfield;

import java.util.ArrayList;
import java.util.List;

class CalculateFieldBLEDistance {
    private static final double GELB_1_M = -78.91;
    private static final double LILA_1_M = -78;
    private static final double PINK_1_M = -77.5;
    private static final double N = 1.5;
    private static final String BEACON_GELB = "gelb";
    private static final String BEACON_LILA = "lila";
    private static final String BEACON_PINK = "pink";

    public static int calculateField(double gelbAvg, double lilaAvg, double pinkAvg) {
        double gelbDistance = Double.isNaN(gelbAvg) ? -1 : Math.pow(10,(GELB_1_M-gelbAvg)/(10*N));
        double lilaDistance = Double.isNaN(lilaAvg) ? -1 : Math.pow(10,(LILA_1_M-lilaAvg)/(10*N));
        double pinkDistance = Double.isNaN(pinkAvg) ? -1 : Math.pow(10,(PINK_1_M-pinkAvg)/(10*N));

        List<PositionBLE> gelbPossibleFields = getFields(gelbDistance, BEACON_GELB);
        List<PositionBLE> lilaPossibleFields = getFields(lilaDistance, BEACON_LILA);
        List<PositionBLE> pinkPossibleFields = getFields(pinkDistance, BEACON_PINK);

        List<PositionBLE> possiblePositions;
        if((gelbPossibleFields.size() == 0)&&(lilaPossibleFields.size()!=0)&&(pinkPossibleFields.size() != 0)){
            possiblePositions = new ArrayList<>(lilaPossibleFields);
            possiblePositions.retainAll(pinkPossibleFields);
        }
        else if((gelbPossibleFields.size() == 0)&&(lilaPossibleFields.size()==0)&&(pinkPossibleFields.size() != 0)){
            possiblePositions = new ArrayList<>(pinkPossibleFields);
        }
        else if((gelbPossibleFields.size() == 0)&&(lilaPossibleFields.size()!=0)&&(pinkPossibleFields.size() == 0)){
            possiblePositions = new ArrayList<>(lilaPossibleFields);
        }
        else if((gelbPossibleFields.size() != 0)&&(lilaPossibleFields.size()!=0)&&(pinkPossibleFields.size() == 0)){
            possiblePositions = new ArrayList<>(gelbPossibleFields);
            possiblePositions.retainAll(lilaPossibleFields);
        }
        else if((gelbPossibleFields.size() != 0)&&(lilaPossibleFields.size()==0)&&(pinkPossibleFields.size() != 0)){
            possiblePositions = new ArrayList<>(gelbPossibleFields);
            possiblePositions.retainAll(pinkPossibleFields);
        }
        else if((gelbPossibleFields.size() != 0)&&(lilaPossibleFields.size()==0)&&(pinkPossibleFields.size() == 0)){
            possiblePositions = new ArrayList<>(gelbPossibleFields);
        }else {
            possiblePositions = new ArrayList<>(gelbPossibleFields);
            possiblePositions.retainAll(lilaPossibleFields);
            possiblePositions.retainAll(pinkPossibleFields);
        }
        double smallestDifference = 0;
        PositionBLE predictedPosition = null;
        for(PositionBLE position : possiblePositions){

            if (predictedPosition == null) {
                predictedPosition = position;
                smallestDifference = calculateDifferenceBLEDistance(position, gelbDistance, lilaDistance, pinkDistance);
            }
            else if(calculateDifferenceBLEDistance(position, gelbDistance, lilaDistance, pinkDistance) < smallestDifference){
                smallestDifference = calculateDifferenceBLEDistance(position, gelbDistance, lilaDistance, pinkDistance);
                predictedPosition = position;
            }

        }
        return predictedPosition.getField();
    }

    private static List<PositionBLE> getFields(double calculatedDistance, String beaconColor) {
        List<PositionBLE> possibleFields = new ArrayList<>();
        for(PositionBLE position : MainActivity.positionsBLEDistance){
            if((beaconColor.equals("gelb") && position.getGelb() <= calculatedDistance)){
                possibleFields.add(position);
            }
            if((beaconColor.equals("lila") && position.getLila() <= calculatedDistance)){
                possibleFields.add(position);
            }
            if((beaconColor.equals("pink") && position.getPink() <= calculatedDistance)){
                possibleFields.add(position);
            }
        }
        return possibleFields;
    }

    private static double calculateDifferenceBLEDistance(PositionBLE position, double gelbDistance, double lilaDistance, double pinkDistance){
        return (Math.abs(position.getLila() - lilaDistance)) + (Math.abs(position.getGelb() - gelbDistance)) + (Math.abs(position.getPink() - pinkDistance));
    }
}
