package at.innolab.magneticfield;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PositionReaderMF extends AsyncTask<InputStream, Void, List<PositionMF>> {
    @Override
    protected List<PositionMF> doInBackground(InputStream... inputStreams) {
        List<PositionMF> positions = new ArrayList<PositionMF>();
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset(inputStreams[0]));
            JSONArray array = obj.getJSONArray("data");
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                PositionMF p = new PositionMF();
                p.setX(o.getDouble("x-Achse"));
                p.setY(o.getDouble("y-Achse"));
                p.setZ(o.getDouble("z-Achse"));
                p.setField(o.getInt("Feld"));
                positions.add(p);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return positions;
    }

    @Override
    protected void onPostExecute(List<PositionMF> positions) {
        MainActivity.positionsMF = positions;
    }

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