package com.android.settings.cyanogenmod;
/*
 * Copyright (C) 2011 The CyanogenMod Project
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



import com.android.settings.R;
import com.android.settings.cyanogenmod.PowerWidgetUtil;
import com.android.settings.cyanogenmod.PowerWidgetUtil.ButtonInfo;
import com.android.settings.cyanogenmod.TouchInterceptor;

import android.app.ListActivity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.TreeSet;

public class PowerWidgetOrderActivity extends ListActivity {
	private static final String TAG = "PowerWidgetOrderActivity";

	private ListView mButtonList;
	private ButtonAdapter mButtonAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.order_power_widget_buttons_activity);
		mButtonList = getListView();
		((TouchInterceptor) mButtonList).setDropListener(mDropListener);
		mButtonAdapter = new ButtonAdapter(this);
		setListAdapter(mButtonAdapter);
		mButtonList.setSelection(mButtonList.getCount() - 5);
	}

	@Override
	public void onResume() {
		super.onResume();
		// reload our buttons and invalidate the views for redraw
		mButtonAdapter.reloadButtons();
		mButtonList.invalidateViews();
	}

	private TouchInterceptor.DropListener mDropListener = new TouchInterceptor.DropListener() {
		
	    public void drop(int from, int to) {
			// get the current button list
			ArrayList<String> buttons = PowerWidgetUtil
					.getButtonListFromString(PowerWidgetUtil
							.getCurrentButtons(PowerWidgetOrderActivity.this));

			// move the button
			if (from < buttons.size()) {
				String button = buttons.remove(from);

				if (to <= buttons.size()) {
					buttons.add(to, button);

					// save our buttons
					Log.d("SFWww", "saveCurrentButtons = " + buttons);
					PowerWidgetUtil.saveCurrentButtons(
							PowerWidgetOrderActivity.this,
							PowerWidgetUtil.getButtonStringFromList(buttons));

					// tell our adapter/listview to reload
					mButtonAdapter.reloadButtons();
					mButtonList.invalidateViews();
				}
			}
		}
	};

	public class ButtonAdapter extends BaseAdapter {
		private Context mContext;
		private static final int TYPE_ITEM = 0;
		private static final int TYPE_SEPARATOR = 1;
		private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;
		private TreeSet mSeparatorsSet = new TreeSet();
		private Resources mSystemUIResources = null;
		private LayoutInflater mInflater;
		private ArrayList<PowerWidgetUtil.ButtonInfo> mButtons;

		public ButtonAdapter(Context c) {
			mContext = c;
			mInflater = LayoutInflater.from(mContext);

			PackageManager pm = mContext.getPackageManager();
			if (pm != null) {
				try {
					mSystemUIResources = pm
							.getResourcesForApplication("com.android.systemui");
				} catch (Exception e) {
					mSystemUIResources = null;
					Log.e(TAG, "Could not load SystemUI resources", e);
				}
			}

			reloadButtons();
		}
		
		public void reloadButtons() {
			ArrayList<String> buttons = PowerWidgetUtil
					.getButtonListFromString(PowerWidgetUtil
							.getCurrentButtons(mContext));
			mButtons = new ArrayList<PowerWidgetUtil.ButtonInfo>();
			mButtons.clear();
			mSeparatorsSet.clear();
			for(int i = 0; i<buttons.size();i++){
				if(PowerWidgetUtil.BUTTON_DRIVER.equals(buttons.get(i))){
					mSeparatorsSet.add(i);
				}
				mButtons.add(PowerWidgetUtil.BUTTONS.get(buttons.get(i)));
				
			}
/*			for (String button : buttons) {
				mButtons.add(PowerWidgetUtil.BUTTONS.get(button));
			}
			mSeparatorsSet.add(12);*/
		}

		public int getCount() {
			return mButtons.size();
		}

		public Object getItem(int position) {
			return mButtons.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getItemViewType(int position) {
			return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR
					: TYPE_ITEM;
		}

		@Override
		public int getViewTypeCount() {
			return TYPE_MAX_COUNT;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			int type = getItemViewType(position);

			PowerWidgetUtil.ButtonInfo button = mButtons.get(position);

			    convertView = null;
			    holder = null;
				holder = new ViewHolder();
				switch (type) {
				case TYPE_ITEM:
					convertView = mInflater.inflate(
							R.layout.order_power_widget_button_list_item, null);
					holder.textView = (TextView) convertView
							.findViewById(R.id.name);
					holder.icon = (ImageView) convertView
							.findViewById(R.id.icon);
					holder.dragger = (ImageView) convertView
							.findViewById(R.id.grabber);
					holder.type = TYPE_ITEM;
					break;
				case TYPE_SEPARATOR:
					convertView = mInflater.inflate(
							R.layout.order_power_widget_button_list_driver,
							null);
					holder.textView = (TextView) convertView
							.findViewById(R.id.driver);
					holder.type = TYPE_SEPARATOR;
					break;
				}
				convertView.setTag(holder);
		
			setViewStyle(type, holder, button);
			
			return convertView;
		}

		public void setViewStyle(int type, ViewHolder holder,
				PowerWidgetUtil.ButtonInfo button) {
			switch (type) {
			case TYPE_ITEM:
				holder.textView.setText(button.getTitleResId());
				holder.icon.setVisibility(View.GONE);
				// attempt to load the icon for this button
				if (mSystemUIResources != null) {
					int resId = mSystemUIResources.getIdentifier(
							button.getIcon(), null, null);
					if (resId > 0) {
						try {
							Drawable d = mSystemUIResources.getDrawable(resId);
							holder.icon.setVisibility(View.VISIBLE);
							holder.icon.setImageDrawable(d);
						} catch (Exception e) {
							Log.e(TAG, "Error retrieving icon drawable", e);
						}
					}
				}
				break;
			case TYPE_SEPARATOR:
				break;
			}
		}
	}

	public static class ViewHolder {
		public TextView textView;
		public ImageView icon;
		public ImageView dragger;
		public int type;
	}

}
