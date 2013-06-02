package org.twuni.bluebot.activity;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.twuni.bluebot.Action;
import org.twuni.bluebot.R;
import org.twuni.bluebot.bluetooth.BluetoothDevice;
import org.twuni.bluebot.bluetooth.BluetoothUtils;
import org.twuni.bluebot.bluetooth.listener.OnConnectedListener;
import org.twuni.bluebot.bluetooth.receiver.BluetoothDeviceReceiver;

import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

abstract class Activity extends android.app.Activity {

	protected static final int REQUEST_BLUETOOTH_ENABLED = 0xF0;
	protected static final int REQUEST_BLUETOOTH_DISCOVERABILITY = 0x0F;

	protected Thread serverThread;
	private BroadcastReceiver deviceReceiver;

	protected boolean listening;

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
				public void onConnected( final BluetoothSocket socket ) {

					serverThread = new Thread() {

						@Override
						public void run() {
							try {
								DataInputStream in = new DataInputStream( socket.getInputStream() );
								while( true ) {
									Action action = Action.valueOf( in.readUTF() );
									switch( action ) {
										case STREAM:
											int sampleRate = in.readInt();
											int channels = in.readInt();
											int encoding = in.readInt();
											int bufferSize = AudioTrack.getMinBufferSize( sampleRate, channels, encoding );
											AudioTrack out = new AudioTrack( AudioManager.STREAM_MUSIC, sampleRate, channels, encoding, bufferSize, AudioTrack.MODE_STREAM );
											byte [] buffer = new byte [bufferSize];
											out.play();
											listening = true;
											for( int size = in.read( buffer ); listening && size > 0; size = in.read( buffer, 0, size ) ) {
												out.write( buffer, 0, size );
											}
											out.stop();
											out.release();
											break;
										default:
											onActionReceived( action, in );
									}
								}
							} catch( IOException exception ) {
								report( exception );
							}
						}
					};

					serverThread.start();

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

	protected void onActionReceived( Action action, InputStream in ) {
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

	protected void onDeviceReceived( android.bluetooth.BluetoothDevice device ) {
		onDeviceReceived( new BluetoothDevice( device ) );
	}

	protected void onDeviceReceived( BluetoothDevice device ) {
		// By default, do nothing.
	}

	protected Activity getActivity() {
		return this;
	}

	@Override
	protected void onPause() {
		super.onPause();
		listening = false;
	}

	@Override
	protected void onStart() {

		super.onStart();

		deviceReceiver = new BluetoothDeviceReceiver() {

			@Override
			public void onDeviceReceived( android.bluetooth.BluetoothDevice device ) {
				getActivity().onDeviceReceived( device );
			}

		};

		registerReceiver( deviceReceiver, new IntentFilter( android.bluetooth.BluetoothDevice.ACTION_FOUND ) );

		if( BluetoothUtils.isBluetoothEnabled() ) {
			listenForBluetoothConnections();
			for( android.bluetooth.BluetoothDevice device : BluetoothUtils.getPairedDevices() ) {
				onDeviceReceived( device );
			}
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
