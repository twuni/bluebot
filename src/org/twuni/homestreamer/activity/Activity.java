package org.twuni.homestreamer.activity;

import java.io.DataInputStream;
import java.io.IOException;

import org.twuni.homestreamer.io.BluetoothClientThread.OnConnectedListener;
import org.twuni.homestreamer.io.BluetoothUtils;
import org.twuni.homestreamer.model.BluetoothDevice;
import org.twuni.homestreamer.receiver.BluetoothDeviceReceiver;

import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

abstract class Activity extends android.app.Activity {

	protected static final int REQUEST_BLUETOOTH_ENABLED = 0xF0;
	protected static final int REQUEST_BLUETOOTH_DISCOVERABILITY = 0x0F;

	protected Thread serverThread;
	private BroadcastReceiver deviceReceiver;

	protected void listenForBluetoothConnections() {

		if( !BluetoothUtils.isBluetoothEnabled() ) {
			return;
		}

		try {

			serverThread = BluetoothUtils.listen( this, new OnConnectedListener() {

				@Override
				public void onConnected( BluetoothSocket socket ) {
					try {
						DataInputStream dis = new DataInputStream( socket.getInputStream() );
						while( true ) {
							String uri = dis.readUTF();
							Intent intent = new Intent( getActivity(), AudioPlayerActivity.class );
							intent.setData( Uri.parse( uri ) );
							startActivity( intent );
						}
					} catch( IOException exception ) {
						report( exception );
					}
				}

				@Override
				public void onError( Throwable throwable ) {
					report( throwable );
				}

			} );

		} catch( IOException exception ) {
			report( exception );
		}

	}

	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data ) {

		switch( requestCode ) {

			case REQUEST_BLUETOOTH_ENABLED:

				switch( resultCode ) {

					case RESULT_OK:

						BluetoothUtils.makeDiscoverable( 30, this, REQUEST_BLUETOOTH_DISCOVERABILITY );
						BluetoothUtils.discover();
						return;

					case RESULT_CANCELED:
						finish();
						return;

				}

			case REQUEST_BLUETOOTH_DISCOVERABILITY:

				switch( resultCode ) {

					case RESULT_OK:
						listenForBluetoothConnections();
						return;

					case RESULT_CANCELED:
						finish();
						return;

				}

		}

		super.onActivityResult( requestCode, resultCode, data );

	}

	protected void onDeviceReceived( BluetoothDevice device ) {
		// By default, do nothing.
	}

	protected Activity getActivity() {
		return this;
	}

	@Override
	protected void onStart() {

		super.onStart();

		deviceReceiver = new BluetoothDeviceReceiver() {

			@Override
			public void onDeviceReceived( android.bluetooth.BluetoothDevice device ) {
				getActivity().onDeviceReceived( new BluetoothDevice( device ) );
			}

		};

		registerReceiver( deviceReceiver, new IntentFilter( android.bluetooth.BluetoothDevice.ACTION_FOUND ) );

		if( BluetoothUtils.isBluetoothEnabled() ) {
			listenForBluetoothConnections();
			BluetoothUtils.makeDiscoverable( 30, this, REQUEST_BLUETOOTH_DISCOVERABILITY );
			BluetoothUtils.discover();
		} else {
			BluetoothUtils.enable( this, REQUEST_BLUETOOTH_ENABLED );
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		BluetoothUtils.cancelDiscovery();
		unregisterReceiver( deviceReceiver );
		if( serverThread != null ) {
			serverThread.interrupt();
		}
	}

	protected void report( final Throwable throwable ) {
		runOnUiThread( new Runnable() {

			@Override
			public void run() {
				Toast.makeText( getBaseContext(), String.format( "[%s] %s", throwable.getClass().getSimpleName(), throwable.getMessage() ), Toast.LENGTH_SHORT ).show();
			}

		} );
	}

}
