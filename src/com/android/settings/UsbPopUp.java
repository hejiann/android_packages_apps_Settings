package com.android.settings;

import android.app.Activity;
import android.os.Bundle;

public class UsbPopUp extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new com.android.settings.deviceinfo.UsbPopUp()).commit();
	}


}
