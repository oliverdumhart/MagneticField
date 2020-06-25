package at.innolab.magneticfield;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the BLE Distance JSON data
 * Used to read the pre defined position data from a JSON file and return a List of positions to
 * the Main Activity
 */
public class PositionReaderBLEDistance extends AsyncTask<InputStream, Void, List<PositionBLE>> {
    /**
     * Reads the position data from a JSON file and generates a list of positions
     *
     * @param inputStreams the inputstream from the JSON file
     * @return a List of positions
     */
    @Override
    protected List<PositionBLE> doInBackground(InputStream... inputStreams) {
        List<PositionBLE> positions = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset(inputStreams[0]));
            JSONArray array = obj.getJSONArray("data");
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                PositionBLE p = new PositionBLE();
                p.setGelb(o.getDouble("Gelb"));
                p.setLila(o.getDouble("Lila"));
                p.setPink(o.getDouble("Pink"));
                p.setField(o.getInt("Feld"));
                positions.add(p);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return positions;
    }

    /**
     * Updates the List of positionsBLEDistance in the MainActivity after the AsyncTask finished
     *
     * @param positions List of the read positions
     */
    @Override
    protected void onPostExecute(List<PositionBLE> positions) {
        MainActivity.positionsBLEDistance = positions;
    }

    /**
     * Loads the content of the JSON file
     *
     * @param is Inputstream of the JSON file
     * @return the content of the file as string
     */
    public String loadJSONFromAsset(InputStream is) {
        String json = null;
        try {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
