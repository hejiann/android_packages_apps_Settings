/*
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Display;
import android.view.Window;
import android.widget.Toast;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.notificationlight.ColorPickerView;

public class LockscreenInterface extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "LockscreenInterface";
    private static final int LOCKSCREEN_BACKGROUND = 1024;
    private static final int LOCKSCREEN_HEAD_SCULPTURE = 1023;
    public static final String KEY_WEATHER_PREF = "lockscreen_weather";
    public static final String KEY_CALENDAR_PREF = "lockscreen_calendar";
    public static final String KEY_BACKGROUND_PREF = "lockscreen_background";
    public static final String KEY_RING_HEAD_PREF = "lockscreen_head_sculpture";
    private static final String KEY_ALWAYS_BATTERY_PREF = "lockscreen_battery_status";
    private static final String KEY_CLOCK_ALIGN = "lockscreen_clock_align";
    private static final String KEY_CLOCK_TARGETS = "lockscreen_targets";
    

    private ListPreference mCustomBackground;
    private ListPreference mCustomRingHead;
//    private Preference mWeatherPref;
//    private Preference mCalendarPref;
//    private ListPreference mBatteryStatus;
//    private ListPreference mClockAlign;
    private Activity mActivity;
    ContentResolver mResolver;
    
    private File wallpaperImage;
    private File wallpaperTemporary;
    private File headSculptureImage;
    private File headSculptureTemporary;
    private boolean mIsScreenLarge;
    
    private float radius = 100;
    private Bitmap headSculpture=null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mResolver = mActivity.getContentResolver();
        
        mActivity.getActionBar().setIcon(R.drawable.ic_settings_lockscreen);

        addPreferencesFromResource(R.xml.lockscreen_interface_settings);
        //mWeatherPref = (Preference) findPreference(KEY_WEATHER_PREF);
       //mCalendarPref = (Preference) findPreference(KEY_CALENDAR_PREF);

        mCustomBackground = (ListPreference) findPreference(KEY_BACKGROUND_PREF);
        mCustomBackground.setOnPreferenceChangeListener(this);
        wallpaperImage = new File(mActivity.getFilesDir()+"/lockwallpaper");
        wallpaperTemporary = new File(mActivity.getCacheDir()+"/lockwallpaper.tmp");
        
        radius = this.getResources().getDimension(R.dimen.lockscreen_head_sculpture_radius);
        mCustomRingHead = (ListPreference) findPreference(KEY_RING_HEAD_PREF);
        mCustomRingHead.setOnPreferenceChangeListener(this);
        headSculptureImage = new File(mActivity.getFilesDir()+"/headSculpture");
        headSculptureTemporary = new File(mActivity.getCacheDir()+"/headSculpture.tmp");

        //mBatteryStatus = (ListPreference) findPreference(KEY_ALWAYS_BATTERY_PREF);
        //mBatteryStatus.setOnPreferenceChangeListener(this);

        //mClockAlign = (ListPreference) findPreference(KEY_CLOCK_ALIGN);
        //mClockAlign.setOnPreferenceChangeListener(this);

        mIsScreenLarge = Utils.isTablet(getActivity());

        updateCustomBackgroundSummary();
        updateCustomHeadSculptureSummary();
        
    }

    private void updateCustomBackgroundSummary() {
        int resId;
        String value = Settings.System.getString(getContentResolver(),
                Settings.System.LOCKSCREEN_BACKGROUND);
        if (value == null) {
            resId = R.string.lockscreen_background_default_wallpaper;
            mCustomBackground.setValueIndex(2);
        } else if (value.isEmpty()) {
            resId = R.string.lockscreen_background_custom_image;
            mCustomBackground.setValueIndex(1);
        } else {
            resId = R.string.lockscreen_background_color_fill;
            mCustomBackground.setValueIndex(0);
        }
        mCustomBackground.setSummary(getResources().getString(resId));
    }
    
    private void updateCustomHeadSculptureSummary() {
    	int resId ;
        String value = Settings.System.getString(getContentResolver(),
                Settings.System.LOCKSCREEN_HEADSCULPTURE);
        if (value == null|| value.isEmpty()) {
            resId = R.string.lockscreen_ring_head_default_wallpaper;
            mCustomRingHead.setValueIndex(0);
        } else {
        	resId = R.string.lockscreen_ring_head_custom_image;
            mCustomRingHead.setValueIndex(1);
        }
        mCustomRingHead.setSummary(getResources().getString(resId));
    }

    @Override
    public void onResume() {
        super.onResume();
       // updateState();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
/*
    private void updateState() {
        int resId;
        // Set the weather description text
        if (mWeatherPref != null) {
            boolean weatherEnabled = Settings.System.getInt(mResolver,
                    Settings.System.LOCKSCREEN_WEATHER, 0) == 1;
            if (weatherEnabled) {
                mWeatherPref.setSummary(R.string.lockscreen_weather_enabled);
            } else {
                mWeatherPref.setSummary(R.string.lockscreen_weather_summary);
            }
        }

        // Set the calendar description text
        if (mCalendarPref != null) {
            boolean weatherEnabled = Settings.System.getInt(mResolver,
                    Settings.System.LOCKSCREEN_CALENDAR, 0) == 1;
            if (weatherEnabled) {
                mCalendarPref.setSummary(R.string.lockscreen_calendar_enabled);
            } else {
                mCalendarPref.setSummary(R.string.lockscreen_calendar_summary);
            }
        }

        // Set the battery status description text
        if (mBatteryStatus != null) {
            boolean batteryStatusAlwaysOn = Settings.System.getInt(mResolver,
                    Settings.System.LOCKSCREEN_ALWAYS_SHOW_BATTERY, 0) == 1;
            if (batteryStatusAlwaysOn) {
                mBatteryStatus.setValueIndex(1);
            } else {
                mBatteryStatus.setValueIndex(0);
            }
            mBatteryStatus.setSummary(mBatteryStatus.getEntry());
        }

        // Set the clock align value
        if (mClockAlign != null) {
            int clockAlign = Settings.System.getInt(mResolver,
                    Settings.System.LOCKSCREEN_CLOCK_ALIGN, 2);
            mClockAlign.setValue(String.valueOf(clockAlign));
            mClockAlign.setSummary(mClockAlign.getEntries()[clockAlign]);
        }
    }
*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCKSCREEN_BACKGROUND) {
            if (resultCode == Activity.RESULT_OK) {
                if (wallpaperTemporary.exists()) {
                    wallpaperTemporary.renameTo(wallpaperImage);
                }
                wallpaperImage.setReadOnly();
                Toast.makeText(mActivity, getResources().getString(R.string.
                        lockscreen_background_result_successful), Toast.LENGTH_LONG).show();
                Settings.System.putString(getContentResolver(),
                        Settings.System.LOCKSCREEN_BACKGROUND,"");
                updateCustomBackgroundSummary();
            } else {
                if (wallpaperTemporary.exists()) {
                    wallpaperTemporary.delete();
                }
                Toast.makeText(mActivity, getResources().getString(R.string.
                        lockscreen_background_result_not_successful), Toast.LENGTH_LONG).show();
            }
        } else if(requestCode == LOCKSCREEN_HEAD_SCULPTURE){			// lockscreen head_sculpture
        	 if (resultCode == Activity.RESULT_OK) {
                 if (headSculptureTemporary.exists()) {
                	 headSculpture = BitmapFactory.decodeFile(headSculptureTemporary.getPath());
                	 //new Thread(headSculptureRunnable).start();
                	 headSculptureRunnable();
                 Toast.makeText(mActivity, getResources().getString(R.string.
                		 lockscreen_head_sculpture_result_successful), Toast.LENGTH_LONG).show();
                 Settings.System.putString(getContentResolver(),
                         Settings.System.LOCKSCREEN_HEADSCULPTURE,"head_sculpture");
                 }
                 updateCustomHeadSculptureSummary();
             } else {
                 if (headSculptureTemporary.exists()) {
                	 	headSculptureTemporary.delete();
                 }
                 Toast.makeText(mActivity, getResources().getString(R.string.
                		 lockscreen_head_sculpture_result_not_successful), Toast.LENGTH_LONG).show();
             };
        }
    }
	public void headSculptureRunnable(){
		if(headSculpture==null) {
			Log.e(TAG, "headSculpture ==null!!");
			return;
		}
		Log.d(TAG, "radius="+radius);
		headSculpture = toRoundBitmap(headSculpture,radius);
		FileOutputStream fos = null;
		try{
			if(headSculptureImage.exists())
				headSculptureImage.delete();
    		fos = new FileOutputStream(headSculptureImage);
    		headSculpture.compress(Bitmap.CompressFormat.PNG, 100, fos);
    		fos.close();
		} catch(FileNotFoundException e){
			e.printStackTrace();
		} catch(IOException e1){
			e1.printStackTrace();
		}finally{
    		headSculptureImage.setReadOnly();
    		if (headSculptureTemporary.exists()) 
    			headSculptureTemporary.deleteOnExit();
		}
	}

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mCustomBackground) {
            int indexOf = mCustomBackground.findIndexOfValue(objValue.toString());
            switch (indexOf) {
            //Displays color dialog when user has chosen color fill
            case 0:
                final ColorPickerView colorView = new ColorPickerView(mActivity);
                int currentColor = Settings.System.getInt(getContentResolver(),
                        Settings.System.LOCKSCREEN_BACKGROUND, -1);
                if (currentColor != -1) {
                    colorView.setColor(currentColor);
                }
                colorView.setAlphaSliderVisible(true);
                new AlertDialog.Builder(mActivity)
                .setTitle(R.string.lockscreen_custom_background_dialog_title)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_BACKGROUND, colorView.getColor());
                        updateCustomBackgroundSummary();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setView(colorView).show();
                return false;
            //Launches intent for user to select an image/crop it to set as background
            case 1:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                intent.setType("image/*");
                intent.putExtra("crop", "true");
                intent.putExtra("scale", true);
                intent.putExtra("scaleUpIfNeeded", false);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
                Display display = mActivity.getWindowManager().getDefaultDisplay();
                int width = display.getWidth();
                int height = display.getHeight();
                Rect rect = new Rect();
                Window window = mActivity.getWindow();
                window.getDecorView().getWindowVisibleDisplayFrame(rect);
                int statusBarHeight = rect.top;
                int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
                int titleBarHeight = contentViewTop - statusBarHeight;
                // Lock screen for tablets visible section are different in landscape/portrait,
                // image need to be cropped correctly, like wallpaper setup for scrolling in background in home screen
                // other wise it does not scale correctly
                if (mIsScreenLarge) {
                    width = mActivity.getWallpaperDesiredMinimumWidth();
                    height = mActivity.getWallpaperDesiredMinimumHeight();
                    float spotlightX = (float) display.getWidth() / width;
                    float spotlightY = (float) display.getHeight() / height;
                    intent.putExtra("aspectX", width);
                    intent.putExtra("aspectY", height);
                    intent.putExtra("outputX", width);
                    intent.putExtra("outputY", height);
                    intent.putExtra("spotlightX", spotlightX);
                    intent.putExtra("spotlightY", spotlightY);

                } else {
                    boolean isPortrait = getResources().getConfiguration().orientation ==
                        Configuration.ORIENTATION_PORTRAIT;
                    intent.putExtra("aspectX", isPortrait ? width : height - titleBarHeight);
                    intent.putExtra("aspectY", isPortrait ? height - titleBarHeight : width);
                }
                try {
                    wallpaperTemporary.createNewFile();
                    wallpaperTemporary.setWritable(true, false);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(wallpaperTemporary));
                    intent.putExtra("return-data", false);
                    mActivity.startActivityFromFragment(this, intent, LOCKSCREEN_BACKGROUND);
                } catch (IOException e) {
                } catch (ActivityNotFoundException e) {
                }
                return false;
         	   
            //Sets background color to default
            case 2:
                Settings.System.putString(getContentResolver(),
                        Settings.System.LOCKSCREEN_BACKGROUND, null);
                updateCustomBackgroundSummary();
                break;
            }
            return true;
        } else if(preference == mCustomRingHead) {										// lockscreen default head sculpture
        	int indexOf = mCustomRingHead.findIndexOfValue(objValue.toString());
           switch (indexOf) {
           case 0:
        	   Settings.System.putString(getContentResolver(),
                       Settings.System.LOCKSCREEN_HEADSCULPTURE,null);
        	   updateCustomHeadSculptureSummary();
        	   break;
           case 1:
	           Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
	           intent.setType("image/*");
	           intent.putExtra("crop", "true");
	           intent.putExtra("scale", true);
	           intent.putExtra("scaleUpIfNeeded", true);
	           intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
	           int r = (int)(radius *2);
	           intent.putExtra("aspectX", r);
	           intent.putExtra("aspectY", r);
	           intent.putExtra("outputX", r);
	           intent.putExtra("outputY", r);
	           try {
	        	   	headSculptureTemporary.createNewFile();
	        	   	headSculptureTemporary.setWritable(true, false);
                 intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(headSculptureTemporary));
                 intent.putExtra("return-data", false);
                 mActivity.startActivityFromFragment(this, intent, LOCKSCREEN_HEAD_SCULPTURE);
               } catch (IOException e) {
               } catch (ActivityNotFoundException e) {
               }
            return false;
            }
       /* } else if (preference == mBatteryStatus) {
            int value = Integer.valueOf((String) objValue);
            int index = mBatteryStatus.findIndexOfValue((String) objValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_ALWAYS_SHOW_BATTERY, value);
            mBatteryStatus.setSummary(mBatteryStatus.getEntries()[index]);
            return true;
        } else if (preference == mClockAlign) {
            int value = Integer.valueOf((String) objValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_CLOCK_ALIGN, value);
            mClockAlign.setSummary(mClockAlign.getEntries()[value]);
            return true;*/
        }
        return false;
    }
    /**
     * 转换图片成圆形
     * @param bitmap 传入Bitmap对象
     * @return
     */
	static PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(Mode.SRC_IN);
	
    public static Bitmap toRoundBitmap(Bitmap bitmap,float radius) {
    		if(radius<=0) {
    			Log.e(TAG, "radius err:"+radius);
    			return null;
    		}
            int width = bitmap.getWidth();
				int height = bitmap.getHeight();
				float roundPx;
				float left,top,right,bottom,dst_left,dst_top,dst_right,dst_bottom;
				if (width <= height) {
					roundPx = width / 2;
					top = 0;
					bottom = width;
					left = 0;
					right = width;
					height = width;
					dst_left = 0;
					dst_top = 0;
					dst_right = width;
					dst_bottom = width;
				} else {
					roundPx = height / 2;
					float clip = (width - height) / 2;
					left = clip;
					right = width - clip;
					top = 0;
					bottom = height;
					width = height;
		    		dst_left = 0;
		    		dst_top = 0;
		    		dst_right = height;
		    		dst_bottom = height;
				}
				
				Bitmap output = Bitmap.createBitmap(width,height, Config.ARGB_8888);
		    	Canvas canvas = new Canvas(output);
				 final int color = 0xff424242;
				final Paint paint = new Paint();
				final Rect src = new Rect((int)left, (int)top, (int)right, (int)bottom);
				final Rect dst = new Rect((int)dst_left, (int)dst_top, (int)dst_right, (int)dst_bottom);
				final RectF rectF = new RectF(dst);
				paint.setAntiAlias(true);
				paint.setMaskFilter(new BlurMaskFilter(3, BlurMaskFilter.Blur.NORMAL));
				canvas.drawARGB(0, 0, 0, 0);
				paint.setColor(color);
				canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
				paint.setXfermode(porterDuffXfermode);
				paint.setAntiAlias(true);
				canvas.drawBitmap(bitmap, src, dst, paint);
				bitmap.recycle();
				/*
				float sxy = 2*radius/width;
				Matrix matrix = new Matrix();
				matrix.postScale(sxy, sxy);
				return Bitmap.createBitmap(output, 0, 0,output.getWidth(), output.getHeight(), matrix, true);
				*/
				int w_h = (int)(radius * 2);
				return Bitmap.createScaledBitmap(output,w_h,w_h,true);
    }
}
