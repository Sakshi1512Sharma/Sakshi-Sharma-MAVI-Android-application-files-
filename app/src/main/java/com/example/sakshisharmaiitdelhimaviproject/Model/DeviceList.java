package com.example.sakshisharmaiitdelhimaviproject.Model;

public class DeviceList {
    String name;
    String deviceID;
    boolean paired;

    public DeviceList(String name, String deviceID, boolean paired) {
        this.name = name;
        this.deviceID = deviceID;
        this.paired = paired;
    }

   public String getName() {
        return name;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public boolean isPaired() {
        return paired;
    }

}
