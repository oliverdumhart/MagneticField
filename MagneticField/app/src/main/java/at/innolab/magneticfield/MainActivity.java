package at.innolab.magneticfield;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    private SensorManager sensorManager;
    private Sensor uncalibratedSensor;
    private Sensor calibratedSensor;
    private TextView calibratedTextView;
    private TextView uncalibratedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        uncalibratedSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        calibratedSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        calibratedTextView = findViewById(R.id.calibratedTextView);
        uncalibratedTextView = findViewById(R.id.uncalibratedTextView);
    }

    private SensorEventListener calibratedSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            StringBuilder builder = new StringBuilder();
            for(float v : sensorEvent.values)
                builder.append(v);
            
            calibratedTextView.setText(builder.toString());
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    private SensorEventListener uncalibratedSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            StringBuilder builder = new StringBuilder();
            for(float v : sensorEvent.values)
                builder.append(v);

            uncalibratedTextView.setText(builder.toString());
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(uncalibratedSensorListener, uncalibratedSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(calibratedSensorListener, calibratedSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(uncalibratedSensorListener);
        sensorManager.unregisterListener(calibratedSensorListener);
    }
}
