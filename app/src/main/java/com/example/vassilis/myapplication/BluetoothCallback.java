package com.example.vassilis.myapplication;

/**
 * Created by vassilis on 6/16/16.
 */

public interface BluetoothCallback {
    void onMessageReceived(String s);

    void onMessageSend();
}
