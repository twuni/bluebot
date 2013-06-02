package org.twuni.homestreamer.activity;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.twuni.homestreamer.R;
import org.twuni.homestreamer.io.BluetoothClientThread.OnConnectedListener;
import org.twuni.homestreamer.io.BluetoothUtils;
import org.twuni.homestreamer.model.BluetoothDevice;
import org.twuni.homestreamer.view.adapter.ListAdapter;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class BluetoothClientActivity extends Activity {

	protected final List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
	protected ListAdapter<BluetoothDevice> adapter;
	protected Thread clientThread;

	@Override
	protected void onStop() {
		super.onStop();
		if( clientThread != null ) {
			clientThread.interrupt();
		}
	}

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

					clientThread = BluetoothUtils.connect( view.getContext(), device.getDevice(), new OnConnectedListener() {

						@Override
						public void onConnected( BluetoothSocket socket ) {
							serverThread.interrupt();
							try {
								DataOutputStream dos = new DataOutputStream( socket.getOutputStream() );
								dos.writeUTF( "http://twuni.org/casino.mp3" );
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

		} );

		setContentView( view );

	}

	@Override
	protected void onDeviceReceived( BluetoothDevice device ) {
		if( !devices.contains( device ) ) {
			devices.add( device );
			adapter.notifyDataSetChanged();
		}
	}

}
