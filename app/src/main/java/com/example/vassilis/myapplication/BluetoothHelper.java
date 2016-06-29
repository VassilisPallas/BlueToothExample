package com.example.vassilis.myapplication;


import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.nio.charset.StandardCharsets;

/**
 * Created by vassilis on 6/16/16.
 */

public class BluetoothHelper {

    private static ConnectedThread connectedThread;

    private static Handler handler = new Handler();

    public static void manageConnectedSocket(BluetoothSocket mmSocket) {
        connectedThread = new ConnectedThread(mmSocket);
        connectedThread.setHandler(handler);
        connectedThread.start();
    }

    public static void write(String s) {
        connectedThread.write(s.getBytes());
    }

    public static void read(final BluetoothCallback bluetoothCallback) {

        final String[] data = {"Hello World"};

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.obj != null) {
                    switch (msg.what) {
                        case 101:
                            byte[] buffer = (byte[]) msg.obj;
                            data[0] = new String(buffer, StandardCharsets.UTF_8);
                            connectedThread.cancel();
                            Log.i("data", data[0]);
                            bluetoothCallback.onMessageReceived(data[0]);
                            return true;
                        default:
                            handleMessage(msg);
                            return false;
                    }
                }
                return false;
            }
        });
    }
}
