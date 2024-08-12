package com.example.beacontest1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import android.widget.CompoundButton;

public class SignalHandler extends AppCompatActivity implements BeaconConsumer {

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;

    private Button btscan;
    private ImageView beaconImage;
    private ListView devicesList;
    private ArrayAdapter<String> listAdapter;
    private DbManager dbManager;
    private List<Double> distancesUuid1List = new ArrayList<>();
    private List<Double> distancesUuid2List = new ArrayList<>();
    private List<Double> distancesUuid3List = new ArrayList<>();
    private BeaconManager beaconManager;
    public  String displayInfo1;
    private int rssi ;
    private double d_noiseless ;
    private String uuid1 ;
    private String uuid11;
    private double meanUuid1;
    private double meanUuid2 ;
    private double meanUuid3;

    //    String A_beacon= "0x11111111111111111111";
//    String B_beacon= "0x22222222222222222222";
//    String C_beacon= "0x33333333333333333333";
    int distanceFlag = 100;
    double A = -16.0 ;
    double noise_value = 2.6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_view);

        dbManager = DbManager.instanceOfDataBase(this);
//        devicesList = findViewById(R.id.listView);
        btscan = findViewById(R.id.scanButton);
        beaconImage = findViewById(R.id.beaconImage);
//        swGoToLogs = findViewById(R.id.GoToLogs);
        // ~*~  Here we have to create an array adapter to Display Which device has been detected
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
//        devicesList.setAdapter(listAdapter);

//  !~ The logic of the code to handle the use cases start from the function onBeaconServiceConnect() .


    }


    //  This is the onclick method for the Scan Button
    public void startScan(View view) {
        btscan.setText("The Scanning has been started ");
        getLocPermission();
    }
//We are getting premission to access the device gps
    private void getLocPermission(){
        // Request location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_FINE_LOCATION);
        } else {
            initBeaconManager();
        }
    }

    private void initBeaconManager() {
        beaconManager = BeaconManager.getInstanceForApplication(this);

        // Set the beacon layout for Eddystone-UID
        beaconManager.getBeaconParsers().add(
                new org.altbeacon.beacon.BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT)
        );
        // Bind the BeaconManager to this Activity
        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initBeaconManager();
            } else {
                // Handle permission denied
                Toast.makeText(this, "Location permission denied. Cannot scan for beacons.", Toast.LENGTH_SHORT).show();
            }
        }
    }
// Here we calculate the distance based on the preCalculated A and n
    public static double calculateNoiselessDistance(double A, double current_RSSI, double noise_value) {
        double d0 = 1.0; // Reference distance in meters
        return Math.pow(10, (A - current_RSSI) / (10 * noise_value));
    }

