package org.twuni.bluebot.activity;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.twuni.bluebot.Action;
import org.twuni.bluebot.R;
import org.twuni.bluebot.bluetooth.BluetoothDevice;
import org.twuni.bluebot.bluetooth.BluetoothUtils;
import org.twuni.bluebot.bluetooth.listener.OnConnectedListener;
import org.twuni.bluebot.view.adapter.ListAdapter;

import android.bluetooth.BluetoothSocket;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class BluetoothAudioActivity extends AudioPlayerActivity {

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

		ListView view = (ListView) findViewById( R.id.content );

		adapter = new ListAdapter<BluetoothDevice>( R.layout.device, devices );

		view.setAdapter( adapter );

		view.setOnItemClickListener( new OnItemClickListener() {

			@Override
			public void onItemClick( AdapterView<?> parentView, View view, int position, long itemID ) {

				BluetoothDevice device = devices.get( position );

				try {

					clientThread = BluetoothUtils.connect( view.getContext(), device.getDevice(), new OnConnectedListener() {

						@Override
						public void onConnected( BluetoothSocket socket ) {
							report( getString( R.string.connected_to, socket.getRemoteDevice().getName() ) );
							serverThread.interrupt();
							try {
								DataOutputStream dos = new DataOutputStream( socket.getOutputStream() );
								dos.writeUTF( Action.PLAY.name() );
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

	}

	@Override
	protected void onActionReceived( Action action, Object... args ) {
		super.onActionReceived( action, args );
		switch( action ) {
			case PLAY:
				String uri = (String) args[0];
				play( Uri.parse( uri ) );
				break;
			case PAUSE:
				audio.pause();
				break;
			case STOP:
				audio.stop();
				break;
			default:
				// Nothing to do.
				break;
		}
	}

	@Override
	protected void onDeviceReceived( BluetoothDevice device ) {
		if( !devices.contains( device ) ) {
			devices.add( device );
			adapter.notifyDataSetChanged();
		}
	}

}
