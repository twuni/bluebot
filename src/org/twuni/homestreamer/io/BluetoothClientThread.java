package org.twuni.homestreamer.io;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BluetoothClientThread extends Thread {

	public static interface OnConnectedListener {

		public void onConnected( BluetoothSocket socket );

		public void onError( Throwable throwable );

	}

	private BluetoothSocket socket;
	private final OnConnectedListener onConnectedListener;

	public BluetoothClientThread( UUID serviceID, BluetoothDevice device, OnConnectedListener onConnectedListener ) throws IOException {
		if( serviceID == null || device == null || onConnectedListener == null ) {
			throw new IllegalArgumentException();
		}
		socket = device.createRfcommSocketToServiceRecord( serviceID );
		this.onConnectedListener = onConnectedListener;
	}

	@Override
	public void run() {
		BluetoothUtils.cancelDiscovery();
		try {
			socket.connect();
			onConnectedListener.onConnected( socket );
		} catch( IOException exception ) {
			IOUtils.close( socket );
			onConnectedListener.onError( exception );
		}
	}

}
