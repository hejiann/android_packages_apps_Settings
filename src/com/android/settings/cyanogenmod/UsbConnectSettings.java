/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.android.settings.cyanogenmod;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.os.SystemProperties;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class UsbConnectSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
	
	private static final String USB_CONNECT_FUNC = "usb_connect_func";
	private static final String USB_IS_REMEMBER = "usb_is_remember";
	private static final String USB_SETTING = "usb_setting";
	private static final String STORAGE_SETTING = "storage_setting";
	
	
	private final String USB_DEFALUT_VALUE = "usb_default_value";
	private final String USB_ASK_USER = "usb_ask_user";

	private CheckBoxPreference mUsbFuncToggle;
	private ListPreference mUsbFuncList;
	private PreferenceScreen mStorageSetting;
	
	private final boolean mHasSwitchableStorage = SystemProperties.get("ro.vold.switchablepair","").isEmpty();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.usb_connect_settings);
        
        final ActionBar bar = getActivity().getActionBar();
        
        bar.setIcon(R.drawable.ic_settings_usbconnect);
        
        PreferenceScreen prefSet = getPreferenceScreen();
//        mUsbFuncList = (ListPreference) prefSet.findPreference(USB_CONNECT_FUNC);
//        mUsbFuncToggle = (CheckBoxPreference) prefSet.findPreference(USB_IS_REMEMBER);
        mStorageSetting = (PreferenceScreen) prefSet.findPreference(STORAGE_SETTING);
        
//        if(mHasSwitchableStorage){
//        	prefSet.removePreference(mStorageSetting);
//        }
        
        
//        mUsbFuncToggle.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
//                USB_ASK_USER, 1) == 1));
//        
//        int mUsbFuncListValue = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
//                USB_DEFALUT_VALUE, 2);
//        mUsbFuncList.setValue(String.valueOf(mUsbFuncListValue));
//        switch (mUsbFuncListValue) {
//			case 0:
//				mUsbFuncList.setSummary(R.string.usb_connect_usb);
//				break;
//			case 1:
//				mUsbFuncList.setSummary(R.string.usb_connect_share);
//				break;
//			case 2:
//				mUsbFuncList.setSummary(R.string.usb_connect_charging);
//				break;
//		}
//        mUsbFuncList.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
//    	if(preference == mUsbFuncList){
//    		int mUsbFuncListValue = Integer.valueOf((String) newValue);
//    		switch (mUsbFuncListValue) {
//				case 0:
//					mUsbFuncList.setSummary(R.string.usb_connect_usb);
//					break;
//				case 1:
//					mUsbFuncList.setSummary(R.string.usb_connect_share);
//					break;
//				case 2:
//					mUsbFuncList.setSummary(R.string.usb_connect_charging);
//					break;
//    		}
//    		Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
//                    USB_DEFALUT_VALUE, mUsbFuncListValue);
//    		return true;
//    	}
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
//    	if(preference == mUsbFuncToggle){
//    		boolean isChecked = mUsbFuncToggle.isChecked();
//    		Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
//                    USB_ASK_USER, isChecked ? 1 : 0);
//    	}
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}

