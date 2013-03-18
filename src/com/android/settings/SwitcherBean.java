
package com.android.settings;

import android.preference.PreferenceActivity;

import com.android.settings.airplane.AirplaneEnabler;
import com.android.settings.bluetooth.BluetoothEnabler;
import com.android.settings.profiles.ProfileEnabler;
import com.android.settings.wifi.WifiEnabler;

public class SwitcherBean {

    private WifiEnabler mWifiEnabler = null;
    private BluetoothEnabler mBluetoothEnabler = null;
    private DataEnabler mDataEnabler = null;
    private ProfileEnabler mProfileEnabler = null;
    private AirplaneEnabler mAirplaneEnabler = null;
    private WifiEnabler mWifiEnabler2 = null;
    private BluetoothEnabler mBluetoothEnabler2 = null;
    private DataEnabler mDataEnabler2 = null;
    private ProfileEnabler mProfileEnabler2 = null;
    private AirplaneEnabler mAirplaneEnabler2 = null;
    private UsedSettings usedSettings;
    private int isWifi = 0;
    private int isBluetooth = 0;
    private int bluetoothIndex = 0;
    private int isData = 0;
    private int isProfile = 0;
    private int isAirplane = 0;
    private static SwitcherBean mBean = null;

    private SwitcherBean() {
    }

    public static SwitcherBean getInstance() {
        if (mBean == null) {
            mBean = new SwitcherBean();
        }
        return mBean;
    }

    public WifiEnabler getmWifiEnabler() {
        return mWifiEnabler;
    }

    public void setmWifiEnabler(WifiEnabler mWifiEnabler) {
        this.mWifiEnabler = mWifiEnabler;
    }

    public void setmWifiEnabler2(WifiEnabler mWifiEnabler) {
        this.mWifiEnabler2 = mWifiEnabler;
    }

    public BluetoothEnabler getmBluetoothEnabler() {
        return mBluetoothEnabler;
    }

    public void setmBluetoothEnabler(BluetoothEnabler mBluetoothEnabler) {
        this.mBluetoothEnabler = mBluetoothEnabler;
    }

    public void setmBluetoothEnabler2(BluetoothEnabler mBluetoothEnabler) {
        this.mBluetoothEnabler2 = mBluetoothEnabler;
    }

    public int getIsWifi() {
        return isWifi;
    }

    public void setIsWifi(int isWifi) {
        this.isWifi = isWifi;
    }

    public int getIsBluetooth() {
        return isBluetooth;
    }

    public void setIsBluetooth(int isBluetooth) {
        this.isBluetooth = isBluetooth;
    }

    public int getBluetoothIndex() {
        return bluetoothIndex;
    }

    public void setBluetoothIndex(int bluetoothIndex) {
        this.bluetoothIndex = bluetoothIndex;
    }

    public DataEnabler getmDataEnabler() {
        return mDataEnabler;
    }

    public void setmDataEnabler(DataEnabler mDataEnabler) {
        this.mDataEnabler = mDataEnabler;
    }
    
    public void setmDataEnabler2(DataEnabler mDataEnabler) {
        this.mDataEnabler2 = mDataEnabler;
    }


    public int getIsData() {
        return isData;
    }

    public void setIsData(int isData) {
        this.isData = isData;
    }

    public ProfileEnabler getmProfileEnabler() {
        return mProfileEnabler;
    }

    public void setmProfileEnabler(ProfileEnabler mProfileEnabler) {
            this.mProfileEnabler = mProfileEnabler;
    }

    public void setmProfileEnabler2(ProfileEnabler mProfileEnabler) {
            this.mProfileEnabler2 = mProfileEnabler;
    }

    public int getIsProfile() {
        return isProfile;
    }

    public void setIsProfile(int isProfile) {
        this.isProfile = isProfile;
    }

    public AirplaneEnabler getmAirplaneEnabler() {
        return mAirplaneEnabler;
    }

    public void setmAirplaneEnabler(AirplaneEnabler mAirplaneEnabler) {
            this.mAirplaneEnabler = mAirplaneEnabler;
    }

    public void setmAirplaneEnabler2(AirplaneEnabler mAirplaneEnabler) {
            this.mAirplaneEnabler2 = mAirplaneEnabler;
    }

    public int getIsAirplane() {
        return isAirplane;
    }

    public void setIsAirplane(int isAirplane) {
        this.isAirplane = isAirplane;
    }

    public PreferenceActivity getUsedSettings() {
        return usedSettings;
    }

    public void setUsedSettings(UsedSettings usedSettings) {
        this.usedSettings = usedSettings;
    }

    public void resumeAll() {
        if (this.mAirplaneEnabler != null)
            this.mAirplaneEnabler.resume();
        if (this.mBluetoothEnabler != null)
            this.mBluetoothEnabler.resume();
        if (this.mDataEnabler != null)
            this.mDataEnabler.resume();
        if (this.mProfileEnabler != null)
            this.mProfileEnabler.resume();
        if (this.mWifiEnabler != null)
            this.mWifiEnabler.resume();
        if (this.mAirplaneEnabler2 != null)
            this.mAirplaneEnabler2.resume();
        if (this.mBluetoothEnabler2 != null)
            this.mBluetoothEnabler2.resume();
        if (this.mDataEnabler2 != null)
            this.mDataEnabler2.resume();
        if (this.mProfileEnabler2 != null)
            this.mProfileEnabler2.resume();
        if (this.mWifiEnabler2 != null)
            this.mWifiEnabler2.resume();
        if (this.usedSettings != null)
            this.usedSettings.onResume();
    }

    public void pauseAll() {
        if (this.mAirplaneEnabler != null)
            this.mAirplaneEnabler.pause();
        if (this.mBluetoothEnabler != null)
            this.mBluetoothEnabler.pause();
        if (this.mDataEnabler != null)
            this.mDataEnabler.pause();
        if (this.mProfileEnabler != null)
            this.mProfileEnabler.pause();
        if (this.mWifiEnabler != null)
            this.mWifiEnabler.pause();
        if (this.mAirplaneEnabler2 != null)
            this.mAirplaneEnabler2.pause();
        if (this.mBluetoothEnabler2 != null)
            this.mBluetoothEnabler2.pause();
        if (this.mDataEnabler2 != null)
            this.mDataEnabler2.pause();
        if (this.mProfileEnabler2 != null)
            this.mProfileEnabler2.pause();
        if (this.mWifiEnabler2 != null)
            this.mWifiEnabler2.pause();
    }

}
