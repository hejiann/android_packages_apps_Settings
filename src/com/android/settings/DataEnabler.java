/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.util.Log;

import com.android.internal.telephony.TelephonyIntents;
import com.android.settings.SwitcherBean;

public class DataEnabler {
    private final Context mContext;
    private Switch mSwitch;
    private ConnectivityManager mConnService;
    private static final String TAG_CONFIRM_DATA_DISABLE = "confirmDataDisable";
    
    private StateReceiver mStateReceiver;
    
    private class StateReceiver extends BroadcastReceiver {
        public Context context;
        
        public void register() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
            context.registerReceiver(this, filter);
        }

        public void unregister() {
            context.unregisterReceiver(this);
        }

        public StateReceiver(Context mContext) {
            context = mContext;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            mSwitch.setOnCheckedChangeListener(null);
            mSwitch.setChecked(mConnService.getMobileDataEnabled());
            mSwitch.setOnCheckedChangeListener(mDataEnabledListener);
        }
        
    };


    public DataEnabler(Context context, Switch switch_) {
        mContext = context;
        mSwitch = switch_;

        mStateReceiver = new StateReceiver(context);
        
        mConnService = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private OnCheckedChangeListener mDataEnabledListener = new OnCheckedChangeListener() {
        /** {@inheritDoc} */
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            mConnService.setMobileDataEnabled(isChecked);
        }
    };
    public void pause() {
        mSwitch.setOnCheckedChangeListener(null);
        mStateReceiver.unregister();
    }

    public void resume() {
        mSwitch.setChecked(mConnService.getMobileDataEnabled());
        mSwitch.setOnCheckedChangeListener(mDataEnabledListener);
        mStateReceiver.register();
    }

    public void setSwitch(Switch _switch) {
        mSwitch = _switch;
        mSwitch.setChecked(mConnService.getMobileDataEnabled());
        mSwitch.setOnCheckedChangeListener(mDataEnabledListener);
    }
}
