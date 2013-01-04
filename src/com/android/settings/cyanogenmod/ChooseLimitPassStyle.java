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

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class ChooseLimitPassStyle extends SettingsPreferenceFragment {

	public static final int SET_PASSWORD = 1;
	public static final int SET_PARRTERN = 2;

	private Preference mPasswordStyle;
	private Preference mPartternStyle;

	private PreferenceScreen prefSet;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.security_settings);

		final ActionBar bar = getActivity().getActionBar();
		bar.setIcon(R.drawable.ic_settings_security);

		prefSet = getPreferenceScreen();

		mPasswordStyle = new Preference(getActivity());
		mPasswordStyle.setTitle(R.string.unlock_set_unlock_password_title);
		prefSet.addPreference(mPasswordStyle);
		mPartternStyle = new Preference(getActivity());
		mPartternStyle.setTitle(R.string.unlock_set_unlock_pattern_title);
		prefSet.addPreference(mPartternStyle);

	}

	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference == mPasswordStyle) {
			Intent passIntent = new Intent(getActivity(),
					AppLimitPassword.class);
			startActivityForResult(passIntent, SET_PASSWORD);
		} else if (preference == mPartternStyle) {
			Intent passIntent = new Intent(getActivity(),
					ChooseLimitPattern.class);
			startActivityForResult(passIntent, SET_PARRTERN);
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Settings.System.putInt(getActivity().getApplicationContext()
				.getContentResolver(), Settings.System.APP_LIMIT_ENABLE, 1);
		getActivity().finish();
	}
}
