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

package com.android.settings.wifi;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkUtils;
import android.net.ProxyProperties;
import android.net.RouteInfo;
import android.security.Credentials;
import android.security.KeyStore;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.net.wifi.WifiConfiguration.IpAssignment;
import android.net.wifi.WifiConfiguration.ProxySettings;
import android.widget.TextView;
import android.widget.Toast;

import com.android.settings.ProxySelector;
import com.android.settings.R;

public class WifiConnectionView extends Activity implements
		View.OnClickListener, OnCheckedChangeListener, TextWatcher,
		OnItemSelectedListener {

	private AccessPoint mAccessPoint;

	private boolean mEdit;

	private WifiManager mWifiManager;
	private WifiManager.Channel mChannel;
	private WifiManager.ActionListener mConnectListener;
	private WifiManager.ActionListener mSaveListener;
	private WifiManager.ActionListener mForgetListener;
	
	/* These values from from "wifi_eap_method" resource array */
    public static final int WIFI_EAP_METHOD_PEAP = 0;
    public static final int WIFI_EAP_METHOD_TLS  = 1;
    public static final int WIFI_EAP_METHOD_TTLS = 2;
    public static final int WIFI_EAP_METHOD_PWD  = 3;

	private Scanner mScanner;
	
	private static final String PHASE2_PREFIX = "auth=";
	
	private int mKeyStoreNetworkId = INVALID_NETWORK_ID;
	
	private static final String KEYSTORE_SPACE = WifiConfiguration.KEYSTORE_URI;
	
	private Handler mTextViewChangedHandler;

	private final int MENU_ID_WIFI_CANCEL = 0;
	private final int MENU_ID_WIFI_FORGET = 1;
	private final int MENU_ID_WIFI_CONNECT = 2;

	private WifiConnectionInfo mWifiConnectionInfo;

	private TextView wifi_cancel, wifi_forget, wifi_connect;

	private LayoutInflater mInflater;

	private ViewGroup mView;

	private LinearLayout security_fields, wifi_advanced_toggle,
			wifi_advanced_fields, proxy_settings_fields, ip_fields;
	
	private LinearLayout wifi_forget_parent,wifi_connect_parent;

	private CheckBox show_password, wifi_advanced_togglebox;

	private TextView mPasswordView;

	private int mAccessPointSecurity;

	private boolean wifiIsEnabled = true;

	private Spinner mIpSettingsSpinner;
	private TextView mIpAddressView;
	private TextView mGatewayView;
	private TextView mNetworkPrefixLengthView;
	private TextView mDns1View;
	private TextView mDns2View;

	private Spinner mProxySettingsSpinner;
	private TextView mProxyHostView;
	private TextView mProxyPortView;
	private TextView mProxyExclusionListView;
	
	private IpAssignment mIpAssignment = IpAssignment.UNASSIGNED;
    private ProxySettings mProxySettings = ProxySettings.UNASSIGNED;
    private LinkProperties mLinkProperties = new LinkProperties();
    
    private Spinner mSecuritySpinner;
    private Spinner mEapMethodSpinner;
    private Spinner mEapCaCertSpinner;
    private Spinner mPhase2Spinner;
    private Spinner mEapUserCertSpinner;
    private TextView mEapIdentityView;
    private TextView mEapAnonymousView;

	/* This value comes from "wifi_ip_settings" resource array */
	private static final int DHCP = 0;
	private static final int STATIC_IP = 1;

	/* These values come from "wifi_proxy_settings" resource array */
	public static final int PROXY_NONE = 0;
	public static final int PROXY_STATIC = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		getActionBar().setIcon(R.drawable.ic_settings_wifi);

		mWifiConnectionInfo = WifiConnectionInfo.getInstance();
		

		setContentView(R.layout.wifi_connection);

		mTextViewChangedHandler = new Handler();
		
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mChannel = mWifiManager.initialize(this, getMainLooper(), null);

		mConnectListener = new WifiManager.ActionListener() {
			public void onSuccess() {
			}

			public void onFailure(int reason) {
			}
		};

		mSaveListener = new WifiManager.ActionListener() {
			public void onSuccess() {
			}

			public void onFailure(int reason) {
			}
		};

		mForgetListener = new WifiManager.ActionListener() {
			public void onSuccess() {
			}

			public void onFailure(int reason) {
			}
		};

		mScanner = new Scanner();

		mInflater = getLayoutInflater();

		mView = (ViewGroup) findViewById(R.id.wifi_info);

		mAccessPoint = mWifiConnectionInfo.getmAccessPoint();
		mEdit = mWifiConnectionInfo.ismEdit();

		mAccessPointSecurity = (mAccessPoint == null) ? AccessPoint.SECURITY_NONE :
			mAccessPoint.security;

		getView();
		initData();

	}

	private void getView() {
		wifi_cancel = (TextView) findViewById(R.id.wifi_cancel);
		wifi_cancel.setOnClickListener(this);
		wifi_forget = (TextView) findViewById(R.id.wifi_forget);
		wifi_forget.setOnClickListener(this);
		wifi_connect = (TextView) findViewById(R.id.wifi_connect);
		wifi_connect.setOnClickListener(this);
		security_fields = (LinearLayout) findViewById(R.id.security_fields);
		wifi_forget_parent = (LinearLayout) findViewById(R.id.wifi_forget_parent);
		wifi_connect_parent = (LinearLayout) findViewById(R.id.wifi_connect_parent);
		wifi_advanced_toggle = (LinearLayout) findViewById(R.id.wifi_advanced_toggle);
		wifi_advanced_fields = (LinearLayout) findViewById(R.id.wifi_advanced_fields);
	}

	private void initData() {
		getActionBar().setTitle(mAccessPoint.ssid);

		if (mAccessPoint.mScanResult == null) {
			// state
			DetailedState state = mAccessPoint.getState();

			if (state != null) {
				wifi_connect_parent.setVisibility(View.GONE);
			}

			if (state != null) {
				addRow(R.string.wifi_status, Summary.get(this, state));
			}
			
			// level
			int level = mAccessPoint.getLevel();
			if (level != -1) {
				String[] signal = getResources().getStringArray(
						R.array.wifi_signal);
				addRow(R.string.wifi_signal, signal[level]);
			}

			if (state != null) {
				WifiInfo info = mAccessPoint.getInfo();
				if (info != null && info.getLinkSpeed() != -1) {
					addRow(R.string.wifi_speed, info.getLinkSpeed()
							+ WifiInfo.LINK_SPEED_UNITS);
				}
			}

			addRow(R.string.wifi_security,
					mAccessPoint.getSecurityString(false));

			if (state != null) {
				if (mAccessPoint.networkId != INVALID_NETWORK_ID) {
					WifiConfiguration config = mAccessPoint.getConfig();
					// Display IP addresses
					for (InetAddress a : config.linkProperties.getAddresses()) {
						addRow(R.string.wifi_ip_address, a.getHostAddress());
					}
				}
			}
		} else {
			wifi_connect.setEnabled(false);
			wifi_forget_parent.setVisibility(View.GONE);

			mView.removeAllViews();

			mView.addView(security_fields);
			security_fields.setVisibility(View.VISIBLE);

			mPasswordView = (EditText) security_fields
					.findViewById(R.id.password);
			mPasswordView.addTextChangedListener(this);

			show_password = (CheckBox) findViewById(R.id.show_password);
			show_password.setOnCheckedChangeListener(this);

			mView.addView(wifi_advanced_toggle);
			wifi_advanced_toggle.setVisibility(View.VISIBLE);

			wifi_advanced_togglebox = (CheckBox) findViewById(R.id.wifi_advanced_togglebox);
			wifi_advanced_togglebox.setOnCheckedChangeListener(this);

			mView.addView(wifi_advanced_fields);
			proxy_settings_fields = (LinearLayout) wifi_advanced_fields
					.findViewById(R.id.proxy_settings_fields);
			proxy_settings_fields.setVisibility(View.VISIBLE);
			mProxySettingsSpinner = (Spinner) mView
					.findViewById(R.id.proxy_settings);
			mProxySettingsSpinner.setOnItemSelectedListener(this);

			ip_fields = (LinearLayout) wifi_advanced_fields
					.findViewById(R.id.ip_fields);
			ip_fields.setVisibility(View.VISIBLE);
			mIpSettingsSpinner = (Spinner) mView.findViewById(R.id.ip_settings);
			mIpSettingsSpinner.setOnItemSelectedListener(this);
		}

	}

	private void addRow(int name, String value) {
		View mRow = mInflater.inflate(R.layout.wifi_info_item, null);
		((TextView) mRow.findViewById(R.id.name)).setText(name);
		((TextView) mRow.findViewById(R.id.value)).setText(value);
		mView.addView(mRow);

	}

	private void showProxyFields() {
		WifiConfiguration config = null;

		mView.findViewById(R.id.proxy_settings_fields).setVisibility(
				View.VISIBLE);

		if (mAccessPoint != null
				&& mAccessPoint.networkId != INVALID_NETWORK_ID) {
			config = mAccessPoint.getConfig();
		}

		if (mProxySettingsSpinner.getSelectedItemPosition() == PROXY_STATIC) {
			mView.findViewById(R.id.proxy_warning_limited_support)
					.setVisibility(View.VISIBLE);
			mView.findViewById(R.id.proxy_fields).setVisibility(View.VISIBLE);
			if (mProxyHostView == null) {
				mProxyHostView = (TextView) mView
						.findViewById(R.id.proxy_hostname);
				mProxyHostView.addTextChangedListener(this);
				mProxyPortView = (TextView) mView.findViewById(R.id.proxy_port);
				mProxyPortView.addTextChangedListener(this);
				mProxyExclusionListView = (TextView) mView
						.findViewById(R.id.proxy_exclusionlist);
				mProxyExclusionListView.addTextChangedListener(this);
			}
			if (config != null) {
				ProxyProperties proxyProperties = config.linkProperties
						.getHttpProxy();
				if (proxyProperties != null) {
					mProxyHostView.setText(proxyProperties.getHost());
					mProxyPortView.setText(Integer.toString(proxyProperties
							.getPort()));
					mProxyExclusionListView.setText(proxyProperties
							.getExclusionList());
				}
			}
		} else {
			mView.findViewById(R.id.proxy_warning_limited_support)
					.setVisibility(View.GONE);
			mView.findViewById(R.id.proxy_fields).setVisibility(View.GONE);
		}
	}

	private void showIpConfigFields() {
		WifiConfiguration config = null;

		mView.findViewById(R.id.ip_fields).setVisibility(View.VISIBLE);

		if (mAccessPoint != null
				&& mAccessPoint.networkId != INVALID_NETWORK_ID) {
			config = mAccessPoint.getConfig();
		}

		if (mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) {
			mView.findViewById(R.id.staticip).setVisibility(View.VISIBLE);
			if (mIpAddressView == null) {
				mIpAddressView = (TextView) mView.findViewById(R.id.ipaddress);
				mIpAddressView.addTextChangedListener(this);
				mGatewayView = (TextView) mView.findViewById(R.id.gateway);
				mGatewayView.addTextChangedListener(this);
				mNetworkPrefixLengthView = (TextView) mView
						.findViewById(R.id.network_prefix_length);
				mNetworkPrefixLengthView.addTextChangedListener(this);
				mDns1View = (TextView) mView.findViewById(R.id.dns1);
				mDns1View.addTextChangedListener(this);
				mDns2View = (TextView) mView.findViewById(R.id.dns2);
				mDns2View.addTextChangedListener(this);
			}
			if (config != null) {
				LinkProperties linkProperties = config.linkProperties;
				Iterator<LinkAddress> iterator = linkProperties
						.getLinkAddresses().iterator();
				if (iterator.hasNext()) {
					LinkAddress linkAddress = iterator.next();
					mIpAddressView.setText(linkAddress.getAddress()
							.getHostAddress());
					mNetworkPrefixLengthView.setText(Integer
							.toString(linkAddress.getNetworkPrefixLength()));
				}

				for (RouteInfo route : linkProperties.getRoutes()) {
					if (route.isDefaultRoute()) {
						mGatewayView.setText(route.getGateway()
								.getHostAddress());
						break;
					}
				}

				Iterator<InetAddress> dnsIterator = linkProperties.getDnses()
						.iterator();
				if (dnsIterator.hasNext()) {
					mDns1View.setText(dnsIterator.next().getHostAddress());
				}
				if (dnsIterator.hasNext()) {
					mDns2View.setText(dnsIterator.next().getHostAddress());
				}
			}
		} else {
			mView.findViewById(R.id.staticip).setVisibility(View.GONE);
		}
	}
	
	private boolean requireKeyStore(WifiConfiguration config) {
		if (WifiConfigController.requireKeyStore(config)
				&& KeyStore.getInstance().state() != KeyStore.State.UNLOCKED) {
			mKeyStoreNetworkId = config.networkId;
			Credentials.getInstance().unlock(this);
			return true;
		}
		return false;
	}
	
	/* package */ WifiConfiguration getConfig() {
        if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID && !mEdit) {
            return null;
        }

        WifiConfiguration config = new WifiConfiguration();

        if (mAccessPoint.networkId == INVALID_NETWORK_ID) {
            config.SSID = AccessPoint.convertToQuotedString(
                    mAccessPoint.ssid);
        } else {
            config.networkId = mAccessPoint.networkId;
        }

        switch (mAccessPointSecurity) {
            case AccessPoint.SECURITY_NONE:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                break;

            case AccessPoint.SECURITY_WEP:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                if (mPasswordView.length() != 0) {
                    int length = mPasswordView.length();
                    String password = mPasswordView.getText().toString();
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58) &&
                            password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
                break;

            case AccessPoint.SECURITY_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                if (mPasswordView.length() != 0) {
                    String password = mPasswordView.getText().toString();
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                break;
            case AccessPoint.SECURITY_EAP:
                config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
                config.eap.setValue((String) mEapMethodSpinner.getSelectedItem());

                config.phase2.setValue((mPhase2Spinner.getSelectedItemPosition() == 0) ? "" :
                        PHASE2_PREFIX + mPhase2Spinner.getSelectedItem());
                config.ca_cert.setValue((mEapCaCertSpinner.getSelectedItemPosition() == 0) ? "" :
                        KEYSTORE_SPACE + Credentials.CA_CERTIFICATE +
                        (String) mEapCaCertSpinner.getSelectedItem());
                config.client_cert.setValue((mEapUserCertSpinner.getSelectedItemPosition() == 0) ?
                        "" : KEYSTORE_SPACE + Credentials.USER_CERTIFICATE +
                        (String) mEapUserCertSpinner.getSelectedItem());
                final boolean isEmptyKeyId = (mEapUserCertSpinner.getSelectedItemPosition() == 0);
                config.key_id.setValue(isEmptyKeyId ? "" : Credentials.USER_PRIVATE_KEY +
                        (String) mEapUserCertSpinner.getSelectedItem());
                config.engine.setValue(isEmptyKeyId ? WifiConfiguration.ENGINE_DISABLE :
                        WifiConfiguration.ENGINE_ENABLE);
                config.engine_id.setValue(isEmptyKeyId ? "" : WifiConfiguration.KEYSTORE_ENGINE_ID);
                config.identity.setValue((mEapIdentityView.length() == 0) ? "" :
                        mEapIdentityView.getText().toString());
                config.anonymous_identity.setValue((mEapAnonymousView.length() == 0) ? "" :
                        mEapAnonymousView.getText().toString());
                if (mPasswordView.length() != 0) {
                    config.password.setValue(mPasswordView.getText().toString());
                }
                break;

            default:
                    return null;
        }

        config.proxySettings = mProxySettings;
        config.ipAssignment = mIpAssignment;
        config.linkProperties = new LinkProperties(mLinkProperties);

        return config;
    }
	
	private void loadCertificates(Spinner spinner, String prefix) {
        final Context context = this;
        final String unspecified = context.getString(R.string.wifi_unspecified);

        String[] certs = KeyStore.getInstance().saw(prefix);
        if (certs == null || certs.length == 0) {
            certs = new String[] {unspecified};
        } else {
            final String[] array = new String[certs.length + 1];
            array[0] = unspecified;
            System.arraycopy(certs, 0, array, 1, certs.length);
            certs = array;
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_item, certs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
	
	private void setCertificate(Spinner spinner, String prefix, String cert) {
        if (cert != null && cert.startsWith(prefix)) {
            setSelection(spinner, cert.substring(prefix.length()));
        }
    }
	
	private void setSelection(Spinner spinner, String value) {
        if (value != null) {
            @SuppressWarnings("unchecked")
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
            for (int i = adapter.getCount() - 1; i >= 0; --i) {
                if (value.equals(adapter.getItem(i))) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }
	
	/* package */void submit() {

		final WifiConfiguration config = getConfig();

		if (config == null) {
			if (mAccessPoint != null
					&& !requireKeyStore(mAccessPoint.getConfig())
					&& mAccessPoint.networkId != INVALID_NETWORK_ID) {
				mWifiManager.connect(mChannel, mAccessPoint.networkId,
						mConnectListener);
			}
		} else if (config.networkId != INVALID_NETWORK_ID) {
			if (mAccessPoint != null) {
				mWifiManager.save(mChannel, config, mSaveListener);
			}
		} else {
			if (mEdit || requireKeyStore(config)) {
				mWifiManager.save(mChannel, config, mSaveListener);
			} else {
				mWifiManager.connect(mChannel, config, mConnectListener);
			}
		}

		if (mWifiManager.isWifiEnabled()) {
			mScanner.resume();
		}
	}

	/* package */void forget() {
		if (mAccessPoint.networkId == INVALID_NETWORK_ID) {
			// Should not happen, but a monkey seems to triger it
			return;
		}

		mWifiManager.forget(mChannel, mAccessPoint.networkId, mForgetListener);

		if (mWifiManager.isWifiEnabled()) {
			mScanner.resume();
		}
	}
	
	/**
	 * Shows the latest access points available with supplimental information
	 * like the strength of network and the security for it.
	 */

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int _id = v.getId();
		switch (_id) {
		case R.id.wifi_forget:
			forget();
			finish();
			break;
		case R.id.wifi_connect:
			submit();
			finish();
			break;
		case R.id.wifi_cancel:
			finish();
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		int check_id = buttonView.getId();
		switch (check_id) {
		case R.id.show_password:
			int pos = mPasswordView.getSelectionEnd();
			mPasswordView
					.setInputType(InputType.TYPE_CLASS_TEXT
							| (isChecked ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
									: InputType.TYPE_TEXT_VARIATION_PASSWORD));
			if (pos >= 0) {
				((EditText) mPasswordView).setSelection(pos);
			}
			break;
		case R.id.wifi_advanced_togglebox:
			if (isChecked) {
				wifi_advanced_fields.setVisibility(View.VISIBLE);
			} else {
				wifi_advanced_fields.setVisibility(View.GONE);
			}
			break;
		}
	}
	
	private void showSecurityFields() {
        if (mAccessPointSecurity == AccessPoint.SECURITY_NONE) {
            mView.findViewById(R.id.security_fields).setVisibility(View.GONE);
            return;
        }
        mView.findViewById(R.id.security_fields).setVisibility(View.VISIBLE);

        if (mPasswordView == null) {
            mPasswordView = (TextView) mView.findViewById(R.id.password);
            mPasswordView.addTextChangedListener(this);
            ((CheckBox) mView.findViewById(R.id.show_password)).setOnClickListener(this);

            if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
                mPasswordView.setHint(R.string.wifi_unchanged);
            }
        }

        if (mAccessPointSecurity != AccessPoint.SECURITY_EAP) {
            mView.findViewById(R.id.eap).setVisibility(View.GONE);
            return;
        }
        mView.findViewById(R.id.eap).setVisibility(View.VISIBLE);

        if (mEapMethodSpinner == null) {
            mEapMethodSpinner = (Spinner) mView.findViewById(R.id.method);
            mEapMethodSpinner.setOnItemSelectedListener(this);
            mPhase2Spinner = (Spinner) mView.findViewById(R.id.phase2);
            mEapCaCertSpinner = (Spinner) mView.findViewById(R.id.ca_cert);
            mEapUserCertSpinner = (Spinner) mView.findViewById(R.id.user_cert);
            mEapIdentityView = (TextView) mView.findViewById(R.id.identity);
            mEapAnonymousView = (TextView) mView.findViewById(R.id.anonymous);

            loadCertificates(mEapCaCertSpinner, Credentials.CA_CERTIFICATE);
            loadCertificates(mEapUserCertSpinner, Credentials.USER_PRIVATE_KEY);

            if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
                WifiConfiguration config = mAccessPoint.getConfig();
                setSelection(mEapMethodSpinner, config.eap.value());

                final String phase2Method = config.phase2.value();
                if (phase2Method != null && phase2Method.startsWith(PHASE2_PREFIX)) {
                    setSelection(mPhase2Spinner, phase2Method.substring(PHASE2_PREFIX.length()));
                } else {
                    setSelection(mPhase2Spinner, phase2Method);
                }

                setCertificate(mEapCaCertSpinner, KEYSTORE_SPACE + Credentials.CA_CERTIFICATE,
                        config.ca_cert.value());
                setCertificate(mEapUserCertSpinner, Credentials.USER_PRIVATE_KEY,
                        config.key_id.value());
                mEapIdentityView.setText(config.identity.value());
                mEapAnonymousView.setText(config.anonymous_identity.value());
            }
        }

        mView.findViewById(R.id.l_method).setVisibility(View.VISIBLE);
        mView.findViewById(R.id.l_identity).setVisibility(View.VISIBLE);

        if (mEapMethodSpinner.getSelectedItemPosition() == WIFI_EAP_METHOD_PWD){
            mView.findViewById(R.id.l_phase2).setVisibility(View.GONE);
            mView.findViewById(R.id.l_ca_cert).setVisibility(View.GONE);
            mView.findViewById(R.id.l_user_cert).setVisibility(View.GONE);
            mView.findViewById(R.id.l_anonymous).setVisibility(View.GONE);
        } else {
            mView.findViewById(R.id.l_phase2).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.l_ca_cert).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.l_user_cert).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.l_anonymous).setVisibility(View.VISIBLE);
        }
    }
	
	/* show submit button if password, ip and proxy settings are valid */
    void enableSubmitIfAppropriate() {
        TextView submit = wifi_connect;
        if (submit == null) return;

        boolean enabled = false;
        boolean passwordInvalid = false;

        if (mPasswordView != null &&
            ((mAccessPointSecurity == AccessPoint.SECURITY_WEP && mPasswordView.length() == 0) ||
            (mAccessPointSecurity == AccessPoint.SECURITY_PSK && mPasswordView.length() < 8))) {
            passwordInvalid = true;
        }

        if (((mAccessPoint == null || mAccessPoint.networkId == INVALID_NETWORK_ID) &&
            passwordInvalid)) {
            enabled = false;
        } else {
            if (ipAndProxyFieldsAreValid()) {
                enabled = true;
            } else {
                enabled = false;
            }
        }
        submit.setEnabled(enabled);
    }
    
    private int validateIpConfigFields(LinkProperties linkProperties) {
        if (mIpAddressView == null) return 0;

        String ipAddr = mIpAddressView.getText().toString();
        if (TextUtils.isEmpty(ipAddr)) return R.string.wifi_ip_settings_invalid_ip_address;

        InetAddress inetAddr = null;
        try {
            inetAddr = NetworkUtils.numericToInetAddress(ipAddr);
        } catch (IllegalArgumentException e) {
            return R.string.wifi_ip_settings_invalid_ip_address;
        }

        int networkPrefixLength = -1;
        try {
            networkPrefixLength = Integer.parseInt(mNetworkPrefixLengthView.getText().toString());
            if (networkPrefixLength < 0 || networkPrefixLength > 32) {
                return R.string.wifi_ip_settings_invalid_network_prefix_length;
            }
            linkProperties.addLinkAddress(new LinkAddress(inetAddr, networkPrefixLength));
        } catch (NumberFormatException e) {
            // Set the hint as default after user types in ip address
            mNetworkPrefixLengthView.setText(getResources().getString(
                    R.string.wifi_network_prefix_length_hint));
        }

        String gateway = mGatewayView.getText().toString();
        if (TextUtils.isEmpty(gateway)) {
            try {
                //Extract a default gateway from IP address
                InetAddress netPart = NetworkUtils.getNetworkPart(inetAddr, networkPrefixLength);
                byte[] addr = netPart.getAddress();
                addr[addr.length-1] = 1;
                mGatewayView.setText(InetAddress.getByAddress(addr).getHostAddress());
            } catch (RuntimeException ee) {
            } catch (java.net.UnknownHostException u) {
            }
        } else {
            InetAddress gatewayAddr = null;
            try {
                gatewayAddr = NetworkUtils.numericToInetAddress(gateway);
            } catch (IllegalArgumentException e) {
                return R.string.wifi_ip_settings_invalid_gateway;
            }
            linkProperties.addRoute(new RouteInfo(gatewayAddr));
        }

        String dns = mDns1View.getText().toString();
        InetAddress dnsAddr = null;

        if (TextUtils.isEmpty(dns)) {
            //If everything else is valid, provide hint as a default option
            mDns1View.setText(getResources().getString(R.string.wifi_dns1_hint));
        } else {
            try {
                dnsAddr = NetworkUtils.numericToInetAddress(dns);
            } catch (IllegalArgumentException e) {
                return R.string.wifi_ip_settings_invalid_dns;
            }
            linkProperties.addDns(dnsAddr);
        }

        if (mDns2View.length() > 0) {
            dns = mDns2View.getText().toString();
            try {
                dnsAddr = NetworkUtils.numericToInetAddress(dns);
            } catch (IllegalArgumentException e) {
                return R.string.wifi_ip_settings_invalid_dns;
            }
            linkProperties.addDns(dnsAddr);
        }
        return 0;
    }
    
    private boolean ipAndProxyFieldsAreValid() {
        mLinkProperties.clear();
        mIpAssignment = (mIpSettingsSpinner != null &&
                mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) ?
                IpAssignment.STATIC : IpAssignment.DHCP;

        if (mIpAssignment == IpAssignment.STATIC) {
            int result = validateIpConfigFields(mLinkProperties);
            if (result != 0) {
                return false;
            }
        }

        mProxySettings = (mProxySettingsSpinner != null &&
                mProxySettingsSpinner.getSelectedItemPosition() == PROXY_STATIC) ?
                ProxySettings.STATIC : ProxySettings.NONE;

        if (mProxySettings == ProxySettings.STATIC && mProxyHostView != null) {
            String host = mProxyHostView.getText().toString();
            String portStr = mProxyPortView.getText().toString();
            String exclusionList = mProxyExclusionListView.getText().toString();
            int port = 0;
            int result = 0;
            try {
                port = Integer.parseInt(portStr);
                result = ProxySelector.validate(host, portStr, exclusionList);
            } catch (NumberFormatException e) {
                result = R.string.proxy_error_invalid_port;
            }
            if (result == 0) {
                ProxyProperties proxyProperties= new ProxyProperties(host, port, exclusionList);
                mLinkProperties.setHttpProxy(proxyProperties);
            } else {
                return false;
            }
        }
        return true;
    }

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		mTextViewChangedHandler.post(new Runnable() {
			public void run() {
				enableSubmitIfAppropriate();
			}
		});
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long arg3) {
		// TODO Auto-generated method stub
		if (parent == mSecuritySpinner) {
            mAccessPointSecurity = position;
            showSecurityFields();
        } else if (parent == mEapMethodSpinner) {
            showSecurityFields();
        } else if (parent == mProxySettingsSpinner) {
			showProxyFields();
		} else {
			showIpConfigFields();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}
	

	// Combo scans can take 5-6s to complete - set to 10s.
	private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;

	private class Scanner extends Handler {
		private int mRetry = 0;

		void resume() {
			if (!hasMessages(0)) {
				sendEmptyMessage(0);
			}
		}

		void forceScan() {
			removeMessages(0);
			sendEmptyMessage(0);
		}

		void pause() {
			mRetry = 0;
			removeMessages(0);
		}

		@Override
		public void handleMessage(Message message) {
			if (mWifiManager.startScanActive()) {
				mRetry = 0;
			} else if (++mRetry >= 3) {
				mRetry = 0;
				return;
			}
			sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
		}
	}

}
