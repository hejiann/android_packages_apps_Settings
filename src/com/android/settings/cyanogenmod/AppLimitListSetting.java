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

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.settings.R;

public class AppLimitListSetting extends Fragment {

	private PackageManager pm;
	private LayoutInflater mInflater;
	private List<PackageInfo> apkInfos;
	private List<ResolveInfo> resolveInfos;
	private List<DataItem> resultData = new ArrayList<DataItem>();;
	private final String APP_LIST = "com.android.settings";

	private ListView mLimitList;
	private LimitListAdapter mLimitListAdapter;

	class DataItem {
		String mAppName;
		Drawable mAppIcon;
		boolean isCheck;
		String mPkgName;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		final ActionBar bar = getActivity().getActionBar();
		bar.setIcon(R.drawable.ic_settings_security);
		pm = getActivity().getPackageManager();

		mInflater = inflater;
		RelativeLayout view = (RelativeLayout) inflater.inflate(
				R.layout.app_limit_listview, container, false);

		mLimitList = (ListView) view.findViewById(R.id.app_limit_list);
		
		mThread.start();

		return view;
	}
	
	private Thread mThread = new Thread(){
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			initView();
		}
	};

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		saveData();
	}
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			mLimitListAdapter = new LimitListAdapter();
			mLimitList.setAdapter(mLimitListAdapter);
		}
		
	}; 

	private void initView() {
		String limitList = Settings.System.getString(getActivity()
				.getApplicationContext().getContentResolver(),
				Settings.System.APP_LIMIT_LIST);
		Intent launcherIntent = new Intent(Intent.ACTION_MAIN, null);
		launcherIntent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> launcherResolveInfos = pm.queryIntentActivities(
				launcherIntent, 0);
		StringBuilder launcherPkg = new StringBuilder();
		for (ResolveInfo launcher : launcherResolveInfos) {
			ApplicationInfo launcherInfo = launcher.activityInfo.applicationInfo;
			launcherPkg.append(launcherInfo.packageName + "|");
		}
		String LAUNCHER_PKG = launcherPkg.toString();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveInfos = pm.queryIntentActivities(mainIntent, 0);
		for (ResolveInfo info : resolveInfos) {
			ApplicationInfo applicationInfo = info.activityInfo.applicationInfo;
			if (!APP_LIST.contains(applicationInfo.packageName)
					&& !LAUNCHER_PKG.contains(applicationInfo.packageName)) {
				DataItem item = new DataItem();
				item.mAppName = applicationInfo.loadLabel(pm).toString();
				item.mAppIcon = pm.getApplicationIcon(applicationInfo);
				item.mPkgName = applicationInfo.packageName;
					if (limitList.contains(applicationInfo.packageName)) {
						item.isCheck = true;
					} else {
						item.isCheck = false;
					}
					resultData.add(item);
			}
		}
		
		mHandler.sendEmptyMessage(0);
	}

	private void saveData() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < resultData.size(); i++) {
			DataItem item = resultData.get(i);
			if (item.isCheck) {
				sb.append(item.mPkgName + "|");
			}
		}
		Settings.System.putString(getActivity().getApplicationContext()
				.getContentResolver(), Settings.System.APP_LIMIT_LIST, sb.toString());
	}

	class LimitListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return resultData.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		private class ViewHolder {
			CheckBox toggle;
			ImageView appIcon;
			TextView appName;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final int pos = position;
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.app_limit_list_item,
						null);
				holder = new ViewHolder();
				// find view by id
				holder.toggle = (CheckBox) convertView
						.findViewById(R.id.toggle);
				holder.appIcon = (ImageView) convertView
						.findViewById(R.id.appicon);
				holder.appName = (TextView) convertView
						.findViewById(R.id.appname);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					DataItem item = resultData.get(pos);
					CheckBox checkbox = (CheckBox) v.findViewById(R.id.toggle);
					item.isCheck = !checkbox.isChecked();
					resultData.set(pos, item);
					mLimitListAdapter.notifyDataSetChanged();
				}
			});

			// get data from mRootData
			DataItem itemInfo = resultData.get(position);

			holder.toggle.setChecked(itemInfo.isCheck);
			
			holder.toggle.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					DataItem item = resultData.get(pos);
					CheckBox checkbox = (CheckBox) v;
					item.isCheck = checkbox.isChecked();
					resultData.set(pos, item);
					mLimitListAdapter.notifyDataSetChanged();
				}
			});

			// set appname
			holder.appName.setText(itemInfo.mAppName);
			// set appicon
			holder.appIcon.setImageDrawable(itemInfo.mAppIcon);

			return convertView;
		}
	}
}
