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
import android.app.Activity;
import android.content.Intent;
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

public class AppLimitSettings extends SettingsPreferenceFragment {

	public static final int CONFIRM_PASSWORD = 2;
	public static final int CANCEL_PASSWORD = 3;
	public static final int CHANGE_PASSWORD = 4;

	private static final String APP_LIMIT_ENABLED = "app_limit_enabled";
	private static final String APP_LIMIT_LIST = "app_limit_list";
	private static final String APP_LIMIT_PASSWORD = "app_limit_password";

	private CheckBoxPreference mAppLimitEnabled;
	private PreferenceScreen mLimitList;
	private PreferenceScreen mSetPassword;

	private PreferenceScreen prefSet;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.app_limit_settings);

		final ActionBar bar = getActivity().getActionBar();
		bar.setIcon(R.drawable.ic_settings_security);

		prefSet = getPreferenceScreen();

		mAppLimitEnabled = (CheckBoxPreference) prefSet
				.findPreference(APP_LIMIT_ENABLED);

		mLimitList = (PreferenceScreen) prefSet.findPreference(APP_LIMIT_LIST);
		mSetPassword = (PreferenceScreen) prefSet
				.findPreference(APP_LIMIT_PASSWORD);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateStatus();
	}

	private void updateStatus() {
		boolean isAppEnabled = (Settings.System.getInt(getActivity()
				.getApplicationContext().getContentResolver(),
				Settings.System.APP_LIMIT_ENABLE, 0) == 1) ? true : false;
		mAppLimitEnabled.setChecked(isAppEnabled);
		mAppLimitEnabled
				.setSummary(isAppEnabled ? R.string.app_limit_enabled_on
						: R.string.app_limit_enabled_off);
		mLimitList.setEnabled(isAppEnabled);
		mSetPassword.setEnabled(isAppEnabled);
	}

	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		boolean value;
		if (preference == mAppLimitEnabled) {
			value = mAppLimitEnabled.isChecked();
			if (value) {
				String password = Settings.System.getString(getActivity()
						.getApplicationContext().getContentResolver(),
						Settings.System.APP_LIMIT_PASSWORD);
				if (password == null) {
					startFragment(this,
							ChooseLimitPassStyle.class.getCanonicalName(), -1,
							null);
				} else {
					mLimitList.setEnabled(true);
					mSetPassword.setEnabled(true);
					mAppLimitEnabled.setChecked(value);
					mAppLimitEnabled
							.setSummary(value ? R.string.app_limit_enabled_on
									: R.string.app_limit_enabled_off);
				}
				Settings.System.putInt(getActivity().getApplicationContext()
						.getContentResolver(),
						Settings.System.APP_LIMIT_ENABLE, 1);
			} else {
				boolean passwordSytle = (Settings.System.getInt(getActivity()
						.getApplicationContext().getContentResolver(),
						Settings.System.APP_LIMIT_PASSWORD_STYLE, 0) == 0) ? true
						: false; // 0 password; 1 images
				if (passwordSytle) {
					Intent confirmPassIntent = new Intent(getActivity(),
							ConfirmLimitPassword.class);
					startActivityForResult(confirmPassIntent, CANCEL_PASSWORD);
				} else {
					Intent confirmPassIntent = new Intent(getActivity(),
							ConfirmLimitPattern.class);
					startActivityForResult(confirmPassIntent, CANCEL_PASSWORD);
				}
			}
		} else if (preference == mSetPassword) {
			boolean passwordSytle = (Settings.System.getInt(getActivity()
					.getApplicationContext().getContentResolver(),
					Settings.System.APP_LIMIT_PASSWORD_STYLE, 0) == 0) ? true
					: false; // 0 password; 1 images
			if (passwordSytle) {
				Intent confirmPassIntent = new Intent(getActivity(),
						ConfirmLimitPassword.class);
				startActivityForResult(confirmPassIntent, CHANGE_PASSWORD);
			} else {
				Intent confirmPassIntent = new Intent(getActivity(),
						ConfirmLimitPattern.class);
				startActivityForResult(confirmPassIntent, CHANGE_PASSWORD);
			}
			return false;
		} else if (preference == mLimitList) {
			boolean passwordSytle = (Settings.System.getInt(getActivity()
					.getApplicationContext().getContentResolver(),
					Settings.System.APP_LIMIT_PASSWORD_STYLE, 0) == 0) ? true
					: false; // 0 password; 1 images
			if (passwordSytle) {
				Intent confirmPassIntent = new Intent(getActivity(),
						ConfirmLimitPassword.class);
				startActivityForResult(confirmPassIntent, CONFIRM_PASSWORD);
			} else {
				Intent confirmPassIntent = new Intent(getActivity(),
						ConfirmLimitPattern.class);
				startActivityForResult(confirmPassIntent, CONFIRM_PASSWORD);
			}

			return false;
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CONFIRM_PASSWORD && resultCode == Activity.RESULT_OK) {
			super.onPreferenceTreeClick(prefSet, mLimitList);
		} else if (requestCode == CANCEL_PASSWORD
				&& resultCode == Activity.RESULT_OK) {
			Settings.System.putInt(getActivity().getApplicationContext()
					.getContentResolver(), Settings.System.APP_LIMIT_ENABLE, 0);
		} else if (requestCode == CHANGE_PASSWORD
				&& resultCode == Activity.RESULT_OK) {
			super.onPreferenceTreeClick(prefSet, mSetPassword);
		}
	}

}
