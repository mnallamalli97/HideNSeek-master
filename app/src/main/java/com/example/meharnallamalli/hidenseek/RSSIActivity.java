package com.example.meharnallamalli.hidenseek;
//
//import android.Manifest;
//import android.app.Activity;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.os.Handler;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.os.Vibrator;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//import static com.example.meharnallamalli.hidenseek.Utils.getLocationPermission;
//
///**
// * Created by meharnallamalli on 1/12/17.
// */
//
//public class RSSIActivity extends Activity {
//
//    private BluetoothAdapter mBluetoothAdapter;
//    private static final int REQUEST_ENABLE_BT = 1;    //must be greater than 0
//    private static final int REQUEST_COARSE_LOCATION = 1;
//
//    TextView mLabel;
//    Button mCancelButton;
//    Button mDiscoverButton;
//    Button mStartPartyButton;
//    ProgressBar mProgressBar;
//    RecyclerView mRecyclerView;
//    RSSIListAdapter mAdapter;
//    RecyclerView.LayoutManager mLayoutManager;
//
//    private Context mContext;
//    RSSIActivity thisActivity = this;
//
//    ArrayList<Device> deviceList;
//    ArrayList<Device> partyList;                                                                    //PARTY RESET
//
//    private boolean canStartDiscovering = false;
//    private boolean selectedParty = false;                                                          //PARTY RESET
//
//    //for debug purposes
//    private final static String TAG = RSSIActivity.class.getSimpleName();
//
//    private Runnable runnable;
//    private Handler handler;
//    private final int interval = 3000; // 3 Seconds
//
//    // To sample RSSI trend
//    HashMap<String, ArrayList<Integer>> rssiTrends = new HashMap<String, ArrayList<Integer>>();     //PARTY RESET
//    //HashMap<String, Integer> mPartyMapSignalAvgs = new HashMap<String, Integer>();
//    private boolean contScan = false;                                                               //PARTY RESET
//
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_ble);
//
//        deviceList = new ArrayList<>();
//        partyList = new ArrayList<>();
//
//        mLabel = (TextView) findViewById(R.id.label);
//        mDiscoverButton = (Button) findViewById(R.id.discover_btn);
//        mStartPartyButton = (Button) findViewById(R.id.start_party_btn);
//        mCancelButton = (Button) findViewById(R.id.cancel_btn);
//        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
//        mProgressBar.setIndeterminate(true);    //infinite spinning wheel
//        mProgressBar.setVisibility(View.INVISIBLE);
//
//        mRecyclerView = (RecyclerView) findViewById(R.id.rssi_recycler_view);
//        // use this setting to improve performance if you know that changes
//        // in content do not change the layout size of the RecyclerView
//        mRecyclerView.setHasFixedSize(true);
//        mLayoutManager = new LinearLayoutManager(this);
//        mRecyclerView.setLayoutManager(mLayoutManager);
//        mAdapter = new RSSIListAdapter(this, deviceList);
//        mRecyclerView.setAdapter(mAdapter);
//
//        mContext = getApplicationContext();
//
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (mBluetoothAdapter == null) {
//            // Device does not support Bluetooth
//        }
//
//        // Turning on Bluetooth if it wasn't on
//        if (!mBluetoothAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        }
//
//        // Making the device discoverable
//        Intent discoverableIntent =
//                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//        startActivityForResult(discoverableIntent, 1);
//
//
//        canStartDiscovering = (ContextCompat.checkSelfPermission(thisActivity,
//                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
//
//
//        // Starting discovery with button press
//        mDiscoverButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (canStartDiscovering)
//                    startDiscovering();
//                else {
//                    getLocationPermission(thisActivity, REQUEST_COARSE_LOCATION);
//                }
//                canStartDiscovering = (ContextCompat.checkSelfPermission(thisActivity,
//                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
//            }
//        });
//
//        // Cancel party with button press
//        mCancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                    unregisterReceiver(mReceiver);
//                    deviceList.clear();
//                    mAdapter.notifyDataSetChanged();
//                    resetParty();
//
//            }
//        });
//
//
//        // Start party with button press
//        mStartPartyButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (canStartDiscovering) {
//                    if (selectedParty) {
//                        resetParty();
//                        Toast.makeText(mContext, "Stopping party", Toast.LENGTH_SHORT).show();
//                        mStartPartyButton.setText("Start Party");
//                    }else{
//                        startParty();
//                        Toast.makeText(mContext, "Starting party", Toast.LENGTH_SHORT).show();
//                        mStartPartyButton.setText("Stop Party");
//                        handler.postDelayed(runnable, 1000);
//                    }
//                }
//                else {
//                    getLocationPermission(thisActivity, REQUEST_COARSE_LOCATION);
//                }
//            }
//        });
//    }
//
//    private void resetParty() {
//        selectedParty = false;
//        mProgressBar.setVisibility(View.INVISIBLE);
//        deviceList.clear();
//        partyList.clear();
//        rssiTrends.clear();
//        setCardList(deviceList);
//        mLabel.setText("Device List: ");
//        turnOffContinuousScan();
//    }
//
//    private void startParty() {
//        selectedParty = true;
//        Toast.makeText(mContext, "Scanning for party signals", Toast.LENGTH_SHORT).show();
//        if (selectedParty) {
//            Log.d(TAG, "////////////Sampling RSSI");
//            HashMap<String, Integer> mPartyAvgSignals = sampleRSSI();
//            Log.d(TAG, "////////////Done sampling");
//            turnOnContinuousScan();
//            mProgressBar.setVisibility(View.VISIBLE);
//            partyList.clear();
//            partyList = mAdapter.getSelectedDevices();
//            setCardList(partyList);
//            scanForRSSI();
//
//            handler = new Handler();
//            runnable = new Runnable(){
//                public void run() {
//                    scanForRSSI();
//                    Log.d(TAG, "running scanForRSSI every 3 seconds");
//                }
//            };
//            //System.currentTimeMillis()+interval
//            handler.postAtTime(runnable, 1000);
//            handler.postDelayed(runnable, 1000);
//            //interval
//        }
//    }
//
//    private HashMap<String, Integer> sampleRSSI() {
//        for (int i = 0; i < 2; i++) {
//            scanForRSSI();
//        }
//        HashMap<String, Integer> rssi = new HashMap<String, Integer>();
//        for (String device: rssiTrends.keySet()) {
//            int avgSignal = 0;
//            for (int i = 0; i < 5; i++) {
//                avgSignal += rssiTrends.get(device).get(i);
//            }
//            avgSignal /= 5;
//            rssi.put(device, avgSignal);
//        }
//        return rssi;
//    }
//
//    private boolean turnOnContinuousScan() {
//        contScan = true;
//        return contScan;
//    }
//
//    private boolean turnOffContinuousScan() {
//        contScan = false;
//        return contScan;
//    }
//
//    private void scanForRSSI() {
//        // Starting scan for RSSI of devices in party
//        BluetoothDevice remoteDevice = null;
//        if (!mBluetoothAdapter.startDiscovery()) {
//            Toast.makeText(mContext, "ERROR: Can't scan for nearby devices. A requested permission may be disabled.", Toast.LENGTH_SHORT).show();
//            selectedParty = false;
//        }
//        else {
//            if (contScan) {
//                mAdapter.vibrate();
//            }
//
//            IntentFilter filter = new IntentFilter();
//            filter.addAction(BluetoothDevice.ACTION_FOUND);
//            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//            registerReceiver(mReceiver, filter);
//        }
//    }
//
//
//    public void startDiscovering() {
//        // Starting discovery
//        deviceList.clear();
//        mLabel.setText("Device List:");
//        setCardList(deviceList);
//        Toast.makeText(RSSIActivity.this, "Starting discovery for remote devices...", Toast.LENGTH_SHORT).show();
//        if (!mBluetoothAdapter.startDiscovery())
//            Toast.makeText(mContext, "Could not find any devices to connect to. A requested permission may be disabled.", Toast.LENGTH_SHORT).show();
//        else {
//            if (selectedParty) {
//                mLabel.setText("Your party:");
//            }
//            IntentFilter filter = new IntentFilter();
//            filter.addAction(BluetoothDevice.ACTION_FOUND);
//            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//            registerReceiver(mReceiver, filter);
//        }
//    }
//
//    // Create a BroadcastReceiver for ACTION_FOUND.
//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.d(RSSIActivity.class.getSimpleName(), "---- onReceive method started ----");
//            String action = intent.getAction();
//
//            // Just discovering: haven't selected party
//            if (!selectedParty) {
//                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
//                    //discovery starts, we can show progress dialog or perform other tasks
//                    Toast.makeText(mContext, "Discovery thread started... Scanning for devices", Toast.LENGTH_SHORT).show();
//                    Log.d(TAG, " ---- Reached ACTION_DISCOVERY_STARTED ----");
//                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                    //discovery finishes, dismiss progress dialog
//
//                    mAdapter.notifyDataSetChanged();
//
//                    Toast.makeText(mContext, "Done scanning", Toast.LENGTH_SHORT).show();
//                    Log.d(TAG, " ---- Reached ACTION_DISCOVERY_FINISHED ----");
//
//                    //mBluetoothAdapter.cancelDiscovery();
//                    //printDiscoveryResults();
//                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                    // Discovery has found a device. Get the BluetoothDevice
//                    // object and its info from the Intent.
//                    Log.d(TAG, " ---- Reached ACTION_FOUND ----");
//                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                    String deviceName = device.getName();
//                    String deviceAddress = device.getAddress();
//                    if (deviceAddress != null) {
//                        Toast.makeText(mContext, "Found device " + deviceName, Toast.LENGTH_SHORT).show();
//
//                        int rssi = (int) intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
//
//                        //TODO: Make this a checklist where you can select party members
//                        addDeviceToList(deviceList, deviceName, rssi, device.getAddress(), false);
//
//                        Toast.makeText(mContext, "There are " + deviceList.size() + " device(s) in party.", Toast.LENGTH_SHORT).show();
//                    }  else {
//                        Toast.makeText(mContext, "Found unknown device, ignoring...", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//
//            // You've selected party, scanning for RSSI values
//            else {
//                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
//                    //discovery starts, we can show progress dialog or perform other tasks
//                    Log.d(TAG, " ---- Reached ACTION_DISCOVERY_STARTED ----");
//                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                    //discovery finishes, dismiss progress dialog
//                    mAdapter.notifyDataSetChanged();
//                    // You've already determined which Bluetooth devices are in your party
//                    if (contScan) {
//                        scanForRSSI();  // Won't be called if selectedParty = false, so if you click cancel recursive loop will stop
//                    }
//                    // If not in continuous scan mode, the only reason to be called while selectedParty is on is to sample RSSI and find average
//
//                    //mBluetoothAdapter.cancelDiscovery();
//                    //printDiscoveryResults();
//                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                    // Discovery has found a device. Get the BluetoothDevice
//                    // object and its info from the Intent.
//                    Log.d(TAG, " ---- Reached ACTION_FOUND ----");
//                    BluetoothDevice newDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                    String deviceName = newDevice.getName();
//                    if (newDevice.getName() != null) {
//                        Toast.makeText(mContext, "Found device " + deviceName, Toast.LENGTH_SHORT).show();
//
//                        int rssi = (int) intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
//                        addDeviceToList(partyList, deviceName, rssi, newDevice.getAddress(), true);
//                        Toast.makeText(mContext, "There are " + partyList.size() + " device(s) in party.", Toast.LENGTH_SHORT).show();
//
//                        // If not in continuous scan mode, the only reason to be called while selectedParty is on is to sample RSSI and find average
//                        if (!contScan) {
//                            if (!rssiTrends.containsKey(deviceName)) {
//                                ArrayList<Integer> deviceSignalTrend = new ArrayList<Integer>();
//                                deviceSignalTrend.add(rssi);
//                                rssiTrends.put(deviceName, deviceSignalTrend);
//                            } else {    // The device is already there, just append the new RSSI to the deviceSignalTrend
//                                rssiTrends.get(deviceName).add(rssi);
//                            }
//
//                        }
//                    } else {
//                        Toast.makeText(mContext, "Found unknown device, ignoring...", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//
//
//        }
//    };
//
//
//
//
//
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case REQUEST_COARSE_LOCATION: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    // permission was granted, yay! Do the
//                    // location-related task you need to do.
//                    canStartDiscovering = true;
//
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//                return;
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request
//        }
//    }
//
//    private void setCardList(ArrayList<Device> list) {
//        mAdapter.setDeviceList(list, selectedParty);
//        mAdapter.notifyDataSetChanged();
//    }
//
//    private void addDeviceToList(ArrayList<Device> list, String name, int rssi, String address, boolean updateFlag) {
//        for (Device d : list) {
//            if(d.address.equals(address)) {
//                d.setRssiStrength(rssi);
//                mAdapter.notifyDataSetChanged();
//                return;
//            }
//        }
//        if(!updateFlag)
//            list.add(new Device(name, rssi, address));
//        mAdapter.notifyDataSetChanged();
//    }
//
//
//
//
//    /*  TODO: Implement onPause and onResume to turn off discovery/etc when app is paused
//    @Override
//    public void onPause() {
//        super.onPause();
//        locationManager.removeUpdates(locationListener);
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                Length, long, locationListener);
//    }
//    */
//
//
//}