// Here we calculate the mean  of a list so we can handle the case of the device beeing equidistant from two beacons.
    private double calculateMean(List<Double> values) {

        Log.i("Calc Mean", "Last 5 values for UUID1: " + values);
        if (values == null || values.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.size();

    }


    private void setImage() {

        Log.i("LogActivity!!! For mean Image Display ", "meanUuid1: " + meanUuid1 + ", meanUuid2: " + meanUuid2 + ", meanUuid3: " + meanUuid3);
        if(meanUuid1 == 0){
            meanUuid1 = 1000000;
        }
        if(meanUuid2 == 0){
            meanUuid2 = 1000000;
        }
        if(meanUuid3 == 0){
            meanUuid3 = 1000000;
        }
        if (meanUuid1 < meanUuid2 && meanUuid1 < meanUuid3 )  {
            beaconImage.setImageResource(R.drawable.lettera);
        }
        if (meanUuid2 < meanUuid1 && meanUuid2 < meanUuid3) {
            beaconImage.setImageResource(R.drawable.letterb);
        }
        if (meanUuid3 < meanUuid1 && meanUuid3 < meanUuid2) {
            beaconImage.setImageResource(R.drawable.letterc);
        }
   }

//   This is a generic code that  allows me to use as much values to calculate the meanValue as I want
    private List<Double> getLastValues(List<Double> list, int numberOfValues) {
        int size = list.size();
        if (size <= numberOfValues) {
            return new ArrayList<>(list);
        } else {
            return new ArrayList<>(list.subList(size - numberOfValues, size));
        }
    }
// Here we try to classify the  signals to our database  based on the message transmitted.
    private void handleDb() {
        if (!distancesUuid1List.isEmpty() && uuid11.contains("1")) {
//      If we found 1 on the msg We create a new List that takes the calc distances of that beacon
//      So that we have our values to calculate the mean
            List<Double> last5Values1 = getLastValues(distancesUuid1List, 3);
            meanUuid1 = calculateMean(last5Values1);
            Log.i("MeanActivity", "Last 5 values for UUID1: " + last5Values1);
            Log.i("LogActivity", "we have calculated the mean 1 " + meanUuid1);
//            Here we store the nessery metrics into a db
            dbManager.addRecordToDb(uuid1, rssi, d_noiseless, meanUuid1, distanceFlag);
        }
        if (!distancesUuid2List.isEmpty()&&uuid11.contains("2")) {
            List<Double> last5Values2 = getLastValues(distancesUuid2List, 3);
            meanUuid2 = calculateMean(last5Values2);
            Log.i("LogActivity", "Last 5 values for UUID2: " + last5Values2);
            Log.i("LogActivity", "we have calculated the mean 2 " + meanUuid2);
            dbManager.addRecordToDb(uuid1, rssi, d_noiseless, meanUuid2, distanceFlag);
        }
        if (!distancesUuid3List.isEmpty() && uuid11.contains("3")) {
            List<Double> last5Values3 = getLastValues(distancesUuid3List, 3);
            meanUuid3 = calculateMean(last5Values3);
            Log.i("LogActivity", "Last 5 values for UUID3: " + last5Values3);
            Log.i("LogActivity", "we have calculated the mean 3 " + meanUuid3);
            dbManager.addRecordToDb(uuid1, rssi, d_noiseless, meanUuid3, distanceFlag);
        }
//      Then We go to setImage and based on the mean calculations that have beed done We display an
//      Icon/msg
        setImage();
    }
// If we detect The signal
    private  void handleDetectedSignal(){
        if (uuid11.contains("1")) {
            Log.i("LogActivity", "we are in uuid11  before adding to the list ");
            distancesUuid1List.add(d_noiseless);
            Toast.makeText(SignalHandler.this, "Found BLE MATCH For beacon 1", Toast.LENGTH_SHORT).show();
            handleDb();
        }
        if (uuid11.contains("2")) {
            Log.i("LogActivity", "we are in uuid22  before adding to the list ");
            distancesUuid2List.add(d_noiseless);
            Toast.makeText(SignalHandler.this, "Found BLE MATCH For beacon 2", Toast.LENGTH_SHORT).show();
            handleDb();
        }
        if (uuid11.contains("3")) {
            Log.i("LogActivity", "we are in uuid33  before adding to the list ");
            distancesUuid3List.add(d_noiseless);
            Toast.makeText(SignalHandler.this, "Found BLE MATCH For beacon 3", Toast.LENGTH_SHORT).show();
            handleDb();
        }

    }
// Here if we detect a beacon this code starts running. Creats a list that reads all the incoming signals.
    @Override
    public void onBeaconServiceConnect() {
        Region region = new Region("all-beacons-region", null, null, null);

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if(beacons != null) {
                    for (Beacon beacon : beacons) {
//                  Now from every signal that we have collected We are taking the Uuid's
                        // Retrieve the UUID from the Eddystone UID frame
                         uuid1 = beacon.getId1().toString();
                         uuid11 = beacon.getId2().toString();
//                  Below we are retrieving the Rssi from the Library Alt.Beacon we are using.
                         rssi = beacon.getRssi();
//                  Below we are calculating  the distance based on the noiseless formula
                        d_noiseless = calculateNoiselessDistance(A,rssi,noise_value);

                        displayInfo1 = "UUIDmax: " + uuid1 + "\nUUIDmin: " + uuid11 + "\n RSSi: " + rssi+ "\n d_noise: " +d_noiseless;
                        Log.d("Beacon", displayInfo1);
                        Log.d("Distance", String.valueOf(d_noiseless));

//                  After the reading signals of the beacons and necessary calculations. We procced
//                  with the HandleDetecctedSignals function.

                        handleDetectedSignal();
//                        handleDb();
                        listAdapter.add(displayInfo1);
                        listAdapter.notifyDataSetChanged();

                    }
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}