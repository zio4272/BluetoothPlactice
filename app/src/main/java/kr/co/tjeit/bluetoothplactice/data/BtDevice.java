package kr.co.tjeit.bluetoothplactice.data;

import java.io.Serializable;

/**
 * Created by the on 2017-09-13.
 */

public class BtDevice implements Serializable {

    private String deviceName; // 천고바의 아이퐁
    private String deviceAddress; // BSD123V-12424-2144

    public BtDevice() {
    }

    public BtDevice(String deviceName, String deviceAddress) {
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }
}
