package org.twuni.homestreamer.bluetooth.listener;

import android.bluetooth.BluetoothSocket;

public interface OnConnectedListener {

	public void onConnected( BluetoothSocket socket );

	public void onError( Throwable throwable );

}