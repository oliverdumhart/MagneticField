package at.innolab.magneticfield;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private SensorManager sensorManager;
    private Sensor sensor;
    private TextView calibratedTextView;
    private Handler handler = new Handler();
    private Button button;
    private List<Position> positions = new ArrayList<Position>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        calibratedTextView = findViewById(R.id.calibratedTextView);
        button = findViewById(R.id.button);

        PositionReader reader = new PositionReader();

        InputStream is = getResources().openRawResource(R.raw.position);
        reader.execute(is);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startListening();
            }
        });
    }

    private List<Float> xValues = new ArrayList<>();
    private List<Float> yValues = new ArrayList<>();
    private List<Float> zValues = new ArrayList<>();

    private SensorEventListener calibratedSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            xValues.add(sensorEvent.values[0]);
            yValues.add(sensorEvent.values[1]);
            zValues.add(sensorEvent.values[2]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    private void startListening(){
        button.setEnabled(false);
        xValues.clear();
        yValues.clear();
        zValues.clear();

        sensorManager.registerListener(calibratedSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopListening();
                int predictedField;
                double x = calcAvg(xValues);
                double y = calcAvg(yValues);
                double z = calcAvg(zValues);

                predictedField = calculateDifferencePerField(x, y, z);
                calibratedTextView.setText(x + "\n" + y + "\n" + z + "\nPredicted Field:" + predictedField);
            }
        }, 1_000);
    }

    private int calculateDifferencePerField(double x, double y, double z) {
        Position predictedPosition = null;
        double smallestDifference = 0;

        for(Position p : positions){
            if(predictedPosition == null){
                predictedPosition = p;
                smallestDifference = calculateDifference(p, x, y, z);
            }else
            {
                if(calculateDifference(p, x, y, z)< smallestDifference){
                    predictedPosition = p;
                    smallestDifference = calculateDifference(p, x, y, z);
                }
            }
        }
        return predictedPosition.getField();
    }

    private double calculateDifference(Position p, double x, double y, double z) {
        return (Math.abs(p.getX()-x))+(Math.abs(p.getY()-y))+(Math.abs(p.getZ()-z));
    }

    private void stopListening(){
        button.setEnabled(true);
        sensorManager.unregisterListener(calibratedSensorListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private float calcAvg(List<Float> values){
        float sum = 0;
        for(float v : values){
            sum += v;
        }
        return sum/values.size();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(calibratedSensorListener);
    }

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
        protected void onPostExecute(List<Position> positions) {
            MainActivity.this.positions = positions;
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
}
