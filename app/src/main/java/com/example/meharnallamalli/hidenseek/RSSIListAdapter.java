package com.example.meharnallamalli.hidenseek;

import android.content.Context;
import android.graphics.Color;
import android.os.Vibrator;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vaibhav on 1/13/17.
 */

public class RSSIListAdapter extends RecyclerView.Adapter<RSSIListAdapter.DeviceViewHolder> {

    private static final String TAG = "String";
    private List<Device> deviceList;
    private Context mContext;
    private boolean inParty = false;

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView deviceName;
        TextView rssiStrength;
        TextView vibes;

        DeviceViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.card_view);
            deviceName = (TextView)itemView.findViewById(R.id.device_name);
            rssiStrength = (TextView)itemView.findViewById(R.id.rssi_strength);
            vibes = (TextView)itemView.findViewById(R.id.vibes);
        }
    }

    public RSSIListAdapter(Context c, List<Device> deviceList) {
        this.mContext = c;
        this.deviceList = deviceList;
    }

    public void setDeviceList(ArrayList<Device> l, boolean inParty) {

        deviceList = l;
        this.inParty = inParty;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rssi_card, parent, false);
        DeviceViewHolder pvh = new DeviceViewHolder(v);
        return pvh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final DeviceViewHolder holder, final int position) {
        final Device device = deviceList.get(position);

        holder.deviceName.setText(device.name);
        holder.rssiStrength.setText(String.format("%d", scaledRssi(device)));
        holder.vibes.setText(getPartySignals(device));

        if(!inParty) {
            holder.cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Device onClick at" + position);
                    if (!device.selected) {
                        holder.cv.setCardBackgroundColor(Color.GRAY);
                        device.select();
                    } else if (device.selected) {
                        //unchecked.
                        holder.cv.setCardBackgroundColor(Color.WHITE);
                        device.deselect();
                    }
                }
            });
        }else{
            holder.cv.setCardBackgroundColor(Color.WHITE);
            vibrate();
            setCardViewColor(holder, device);
        }
    }


    private int scaledRssi(Device device){
        int scaled =0;
        if(device.rssiStrength >= -30){
            scaled = 100;
        }else if(device.rssiStrength <= -100){
            scaled =0;
        }else {
            int span = -(-30+100);
            double scaledPercent = 100 - (-device.rssiStrength/span)*100;
            scaled = (int) scaledPercent;
            return scaled;
        }

        return scaled;
    }

    private void setCardViewColor(DeviceViewHolder holder, Device device) {
        if(inParty){
            if (device.rssiStrength >= -50) {
                holder.cv.setCardBackgroundColor(Color.GREEN);
            } else if (device.rssiStrength < -50 && device.rssiStrength >= -80) {
                holder.cv.setCardBackgroundColor(Color.YELLOW);
            } else if (device.rssiStrength < -80 && device.rssiStrength >= -90) {
                holder.cv.setCardBackgroundColor(Color.RED);
            } else if (device.rssiStrength < -90) {
                holder.cv.setCardBackgroundColor(Color.BLACK);
            }
        }
    }


    private String getPartySignals(Device device) {
        String signalStrength = "";
        if (device.rssiStrength >= -50) {
            signalStrength = "Strong vibes";
        } else if (device.rssiStrength < -50 && device.rssiStrength >= -80) {
            signalStrength = "Ok vibes";
        } else if (device.rssiStrength < -80 && device.rssiStrength >= -90) {
            signalStrength = "Low vibes";
        } else if (device.rssiStrength < -90) {
            signalStrength = "Lost signal";
        }
        return signalStrength;
    }



    public void vibrate() {
        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);

        if(deviceList.size() > 0) {
            //only vibrate for the closest device, so logic for getting closest device
            Device closestDevice = deviceList.get(0);
            for (Device device : deviceList) {
                if (device.rssiStrength > closestDevice.rssiStrength) {
                    closestDevice = device;
                }
            }

            if (closestDevice.rssiStrength >= -50) {
                // 0 : Start without a delay
                // 400 : Vibrate for 400 milliseconds
                // 200 : Pause for 200 milliseconds
                // 400 : Vibrate for 400 milliseconds
                long[] mVibratePattern = new long[]{0, 400, 200, 400};
            } else if (closestDevice.rssiStrength < -50 && closestDevice.rssiStrength >= -80){
                long[] mVibratePattern = new long[]{0, 400, 2000, 400};
                v.vibrate(mVibratePattern, -1);
            } else if (closestDevice.rssiStrength < -80 && closestDevice.rssiStrength >= -90) {
                long[] mVibratePattern = new long[]{0, 400, 4000, 400};
                v.vibrate(mVibratePattern, -1);
            } else if (closestDevice.rssiStrength < -90) {
                long[] mVibratePattern = new long[]{0, 400, 6000, 400};
                v.vibrate(mVibratePattern, -1);
            }
        }
    }

    public ArrayList<Device> getSelectedDevices() {
        ArrayList<Device> out = new ArrayList<>();
        for(Device d : deviceList) {
            if(d.isSelected())
                out.add(d);
        }
        return out;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return deviceList.size();
    }
}
