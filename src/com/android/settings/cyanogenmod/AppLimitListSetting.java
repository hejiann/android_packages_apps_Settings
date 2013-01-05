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

import java.util.List;

import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import android.view.LayoutInflater;
import android.os.SystemProperties;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class AppLimitListSetting extends SettingsPreferenceFragment {

    private PackageManager pm;
    private List<PackageInfo> apkInfos;
    private PreferenceScreen prefset;
    private List<ResolveInfo> resolveInfos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.security_settings);

        final ActionBar bar = getActivity().getActionBar();
        bar.setIcon(R.drawable.ic_settings_security);
        pm = getActivity().getPackageManager();
        prefset = getPreferenceScreen();
        intiView();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        saveData();
    }

    private void intiView() {
        if (prefset != null) {
            prefset.removeAll();
        }
        String limitList = Settings.System.getString(getActivity()
                .getApplicationContext().getContentResolver(),
                Settings.System.APP_LIMIT_LIST);
        if (limitList != null) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveInfos = pm.queryIntentActivities(mainIntent, 0);
            for (ResolveInfo info : resolveInfos) {
                ApplicationInfo applicationInfo = info.activityInfo.applicationInfo;
                CheckBoxPreference item = new CheckBoxPreference(getActivity());
                item.setTitle(applicationInfo.loadLabel(pm).toString());
                item.setIcon(pm.getApplicationIcon(applicationInfo));
                if (limitList.contains(applicationInfo.packageName)) {
                    item.setChecked(true);
                } else {
                    item.setChecked(false);
                }
                prefset.addPreference(item);
            }
        }
    }

    private void saveData() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < prefset.getPreferenceCount(); i++) {
            CheckBoxPreference item = (CheckBoxPreference) prefset.getPreference(i);
            boolean isChecked = item.isChecked();
            if (isChecked) {
                ApplicationInfo applicationInfo = resolveInfos.get(i).activityInfo.applicationInfo;
                sb.append(applicationInfo.packageName + "|");
            }
        }
        Settings.System.putString(getActivity().getApplicationContext()
                .getContentResolver(),
                Settings.System.APP_LIMIT_LIST, sb.toString());
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
