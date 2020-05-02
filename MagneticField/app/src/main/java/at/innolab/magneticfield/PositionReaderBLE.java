package at.innolab.magneticfield;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PositionReaderBLE extends AsyncTask<InputStream, Void, List<PositionBLE>> {
    @Override
    protected List<PositionBLE> doInBackground(InputStream... inputStreams) {
        List<PositionBLE> positions = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset(inputStreams[0]));
            JSONArray array = obj.getJSONArray("data");
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                PositionBLE p = new PositionBLE();
                p.setGelb(o.getDouble("gelb"));
                p.setLila(o.getDouble("lila"));
                p.setPink(o.getDouble("pink"));
                p.setField(o.getInt("feld"));
                positions.add(p);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return positions;
    }

    @Override
    protected void onPostExecute(List<PositionBLE> positions) {
        MainActivity.positionsBLE = positions;
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
