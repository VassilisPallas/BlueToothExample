package com.example.vassilis.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.bluetooth.BluetoothAdapter;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity implements RecyclerItemClickListener.OnItemClickListener, BluetoothCallback {

    TextView textview;
    Button btnSend, btnGet;
    EditText editText;
    BluetoothAdapter mBluetoothAdapter;
    private RecyclerView recyclerView;
    private DeviceAdapter adapter;

    AcceptThread acceptThread;
    ConnectThread connectThread;

    private BluetoothCallback bluetoothCallback = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new DeviceAdapter();

        recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), recyclerView, this));

        textview = (TextView) findViewById(R.id.sendedText);

        btnSend = (Button) findViewById(R.id.btn);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableBluetooth();
            }
        });

        btnGet = (Button) findViewById(R.id.btn2);
        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unregisterReceiver(mReceiver);
                if (mBluetoothAdapter.isDiscovering())
                    mBluetoothAdapter.cancelDiscovery();

                BluetoothHelper.read(bluetoothCallback);

                acceptThread = new AcceptThread(mBluetoothAdapter);
                acceptThread.start();
            }
        });

        editText = (EditText) findViewById(R.id.text);
    }

    private void enableBluetooth() {
        adapter.clear();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent intentOpenBluetoothSettings = new Intent();
                intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intentOpenBluetoothSettings);
            } else {
                findDevices();
            }
        }
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                adapter.addDevice(new Device(device.getName(), device.getAddress()), device);
                adapter.notifyDataSetChanged();

            }
        }
    };


    private void findDevices() {

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        e();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
    }

    private void e() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
            switch (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                case PackageManager.PERMISSION_DENIED:
                    ((TextView) new AlertDialog.Builder(this)
                            .setTitle("Runtime Permissions up ahead")
                            .setMessage(Html.fromHtml("<p>To find nearby bluetooth devices please click \"Allow\" on the runtime permissions popup.</p>" +
                                    "<p>For more info see <a href=\"http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id\">here</a>.</p>"))
                            .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                                1111);
                                        //re-start discovery
                                        mBluetoothAdapter.startDiscovery();
                                        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();

                                        for (BluetoothDevice device : bondedDevices) {
                                            adapter.addDevice(new Device(device.getName(), device.getAddress()), device);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            })
                            .show()
                            .findViewById(android.R.id.message))
                            .setMovementMethod(LinkMovementMethod.getInstance());       // Make the link clickable. Needs to be called after show(), in order to generate hyperlinks
                    break;
                case PackageManager.PERMISSION_GRANTED:
                    //re-start discovery
                    mBluetoothAdapter.startDiscovery();
                    Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();

                    for (BluetoothDevice device : bondedDevices) {
                        adapter.addDevice(new Device(device.getName(), device.getAddress()), device);
                        adapter.notifyDataSetChanged();
                    }
                    break;
            }
        } else {
            mBluetoothAdapter.startDiscovery();
            Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();

            for (BluetoothDevice device : bondedDevices) {
                adapter.addDevice(new Device(device.getName(), device.getAddress()), device);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        unregisterReceiver(mReceiver);
        mBluetoothAdapter.cancelDiscovery();
        connectThread = new ConnectThread(adapter.getBluetoothDevice(position));
        connectThread.setData(editText.getText().toString());
        connectThread.setBluetoothAdapter(mBluetoothAdapter);
        connectThread.start();
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }

    @Override
    public void onMessageReceived(String s) {
        textview.setText(s);

        if (acceptThread != null)
            acceptThread.cancel();
        if (connectThread != null)
            connectThread.cancel();
    }

    @Override
    public void onMessageSend() {
        Toast.makeText(getApplicationContext(), "Message send successfully!", Toast.LENGTH_LONG).show();

        if (acceptThread != null)
            acceptThread.cancel();
        if (connectThread != null)
            connectThread.cancel();
    }
}
