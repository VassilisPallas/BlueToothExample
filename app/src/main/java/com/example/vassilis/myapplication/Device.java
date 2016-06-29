package com.example.vassilis.myapplication;

/**
 * Created by vassilis on 6/15/16.
 */

public class Device {
    private String name;
    private String mac;

    public Device(String name, String mac) {
        this.name = name;
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
