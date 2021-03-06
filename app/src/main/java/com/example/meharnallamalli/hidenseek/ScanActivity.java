package com.example.meharnallamalli.hidenseek;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import java.util.ArrayList;

public class ScanActivity extends Activity {

    BluetoothAdapter mBluetoothAdapter;
    private CustomAdapter adp;

    public void doTrack(View v) {
        ArrayList<String> macs = adp.getMacs();
        Intent intent = new Intent(this, RSSIActivity.class);
        intent.putExtra("macs", macs);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        adp = new CustomAdapter();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                adp.setContext(context);
                adp.addElement(device.getAddress(), device.getName());
                Log.i("BT", device.getName() + "\n" + device.getAddress());
                ListView list = (ListView) findViewById(R.id.list);
                list.setAdapter(adp);
            }
        }
    };
}