import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class RSSIActivity extends FragmentActivity implements
        OnChartValueSelectedListener {

    private ArrayList<Integer> iterationsSinceConnect = null;

    private LineChart mChart;
    private static final int MY_PERMISSION_REQUEST_CONSTANT = 1;
    public static ArrayList<String> macs = new ArrayList<String>();
    private ArrayList<BluetoothLeService> LEs = null;
    private Thread t = null;


    private int[] mColors = new int[] {
            ColorTemplate.VORDIPLOM_COLORS[0],
            ColorTemplate.VORDIPLOM_COLORS[1],
            ColorTemplate.VORDIPLOM_COLORS[2]
    };

    public void do_scan(View v) {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void reconnect1(int i) {
        if(LEs == null)
            return;
        iterationsSinceConnect.set(i, 0);
        LEs.get(i).disconnect();
        LEs.get(i).close();
        LEs.get(i).initialize(getApplicationContext());
        LEs.get(i).connect(macs.get(i));
        LEs.get(i).last_rssi_success = 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void reconnect(View v) {
        if(LEs == null)
            return;
        for(int i = 0; i < LEs.size(); i++)
            reconnect1(i);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void refresh1(int i) {
        if(LEs == null)
            return;
        if(iterationsSinceConnect.get(i) > 5 && LEs.get(i).last_rssi_success == 0) {
            reconnect1(i);
            return;
        }

        addEntry(i, LEs.get(i).last_rssi);

        if(iterationsSinceConnect.get(i) >= 3) {
            LEs.get(i).readRssi();
        }

        iterationsSinceConnect.set(i, iterationsSinceConnect.get(i) + 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void refresh(View v) {
        if(LEs == null)
            return;
        for(int i = 0; i < LEs.size(); i++)
            refresh1(i);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ble);

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CONSTANT);

        mChart = findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);
        mChart.getDescription().setEnabled(true);
        mChart.getDescription().setText("Signal level vs time");
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);
        mChart.setBackgroundColor(Color.LTGRAY);

        Legend l = mChart.getLegend();
        l.setForm(LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(20f);
        leftAxis.setAxisMinimum(-50f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        try {
            macs = (ArrayList<String>) getIntent().getSerializableExtra("macs");
        }
        catch(Exception e)
        {
            macs = null;
        }

        Log.d("STATE", "MAIN" + macs);
        if(macs != null) {
            int L = macs.size();
            iterationsSinceConnect = new ArrayList<Integer>();
            for(int i = 0; i < L; i++) iterationsSinceConnect.add(0);

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

            for (int z = 0; z < L; z++) {

                ArrayList<Entry> values = new ArrayList<Entry>();
                values.add(new Entry(0, (float) 0));

                LineDataSet d = new LineDataSet(values, macs.get(z));
                d.setLineWidth(2.5f);
                d.setCircleRadius(4f);

                int color = mColors[z % mColors.length];
                d.setColor(color);
                d.setCircleColor(color);
                dataSets.add(d);
            }

            LineData data = new LineData(dataSets);
            mChart.setData(data);

            LEs = new ArrayList<BluetoothLeService>();
            for (int i = 0; i < L; i++) {
                LEs.add(new BluetoothLeService());
                reconnect1(i);
            }

            spawnUpdateThread();
        }
    }

    public void stopUpdateThread(View v)
    {
        if(t != null)
            t.interrupt();
    }

    private void spawnUpdateThread() {
        if(LEs == null)
            return;
        t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);
                        runOnUiThread(new Runnable() {
                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                            @Override
                            public void run() {
                                refresh(null);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();
    }

    private void addEntry(int i, int value) {
        if(LEs == null)
            return;

        mChart.getData().addEntry(new Entry(mChart.getData().getDataSetByIndex(i).getEntryCount(), (float) value), i);
        mChart.getData().notifyDataChanged();
        mChart.notifyDataSetChanged();
        mChart.moveViewToX(mChart.getData().getDataSetByIndex(i).getEntryCount());
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CONSTANT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("STATE", "Permission OK");
                }
                return;
            }
        }
    }
}