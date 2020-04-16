package at.innolab.magneticfield;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private SensorManager sensorManager;
    private Sensor sensor;
    private TextView calibratedTextView;
    private Handler handler = new Handler();
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        calibratedTextView = findViewById(R.id.calibratedTextView);
        button = findViewById(R.id.button);

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
                float x = calcAvg(xValues);
                float y = calcAvg(yValues);
                float z = calcAvg(zValues);
                calibratedTextView.setText(x + "\n" + y + "\n" + z);
            }
        }, 1_000);
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
}
