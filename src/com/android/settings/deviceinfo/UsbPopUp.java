/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentQueryMap;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

/**
 * USB PopUp Preference
 */
public class UsbPopUp extends SettingsPreferenceFragment {

    private static final String TAG = "UsbPopUp";

    private static final String KEY_MTP = "usb_mtp";
    private static final String KEY_PTP = "usb_ptp";
    private static final String KEY_MASS_STORAGE = "usb_mass_storage";
    private static final String KEY_SWITCH_UMS = "switch_ums";

    private UsbManager mUsbManager;
    private StorageManager storageManager;
    private StorageVolume[] storageVolumes;
    private CheckBoxPreference mMtp;
    private CheckBoxPreference mPtp;
    private CheckBoxPreference mUms;
    private Preference mSwitch;

    private boolean mMounting;
    private String mCurrentState = Environment.MEDIA_REMOVED;

    // thread for working with the storage services, which can be slow
    private Handler mAsyncStorageHandler;
    
    //ui handler
    private Handler mUiHandler;

    private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context content, Intent intent) {
            boolean connected = intent.getExtras().getBoolean(UsbManager.USB_CONNECTED);
            if (!connected) {
                // It was disconnected from the plug, so finish
                finish();
            }
            updateToggles(mUsbManager.getDefaultFunction());
        }
    };

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.usb_popup);
        root = getPreferenceScreen();

        mMtp = (CheckBoxPreference) root.findPreference(KEY_MTP);
        mPtp = (CheckBoxPreference) root.findPreference(KEY_PTP);
        mUms = (CheckBoxPreference) root.findPreference(KEY_MASS_STORAGE);
        mSwitch = (Preference) root.findPreference(KEY_SWITCH_UMS);
        if (!storageVolumes[0].allowMassStorage()) {
            root.removePreference(mUms);
            root.removePreference(mSwitch);
        }

        return root;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        getActivity().getActionBar().setIcon(R.drawable.ic_settings_usbconnect);
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        storageVolumes = storageManager.getVolumeList();
        
        storageManager.registerListener(new onStorageStateChangedListener());
        
        HandlerThread thr = new HandlerThread("SystemUI UsbStorageActivity");
        thr.start();
        mAsyncStorageHandler = new Handler(thr.getLooper());
        
        mUiHandler = new Handler();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mStateReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Make sure we reload the preference hierarchy since some of these
        // settings
        // depend on others...
        createPreferenceHierarchy();

        // ACTION_USB_STATE is sticky so this will call updateToggles
        getActivity().registerReceiver(mStateReceiver,
                new IntentFilter(UsbManager.ACTION_USB_STATE));
        
        mSwitch.setEnabled(canShared(mCurrentState));
    }

    private void updateToggles(String function) {
        if (UsbManager.USB_FUNCTION_MTP.equals(function)) {
            mMtp.setChecked(true);
            mPtp.setChecked(false);
            mUms.setChecked(false);
            mSwitch.setEnabled(false);
        } else if (UsbManager.USB_FUNCTION_PTP.equals(function)) {
            mMtp.setChecked(false);
            mUms.setChecked(false);
            mPtp.setChecked(true);
            mSwitch.setEnabled(false);
        } else if (UsbManager.USB_FUNCTION_MASS_STORAGE.equals(function)) {
            mMtp.setChecked(false);
            mPtp.setChecked(false);
            mUms.setChecked(true);
            mSwitch.setEnabled(true);
        } else {
            mMtp.setChecked(false);
            mPtp.setChecked(false);
            mUms.setChecked(false);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        // Don't allow any changes to take effect as the USB host will be
        // disconnected, killing
        // the monkeys
        if (Utils.isMonkeyRunning()) {
            return true;
        }
        // temporary hack - using check boxes as radio buttons
        // don't allow unchecking them
        if (preference instanceof CheckBoxPreference) {
            CheckBoxPreference checkBox = (CheckBoxPreference) preference;
            if (!checkBox.isChecked()) {
                checkBox.setChecked(true);
                return true;
            }
        }
        if (preference == mMtp) {
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.USB_MASS_STORAGE_ENABLED,
                    0);
            mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MTP, true);
            updateToggles(UsbManager.USB_FUNCTION_MTP);
        } else if (preference == mPtp) {
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.USB_MASS_STORAGE_ENABLED,
                    0);
            mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_PTP, true);
            updateToggles(UsbManager.USB_FUNCTION_PTP);
        } else if (preference == mUms) {
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.USB_MASS_STORAGE_ENABLED,
                    1);
            mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MASS_STORAGE, true);
            updateToggles(UsbManager.USB_FUNCTION_MASS_STORAGE);
        } else if (preference == mSwitch) {
            mSwitch.setEnabled(false);

            mAsyncStorageHandler.post(new Runnable() {
                public void run() {
                    if (mMounting) {
                        storageManager.disableUsbMassStorage();
                        mUiHandler.postDelayed(new Runnable() {
                            public void run() {
                                if (mCurrentState.equals(Environment.MEDIA_SHARED)) {
                                    Toast.makeText(getActivity(),
                                            getActivity().getResources().getString(R.string.unshare_failed),
                                            Toast.LENGTH_SHORT).show();
                                    mSwitch.setEnabled(true);
                                }
                            }
                        }, 4000);
                    } else {
                        storageManager.enableUsbMassStorage();
                    }
                }
            });
        }
        return true;
    }
    
    private boolean canShared(String currentState) {
        if (currentState.equals(Environment.MEDIA_SHARED)) {
            /*
             * Storage is now shared.
             * Modify the mSwitch text for turn off storage.
             */
            mSwitch.setTitle(R.string.turn_off_ums);
            mMounting = true;
            return true;
        } else if (currentState.equals(Environment.MEDIA_MOUNTED)){
            /*
             * Storage is now mounted.
             * Modify the mSwitch text for turn on storage.
             */
            mSwitch.setTitle(R.string.turn_on_ums);
            mMounting = false;
            return true;
        } else {
            mSwitch.setTitle(R.string.turn_on_ums);
            mMounting = false;
            return false;
        }
    }

    private class onStorageStateChangedListener extends StorageEventListener {
        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            Log.i("zhao", "old = " + oldState + " new = " + newState);
            mCurrentState = newState;
            mSwitch.setEnabled(canShared(mCurrentState));
        }
    }
}
