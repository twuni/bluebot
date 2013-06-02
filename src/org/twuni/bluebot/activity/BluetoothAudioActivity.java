package org.twuni.bluebot.activity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.twuni.bluebot.Action;
import org.twuni.bluebot.R;
import org.twuni.bluebot.bluetooth.BluetoothDevice;
import org.twuni.bluebot.bluetooth.BluetoothUtils;
import org.twuni.bluebot.bluetooth.listener.OnConnectedListener;
import org.twuni.bluebot.view.adapter.ListAdapter;

import android.bluetooth.BluetoothSocket;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
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
	protected boolean recording;

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

								DataOutputStream out = new DataOutputStream( socket.getOutputStream() );

								int sampleRate = 44100;
								int channels = AudioFormat.CHANNEL_OUT_STEREO;
								int encoding = AudioFormat.ENCODING_PCM_16BIT;
								int bufferSize = AudioTrack.getMinBufferSize( sampleRate, channels, encoding );
								byte [] buffer = new byte [bufferSize];

								AudioRecord recorder = new AudioRecord( MediaRecorder.AudioSource.MIC, sampleRate, channels, encoding, bufferSize );

								Action action = Action.STREAM;

								out.writeUTF( action.name() );
								out.writeInt( sampleRate );
								out.writeInt( channels );
								out.writeInt( encoding );

								recorder.startRecording();
								recording = true;
								for( int size = recorder.read( buffer, 0, buffer.length ); recording && size > 0; size = recorder.read( buffer, 0, size ) ) {
									out.write( buffer, 0, size );
								}
								recorder.stop();
								recorder.release();

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
	protected void onActionReceived( Action action, InputStream in ) {
		super.onActionReceived( action, in );
		DataInputStream data = new DataInputStream( in );
		switch( action ) {
			case PLAY:
				try {
					play( Uri.parse( data.readUTF() ) );
				} catch( IOException exception ) {
					report( exception );
				}
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

	@Override
	protected void onPause() {
		super.onDestroy();
		recording = false;
	}

}
