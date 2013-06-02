package org.twuni.homestreamer.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothDeviceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive( Context context, Intent intent ) {
		if( BluetoothDevice.ACTION_FOUND.equals( intent.getAction() ) ) {
			BluetoothDevice device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );
			onDeviceReceived( device );
		}
	}

	public void onDeviceReceived( BluetoothDevice device ) {
		// By default, do nothing.
	}

}
