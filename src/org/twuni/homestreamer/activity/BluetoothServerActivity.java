package org.twuni.homestreamer.activity;

import java.io.IOException;

import org.twuni.homestreamer.io.BluetoothClientThread.OnConnectedListener;
import org.twuni.homestreamer.io.BluetoothUtils;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

public class BluetoothServerActivity extends Activity {

	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
		switch( requestCode ) {
			case 0xF0:
				switch( resultCode ) {
					case RESULT_OK:
						BluetoothUtils.makeDiscoverable( 30, this, 0x0F );
						return;
				}
			case 0x0F:
				switch( resultCode ) {
					case RESULT_OK:
						try {
							BluetoothUtils.listen( this, new OnConnectedListener() {

								@Override
								public void onConnected( BluetoothSocket socket ) {
									Log.d( "HomeStreamer", "Connected!" );
								}

								@Override
								public void onError( Throwable throwable ) {
									report( throwable );
									Log.e( "HomeStreamer", String.format( "[%s] %s", throwable.getClass().getSimpleName(), throwable.getMessage() ) );
								}

							} );
						} catch( IOException exception ) {
							report( exception );
						}
						return;
				}
		}
		super.onActivityResult( requestCode, resultCode, data );
	}

	@Override
	protected void onStart() {
		super.onStart();
		if( BluetoothUtils.isBluetoothEnabled() ) {
			BluetoothUtils.makeDiscoverable( 30, this, 0x0F );
		} else {
			BluetoothUtils.enable( this, 0xF0 );
		}
	}

}
