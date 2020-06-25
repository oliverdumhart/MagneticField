package at.innolab.magneticfield;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static List<PositionMF> positionsMF = new ArrayList<>();
    public static List<PositionBLE> positionsBLE = new ArrayList<>();
    public static List<PositionBLE> positionsBLEDistance = new ArrayList<>();

    private static final String BEACON_GELB_MAC = "DB:2A:D7:A7:97:56";
    private static final String BEACON_LILA_MAC = "F6:05:BC:12:72:F3";
    private static final String BEACON_PINK_MAC = "FD:3A:05:B5:97:C3";

    private static final long SCAN_PERIOD = 5_000;

    private BluetoothAdapter bluetoothAdapter;

    private List<Float> gelb = new ArrayList<>();
    private List<Float> lila = new ArrayList<>();
    private List<Float> pink = new ArrayList<>();

    private List<Float> xValues = new ArrayList<>();
    private List<Float> yValues = new ArrayList<>();
    private List<Float> zValues = new ArrayList<>();

    private SensorManager sensorManager;
    private Sensor sensor;
    private TextView calibratedTextView;
    private Handler handler = new Handler();
    private Button button;


    /**
     * Setup, read positions from JSON files, check for Bluetooth, check for magnetic field sensor
     *
     * @param savedInstanceState the saved state of the App if set, not used
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        calibratedTextView = findViewById(R.id.calibratedTextView);
        button = findViewById(R.id.button);

        PositionReaderMF readerMF = new PositionReaderMF();

        InputStream isMF = getResources().openRawResource(R.raw.position_mf);
        readerMF.execute(isMF);

        PositionReaderBLE readerBLE = new PositionReaderBLE();

        InputStream isBLE = getResources().openRawResource(R.raw.position_ble);
        readerBLE.execute(isBLE);

        PositionReaderBLEDistance readerBLEDistance = new PositionReaderBLEDistance();

        InputStream isBLEDistance = getResources().openRawResource(R.raw.position_ble_distance);
        readerBLEDistance.execute(isBLEDistance);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();


        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1234);
        }

        button.setOnClickListener(view -> {
                startListening();
        });
    }

    /**
     * Used to add the magnetic field sensor values to Lists if the sensor values change
     */
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

    /**
     * Used to start the prediction process of the actual position
     * Updates the UI after the prediction process has finished
     * Calculates the combined prediction of all single predictions
     */
    private void startListening() {
        button.setEnabled(false);
        xValues.clear();
        yValues.clear();
        zValues.clear();
        pink.clear();
        gelb.clear();
        lila.clear();


        sensorManager.registerListener(calibratedSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        scanLeDevice(true);

        handler.postDelayed(() -> {
            stopListening();
            bluetoothAdapter.stopLeScan(leScanCallback);

            int predictedFieldMF;
            int predictedFieldBLE;
            int predictedFieldBLEDistance;
            double x = calcAvg(xValues);
            double y = calcAvg(yValues);
            double z = calcAvg(zValues);
            double pinkAvg = calcAvg(pink);
            double lilaAvg = calcAvg(lila);
            double gelbAvg = calcAvg(gelb);
            int combinedPredictedField;

            predictedFieldMF = CalculateDifferenceMF.calculateDifferencePerFieldMF(x, y, z);
            predictedFieldBLEDistance = CalculateFieldBLEDistance.calculateField(gelbAvg, lilaAvg, pinkAvg);
            if (!Double.isNaN(gelbAvg) && !Double.isNaN(pinkAvg) && !Double.isNaN(lilaAvg)) {
                predictedFieldBLE = CalculateDifferenceBLE.calculateDifferencePerFieldBLE(gelbAvg, lilaAvg, pinkAvg);
                combinedPredictedField = (int) Math.round((0.25 * predictedFieldMF + 0.5 * predictedFieldBLEDistance + 0.25 * predictedFieldBLE));
            } else {
                predictedFieldBLE = -1;
                combinedPredictedField = (int) Math.round((0.5 * predictedFieldMF + 0.5 * predictedFieldBLEDistance));
            }


            calibratedTextView.setText("BLE:\nGelb:" + gelbAvg + "\nLila: " + lilaAvg + "\nPink: " + pinkAvg
                    + "\nMF:\n" +
                    x + "\n" + y + "\n" + z + "\nPredicted Field MF:" + predictedFieldMF + "\nPredictedField BLE:" + predictedFieldBLE
                    + "\nPredictedField BLE Distance:" + predictedFieldBLEDistance
                    + "\nCombinedPredictedField: " + combinedPredictedField);
        }, SCAN_PERIOD);
    }

    /**
     * Used for actual scanning
     * Saves the measured RSSIs in a List depending on the Beacon
     * For each Beacon an individual list gets populated
     */
    private BluetoothAdapter.LeScanCallback leScanCallback =
            (device, rssi, scanRecord) -> runOnUiThread(() -> {
                if (device.getAddress().equals(BEACON_GELB_MAC)
                        || device.getAddress().equals(BEACON_LILA_MAC)
                        || device.getAddress().equals(BEACON_PINK_MAC)
                ) {
                    if (device.getAddress().equals(BEACON_GELB_MAC)) {
                        gelb.add((float) rssi);
                    }
                    if (device.getAddress().equals(BEACON_LILA_MAC)) {
                        lila.add((float) rssi);
                    }
                    if (device.getAddress().equals(BEACON_PINK_MAC)) {
                        pink.add((float) rssi);
                    }
                }
            });


    /**
     * Used for start and stop scanning BLE devices
     *
     * @param enable boolean true to start scanning and false to stop scanning
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            bluetoothAdapter.startLeScan(leScanCallback);
        } else {
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }


    /**
     * Update UI and unregister magnetic field sensor
     */
    private void stopListening() {
        button.setEnabled(true);
        sensorManager.unregisterListener(calibratedSensorListener);
    }


    /**
     * Calculates the average of the measured RSSIs per Beacons or the magnetic field sensor values
     * @param values List of the measured RSSIs per Beacons or per magnetic field sensor values
     * @return the average of measured RSSIs or magnetic field sensor values as float
     */
    private float calcAvg(List<Float> values) {
        float sum = 0;
        for (float v : values) {
            sum += v;
        }
        return sum / values.size();
    }

    /**
     * Unregister magnetic field sensor
     */
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(calibratedSensorListener);
    }

}
