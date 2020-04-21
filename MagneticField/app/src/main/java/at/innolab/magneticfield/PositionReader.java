package at.innolab.magneticfield;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PositionReader extends AsyncTask<InputStream, Void, List<Position>> {
    @Override
    protected List<Position> doInBackground(InputStream... inputStreams) {
        List<Position> positions = new ArrayList<Position>();
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset(inputStreams[0]));
            JSONArray array = obj.getJSONArray("data");
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                Position p = new Position();
                p.setX(o.getInt("x-Achse"));
                p.setY(o.getInt("y-Achse"));
                p.setZ(o.getInt("z-Achse"));
                p.setField(o.getInt("Feld"));
                positions.add(p);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return positions;
    }

    @Override
    protected void onPostExecute(List<Position> positions) {
        super.onPostExecute(positions);
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
