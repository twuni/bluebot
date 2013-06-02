package org.twuni.homestreamer.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.twuni.homestreamer.R;
import org.twuni.homestreamer.io.BluetoothClientThread.OnConnectedListener;
import org.twuni.homestreamer.io.BluetoothUtils;
import org.twuni.homestreamer.receiver.BluetoothDeviceReceiver;
import org.twuni.homestreamer.view.adapter.ListAdapter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class BluetoothClientActivity extends Activity {

	private BroadcastReceiver deviceReceiver;
	protected final List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
	protected ListAdapter<BluetoothDevice> adapter;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {

		super.onCreate( savedInstanceState );

		adapter = new ListAdapter<BluetoothDevice>( R.layout.device, devices );

		ListView view = new ListView( this );

		view.setAdapter( adapter );

		view.setOnItemClickListener( new OnItemClickListener() {

			@Override
			public void onItemClick( AdapterView<?> parentView, View view, int position, long itemID ) {

				BluetoothDevice device = devices.get( position );

				try {

					Log.d( "HomeStreamer", String.format( "Connecting to %s", device.getName() ) );
					BluetoothUtils.connect( view.getContext(), device, new OnConnectedListener() {

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
			}

		} );

		setContentView( view );

	}

	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
		switch( requestCode ) {
			case 0xF0:
				switch( resultCode ) {
					case RESULT_OK:
						BluetoothUtils.discover();
						return;
				}
				break;
		}
		super.onActivityResult( requestCode, resultCode, data );
	}

	@Override
	protected void onStart() {
		super.onStart();
		deviceReceiver = new BluetoothDeviceReceiver() {

			@Override
			public void onDeviceReceived( BluetoothDevice device ) {
				devices.add( device );
				adapter.notifyDataSetChanged();
			}

		};
		registerReceiver( deviceReceiver, new IntentFilter( BluetoothDevice.ACTION_FOUND ) );
		if( BluetoothUtils.isBluetoothEnabled() ) {
			BluetoothUtils.discover();
		} else {
			BluetoothUtils.enable( this, 0xF0 );
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		BluetoothUtils.cancelDiscovery();
		unregisterReceiver( deviceReceiver );
	}

}
