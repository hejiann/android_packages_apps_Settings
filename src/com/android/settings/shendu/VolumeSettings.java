package com.android.settings.shendu;

import android.app.Fragment;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import com.android.settings.R;

public class VolumeSettings extends Fragment {
	
	private SeekBar RINGTONE_soundValue;
	private SeekBar ALARM_soundValue;
	private SeekBar NOTIFICATION_soundValue;
	private SeekBar Music_soundValue;
	private SeekBar VIOCE_CALL_soundValue;
	private SeekBar SystemRington_soundValue;
	private SeekBar Bluetooth_soundValue;
	private AudioManager mAudioManager;
	private LinearLayout localLinearLayout;

	private void initView(LinearLayout paramLinearLayout) {
		VIOCE_CALL_soundValue = (SeekBar) paramLinearLayout.findViewById(R.id.call_volume);
		VIOCE_CALL_soundValue.setMax(mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
		VIOCE_CALL_soundValue.setProgress(mAudioManager
				.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
		VIOCE_CALL_soundValue
				.setOnSeekBarChangeListener(new SeekBarChangeListener(
						AudioManager.STREAM_VOICE_CALL));

		RINGTONE_soundValue = (SeekBar) paramLinearLayout.findViewById(R.id.ringtone_volume);
		RINGTONE_soundValue.setMax(mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_RING));
		RINGTONE_soundValue.setProgress(mAudioManager
				.getStreamVolume(AudioManager.STREAM_RING));
		RINGTONE_soundValue
				.setOnSeekBarChangeListener(new SeekBarChangeListener(
						AudioManager.STREAM_RING));

		Music_soundValue = (SeekBar) paramLinearLayout.findViewById(R.id.media_volume);
		Music_soundValue.setMax(mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		Music_soundValue.setProgress(mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC));
		Music_soundValue.setOnSeekBarChangeListener(new SeekBarChangeListener(
				AudioManager.STREAM_MUSIC));

		NOTIFICATION_soundValue = (SeekBar) paramLinearLayout.findViewById(R.id.notification_volume);
		NOTIFICATION_soundValue.setMax(mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
		NOTIFICATION_soundValue.setProgress(mAudioManager
				.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
		NOTIFICATION_soundValue
				.setOnSeekBarChangeListener(new SeekBarChangeListener(
						AudioManager.STREAM_NOTIFICATION));

		ALARM_soundValue = (SeekBar) paramLinearLayout.findViewById(R.id.alarm_volume);
		ALARM_soundValue.setMax(mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_ALARM));
		ALARM_soundValue.setProgress(mAudioManager
				.getStreamVolume(AudioManager.STREAM_ALARM));
		ALARM_soundValue.setOnSeekBarChangeListener(new SeekBarChangeListener(
				AudioManager.STREAM_ALARM));

		SystemRington_soundValue = (SeekBar) paramLinearLayout.findViewById(R.id.system_volume);
		SystemRington_soundValue.setMax(mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
		SystemRington_soundValue.setProgress(mAudioManager
				.getStreamVolume(AudioManager.STREAM_SYSTEM));
		SystemRington_soundValue
				.setOnSeekBarChangeListener(new SeekBarChangeListener(
						AudioManager.STREAM_SYSTEM));

		Bluetooth_soundValue = (SeekBar) paramLinearLayout.findViewById(R.id.bluetooth_volume);
		Bluetooth_soundValue.setMax(mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_BLUETOOTH_SCO));
		Bluetooth_soundValue.setProgress(mAudioManager
				.getStreamVolume(AudioManager.STREAM_BLUETOOTH_SCO));
		Bluetooth_soundValue
				.setOnSeekBarChangeListener(new SeekBarChangeListener(
						AudioManager.STREAM_BLUETOOTH_SCO));
	}

	public View onCreateView(LayoutInflater paramLayoutInflater,
			ViewGroup paramViewGroup, Bundle paramBundle) {
		getActivity().getActionBar().setIcon(R.drawable.ic_settings_sound);
		this.mAudioManager = ((AudioManager) getActivity().getSystemService(
				"audio"));
		localLinearLayout = (LinearLayout) paramLayoutInflater
				.inflate(R.layout.volume_settings, paramViewGroup, false);
		initView(localLinearLayout);
		return localLinearLayout;
	}

	@Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setDisplayShowHomeEnabled(false);
        ((View)localLinearLayout.getParent()).setBackgroundResource(R.drawable.settings_background);
        getActivity().getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_bg));
    }

    private class SeekBarChangeListener implements
			SeekBar.OnSeekBarChangeListener {
		private int type;

		public SeekBarChangeListener(int type) {
			this.type = type;
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			mAudioManager.setStreamVolume(type, seekBar.getProgress(),
					AudioManager.FLAG_PLAY_SOUND);
		}
	}
}