
package com.android.settings.shendu;

import android.os.IPowerManager;

import com.android.settings.R;

import android.app.Fragment;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.os.ServiceManager;

public class BrightnessSettings extends Fragment implements
        SeekBar.OnSeekBarChangeListener, CheckBox.OnCheckedChangeListener {

    private SeekBar mSeekBar;
    private CheckBox mCheckBox;

    private int mOldBrightness;
    private int mOldAutomatic;

    private boolean mAutomaticAvailable;
    private boolean mAutomaticMode;
    private LinearLayout rootView;

    private int mCurBrightness = -1;

    private boolean mRestoredOldState;

    // Backlight range is from 0 - 255. Need to make sure that user
    // doesn't set the backlight to 0 and get stuck
    private int mScreenBrightnessDim;
    private static final int MAXIMUM_BACKLIGHT = android.os.PowerManager.BRIGHTNESS_ON;

    private static final int SEEK_BAR_RANGE = 10000;

    private ContentObserver mBrightnessObserver = new ContentObserver(
            new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mCurBrightness = -1;
            onBrightnessChanged();
        }
    };

    private ContentObserver mBrightnessModeObserver = new ContentObserver(
            new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onBrightnessModeChanged();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        getActivity().getActionBar().setIcon(R.drawable.ic_settings_display);

        rootView = (LinearLayout) inflater.inflate(
                R.layout.brightness_settings, container, false);

        mScreenBrightnessDim = getActivity().getResources().getInteger(
                com.android.internal.R.integer.config_screenBrightnessDim);

        mAutomaticAvailable = getActivity()
                .getResources()
                .getBoolean(
                        com.android.internal.R.bool.config_automatic_brightness_available);
        initView(rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        getActivity().getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                true, mBrightnessObserver);

        getActivity()
                .getContentResolver()
                .registerContentObserver(
                        Settings.System
                                .getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
                        true, mBrightnessModeObserver);
        mRestoredOldState = false;
        ((View) rootView.getParent())
                .setBackgroundResource(R.drawable.settings_background);
        getActivity().getActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.action_bar_bg));
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        final ContentResolver resolver = getActivity().getContentResolver();

        setBrightness(mSeekBar.getProgress(), true);

        resolver.unregisterContentObserver(mBrightnessObserver);
        resolver.unregisterContentObserver(mBrightnessModeObserver);
    }

    private void initView(LinearLayout view) {
        mSeekBar = (SeekBar) view.findViewById(R.id.shendu_brightless);
        mSeekBar.setMax(SEEK_BAR_RANGE);
        mOldBrightness = getBrightness();
        mSeekBar.setProgress(mOldBrightness);

        mCheckBox = (CheckBox) view.findViewById(R.id.automatic_mode);
        if (mAutomaticAvailable) {
            mCheckBox.setOnCheckedChangeListener(this);
            mOldAutomatic = getBrightnessMode(0);
            mAutomaticMode = mOldAutomatic == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            mCheckBox.setChecked(mAutomaticMode);
            mSeekBar.setEnabled(!mAutomaticMode);
        } else {
            mSeekBar.setEnabled(true);
        }
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        // TODO Auto-generated method stub
        setBrightness(progress, false);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setMode(isChecked ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        mSeekBar.setProgress(getBrightness());
        mSeekBar.setEnabled(!mAutomaticMode);
        setBrightness(mSeekBar.getProgress(), false);
    }

    private int getBrightness() {
        int mode = getBrightnessMode(0);
        float brightness = 0;
        if (false && mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            brightness = Settings.System.getFloat(getActivity()
                    .getContentResolver(),
                    Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, 0);
            brightness = (brightness + 1) / 2;
        } else {
            if (mCurBrightness < 0) {
                brightness = Settings.System.getInt(getActivity()
                        .getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, 100);
            } else {
                brightness = mCurBrightness;
            }
            brightness = (brightness - mScreenBrightnessDim)
                    / (MAXIMUM_BACKLIGHT - mScreenBrightnessDim);
        }
        return (int) (brightness * SEEK_BAR_RANGE);
    }

    private int getBrightnessMode(int defaultValue) {
        int brightnessMode = defaultValue;
        try {
            brightnessMode = Settings.System.getInt(getActivity()
                    .getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (SettingNotFoundException snfe) {
        }
        return brightnessMode;
    }

    private void onBrightnessChanged() {
        mSeekBar.setProgress(getBrightness());
    }

    private void onBrightnessModeChanged() {
        boolean checked = getBrightnessMode(0) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        mCheckBox.setChecked(checked);
        mSeekBar.setProgress(getBrightness());
        mSeekBar.setEnabled(!checked);
    }

    private void restoreOldState() {
        if (mRestoredOldState)
            return;

        if (mAutomaticAvailable) {
            setMode(mOldAutomatic);
        }
        setBrightness(mOldBrightness, false);
        mRestoredOldState = true;
        mCurBrightness = -1;
    }

    private void setBrightness(int brightness, boolean write) {
        if (mAutomaticMode) {
            if (false) {
                float valf = (((float) brightness * 2) / SEEK_BAR_RANGE) - 1.0f;
                try {
                    IPowerManager power = IPowerManager.Stub.asInterface(
                            ServiceManager.getService("power"));
                    if (power != null) {
                        power.setAutoBrightnessAdjustment(valf);
                    }
                    if (write) {
                        final ContentResolver resolver = getActivity().getContentResolver();
                        Settings.System.putFloat(resolver,
                                Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, valf);
                    }
                } catch (RemoteException doe) {
                }
            }
        } else {
            int range = (MAXIMUM_BACKLIGHT - mScreenBrightnessDim);
            brightness = (brightness * range) / SEEK_BAR_RANGE + mScreenBrightnessDim;
            try {
                IPowerManager power = IPowerManager.Stub.asInterface(
                        ServiceManager.getService("power"));
                if (power != null) {
                    power.setBacklightBrightness(brightness);
                }
                if (write) {
                    mCurBrightness = -1;
                    final ContentResolver resolver = getActivity().getContentResolver();
                    Settings.System.putInt(resolver,
                            Settings.System.SCREEN_BRIGHTNESS, brightness);
                } else {
                    mCurBrightness = brightness;
                }
            } catch (RemoteException doe) {
            }
        }
    }

    private void setMode(int mode) {
        mAutomaticMode = mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
    }

}
