/*
 * Copyright (C) 2012 CyanogenMod
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

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.IWindowManager;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class FontsSettings extends SettingsPreferenceFragment {
	private static final String TAG = "SystemSettings";

	private static final String SMALL_SIZE = "small_size";
	private static final String NORMAL_SIZE = "normal_size";
	private static final String LARGE_SIZE = "big_size";
	private static final String HUGE_SIZE = "huge_size";

	private CheckBoxPreference mSmallSize;
	private CheckBoxPreference mNormalSize;
	private CheckBoxPreference mLargeSize;
	private CheckBoxPreference mHugeSize;

	private final Configuration mCurConfig = new Configuration();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.fonts_settings);

		getActivity().getActionBar().setIcon(R.drawable.ic_settings_font);

		mSmallSize = (CheckBoxPreference) findPreference(SMALL_SIZE);
		mNormalSize = (CheckBoxPreference) findPreference(NORMAL_SIZE);
		mLargeSize = (CheckBoxPreference) findPreference(LARGE_SIZE);
		mHugeSize = (CheckBoxPreference) findPreference(HUGE_SIZE);

	}

	int floatToIndex(float val) {
		String[] indices = getResources().getStringArray(
				R.array.entryvalues_font_size);
		float lastVal = Float.parseFloat(indices[0]);
		for (int i = 1; i < indices.length; i++) {
			float thisVal = Float.parseFloat(indices[i]);
			if (val < (lastVal + (thisVal - lastVal) * .5f)) {
				return i - 1;
			}
			lastVal = thisVal;
		}
		return indices.length - 1;
	}

	public void readFontSizePreference() {
		try {
			mCurConfig.updateFrom(ActivityManagerNative.getDefault()
					.getConfiguration());
		} catch (RemoteException e) {
			Log.w(TAG, "Unable to retrieve font size");
		}

		// mark the appropriate item in the preferences list
		int index = floatToIndex(mCurConfig.fontScale);
		switch (index) {
		case 0:
			mSmallSize.setChecked(true);
			mNormalSize.setChecked(false);
			mLargeSize.setChecked(false);
			mHugeSize.setChecked(false);
			break;
		case 1:
			mSmallSize.setChecked(false);
			mNormalSize.setChecked(true);
			mLargeSize.setChecked(false);
			mHugeSize.setChecked(false);
			break;
		case 2:
			mSmallSize.setChecked(false);
			mNormalSize.setChecked(false);
			mLargeSize.setChecked(true);
			mHugeSize.setChecked(false);
			break;
		case 3:
			mSmallSize.setChecked(false);
			mNormalSize.setChecked(false);
			mLargeSize.setChecked(false);
			mHugeSize.setChecked(true);
			break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		updateState();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private void updateState() {
		readFontSizePreference();
	}

	public void writeFontSizePreference(String objValue) {
		try {
			mCurConfig.fontScale = Float.parseFloat(objValue.toString());
			ActivityManagerNative.getDefault().updatePersistentConfiguration(
					mCurConfig);
		} catch (RemoteException e) {
			Log.w(TAG, "Unable to save font size");
		}
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		String objValue = "";
		if (preference == mSmallSize) {
			objValue = "0.85";
			mSmallSize.setChecked(true);
			mNormalSize.setChecked(false);
			mLargeSize.setChecked(false);
			mHugeSize.setChecked(false);
		} else if (preference == mNormalSize) {
			objValue = "1.0";
			mSmallSize.setChecked(false);
			mNormalSize.setChecked(true);
			mLargeSize.setChecked(false);
			mHugeSize.setChecked(false);
		} else if (preference == mLargeSize) {
			objValue = "1.15";
			mSmallSize.setChecked(false);
			mNormalSize.setChecked(false);
			mLargeSize.setChecked(true);
			mHugeSize.setChecked(false);
		} else if (preference == mHugeSize) {
			objValue = "1.35";
			mSmallSize.setChecked(false);
			mNormalSize.setChecked(false);
			mLargeSize.setChecked(false);
			mHugeSize.setChecked(true);
		}
		writeFontSizePreference(objValue);

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

}
