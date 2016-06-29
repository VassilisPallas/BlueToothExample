package com.example.vassilis.myapplication;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vassilis on 6/15/16.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    List<Device> devices = new ArrayList<>();
    List<BluetoothDevice> bluetoothDevices = new ArrayList<>();

    @Override
    public DeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceAdapter.ViewHolder holder, int position) {
        holder.name.setText(devices.get(position).getName());
        holder.mac.setText(devices.get(position).getMac());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void clear() {
        bluetoothDevices.clear();
        devices.clear();
    }

    public Device getDeviceData(int pos){
        return devices.get(pos);
    }

    public BluetoothDevice getBluetoothDevice(int pos){
        return bluetoothDevices.get(pos);
    }

    public void addDevice(Device device, BluetoothDevice bluetoothDevice) {
        devices.add(device);
        bluetoothDevices.add(bluetoothDevice);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, mac;

        ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            mac = (TextView) itemView.findViewById(R.id.mac);
        }
    }
}
