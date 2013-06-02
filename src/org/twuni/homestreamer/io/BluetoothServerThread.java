package org.twuni.homestreamer.io;

import java.io.IOException;
import java.util.UUID;

import org.twuni.homestreamer.io.BluetoothClientThread.OnConnectedListener;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

public class BluetoothServerThread extends Thread {

	private BluetoothServerSocket server;
	private final OnConnectedListener onConnectedListener;

	public BluetoothServerThread( String serviceName, UUID serviceID, OnConnectedListener onConnectedListener ) throws IOException {
		super( serviceName );
		if( serviceName == null || serviceID == null || onConnectedListener == null ) {
			throw new IllegalArgumentException();
		}
		this.onConnectedListener = onConnectedListener;
		BluetoothAdapter adapter = BluetoothUtils.getAdapter();
		server = adapter.listenUsingRfcommWithServiceRecord( serviceName, serviceID );
	}

	@Override
	public void run() {
		try {
			BluetoothSocket client = server.accept();
			onConnectedListener.onConnected( client );
		} catch( IOException exception ) {
			onConnectedListener.onError( exception );
		} finally {
			IOUtils.close( server );
		}
	}

}
