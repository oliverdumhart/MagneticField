package at.innolab.magneticfield;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
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

    private static final String BEACON_GELB_MAC = "DB:2A:D7:A7:97:56";
    private static final String BEACON_LILA_MAC = "F6:05:BC:12:72:F3";
    private static final String BEACON_PINK_MAC = "FD:3A:05:B5:97:C3";

    /*private static final String BEACON_GELB_MAC = "F2:1F:C9:BF:86:46";
    private static final String BEACON_LILA_MAC = "C1:05:DE:50:D3:74";
    private static final String BEACON_PINK_MAC = "F0:F5:46:78:6E:2D";*/


    private static final long SCAN_PERIOD = 2_000;
    private BluetoothAdapter bluetoothAdapter;
    private boolean mScanning;


    private List<Float> gelb = new ArrayList<>();
    private List<Float> lila = new ArrayList<>();
    private List<Float> pink = new ArrayList<>();

    private SensorManager sensorManager;
    private Sensor sensor;
    private TextView calibratedTextView;
    private Handler handler = new Handler();
    private Button button;
    private List<Position> positions = new ArrayList<Position>();

    public static List<PositionMF> positionsMF = new ArrayList<>();
    public static List<PositionBLE> positionsBLE = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        calibratedTextView = findViewById(R.id.calibratedTextView);
        button = findViewById(R.id.button);

        PositionReader reader = new PositionReader();
        PositionReaderMF readerMF = new PositionReaderMF();

        InputStream is = getResources().openRawResource(R.raw.position);
        reader.execute(is);
        InputStream isMF = getResources().openRawResource(R.raw.position_mf);
        readerMF.execute(isMF);

        PositionReaderBLE readerBLE = new PositionReaderBLE();

        InputStream isBLE = getResources().openRawResource(R.raw.position_ble);
        readerBLE.execute(isBLE);

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

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopListening();
                int predictedField;
                mScanning = false;
                bluetoothAdapter.stopLeScan(leScanCallback);

                int predictedFieldMF;
                int predictedFieldBLE;
                double x = calcAvg(xValues);
                double y = calcAvg(yValues);
                double z = calcAvg(zValues);
                double pinkAvg = calcAvg(pink);
                double lilaAvg = calcAvg(lila);
                double gelbAvg = calcAvg(gelb);

                predictedField = calculateDifferencePerField(x, y, z);
                calibratedTextView.setText(x + "\n" + y + "\n" + z + "\nPredicted Field:" + predictedField);
                predictedFieldMF = CalculateDifferenceMF.calculateDifferencePerFieldMF(x, y, z);
                if(!Double.isNaN(gelbAvg) && !Double.isNaN(pinkAvg) && !Double.isNaN(lilaAvg)) {
                    predictedFieldBLE = CalculateDifferenceBLE.calculateDifferencePerFieldBLE(gelbAvg, lilaAvg, pinkAvg);
                }
                else{
                    predictedFieldBLE = -1;
                }
                calibratedTextView.setText("BLE:\nGelb:" + gelbAvg + "\nLila: " + lilaAvg + "\nPink: " + pinkAvg
                        + "\nMF:\n" +
                        x + "\n" + y + "\n" + z + "\nPredicted Field MF:" + predictedFieldMF + "\nPredictedField BLE:" + predictedFieldBLE);
            }
        }, 1_000);
        }, SCAN_PERIOD);
    }

    private int calculateDifferencePerField(double x, double y, double z) {
        Position predictedPosition = null;
        double smallestDifference = 0;























    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
                        }
                    });
                }
            };
















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

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mScanning = true;
            bluetoothAdapter.startLeScan(leScanCallback);
        } else {
            mScanning = false;
            bluetoothAdapter.stopLeScan(leScanCallback);


        }
        return predictedPosition.getField();
    }

    private double calculateDifference(Position p, double x, double y, double z) {
        return (Math.abs(p.getX()-x))+(Math.abs(p.getY()-y))+(Math.abs(p.getZ()-z));
    }




    private void stopListening(){





    private void stopListening() {

















        button.setEnabled(true);
        sensorManager.unregisterListener(calibratedSensorListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private float calcAvg(List<Float> values){
    private float calcAvg(List<Float> values) {
        float sum = 0;
        for(float v : values){
        for (float v : values) {
            sum += v;
        }
        return sum/values.size();
        return sum / values.size();
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
}


























































