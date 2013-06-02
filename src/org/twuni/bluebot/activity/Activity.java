package org.twuni.bluebot.activity;

import java.io.DataInputStream;
import java.io.IOException;

import org.twuni.bluebot.bluetooth.BluetoothDevice;
import org.twuni.bluebot.bluetooth.BluetoothUtils;
import org.twuni.bluebot.bluetooth.listener.OnConnectedListener;
import org.twuni.bluebot.bluetooth.receiver.BluetoothDeviceReceiver;
import org.twuni.bluebot.R;

import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.MenuItem;
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

		if( serverThread != null ) {
			if( serverThread.isAlive() ) {
				return;
			} else {
				serverThread = null;
			}
		}

		try {

			serverThread = BluetoothUtils.listen( this, new OnConnectedListener() {

				@Override
				public void onConnected( BluetoothSocket socket ) {
					try {
						DataInputStream dis = new DataInputStream( socket.getInputStream() );
						while( true ) {
							String next = dis.readUTF();
							onDataReceived( next );
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

	/**
	 * @param data
	 */
	protected void onDataReceived( String data ) {
		// By default, do nothing.
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		switch( item.getItemId() ) {
			case R.id.bluetooth_broadcast:
				BluetoothUtils.makeDiscoverable( 30, this, REQUEST_BLUETOOTH_DISCOVERABILITY );
				return true;
		}
		return super.onOptionsItemSelected( item );
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		getMenuInflater().inflate( R.menu.bluetooth, menu );
		return super.onCreateOptionsMenu( menu );
	}

	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data ) {

		switch( requestCode ) {

			case REQUEST_BLUETOOTH_ENABLED:

				switch( resultCode ) {

					case RESULT_OK:

						listenForBluetoothConnections();
						BluetoothUtils.startDiscovery();
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
			BluetoothUtils.startDiscovery();
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

	protected void report( String format, Object... args ) {
		final String message = String.format( format, args );
		runOnUiThread( new Runnable() {

			@Override
			public void run() {
				Toast.makeText( getBaseContext(), message, Toast.LENGTH_SHORT ).show();
			}

		} );
	}

	protected void report( final Throwable throwable ) {
		report( "[%s] %s", throwable.getClass().getSimpleName(), throwable.getMessage() );
	}

}
