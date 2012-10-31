package com.android.settings.wifi;

public class WifiConnectionInfo {
	
	private AccessPoint mAccessPoint;
	private boolean mEdit;
	
	private static WifiConnectionInfo mBean = null;
	
	private WifiConnectionInfo(){}

	public static WifiConnectionInfo getInstance(){
		if(mBean == null){
			mBean = new WifiConnectionInfo();
		}
		return 	mBean;		
	}

	public AccessPoint getmAccessPoint() {
		return mAccessPoint;
	}

	public void setmAccessPoint(AccessPoint mAccessPoint) {
		this.mAccessPoint = mAccessPoint;
	}

	public boolean ismEdit() {
		return mEdit;
	}

	public void setmEdit(boolean mEdit) {
		this.mEdit = mEdit;
	}
	
	
}
