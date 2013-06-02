package org.twuni.homestreamer.io;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.twuni.homestreamer.R;
import org.twuni.homestreamer.io.BluetoothClientThread.OnConnectedListener;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

public class BluetoothUtils {

	public static UUID getServiceID( Context context ) {
		return UUID.fromString( context.getString( R.string.bluetooth_service_id ) );
	}

	public static String getServiceName( Context context ) {
		return context.getString( R.string.bluetooth_service_name );
	}

	public static boolean isBluetoothAvailable() {
		return getAdapter() != null;
	}

	public static boolean isBluetoothEnabled() {
		return isBluetoothEnabled( getAdapter() );
	}

	public static boolean isBluetoothEnabled( BluetoothAdapter adapter ) {
		return adapter != null && adapter.isEnabled();
	}

	public static void connect( Context context, BluetoothDevice device, OnConnectedListener onConnected ) throws IOException {
		new BluetoothClientThread( getServiceID( context ), device, onConnected ).start();
	}

	public static void listen( Context context, OnConnectedListener onConnected ) throws IOException {
		new BluetoothServerThread( getServiceName( context ), getServiceID( context ), onConnected ).start();
	}

	public static void enable( Activity activity, int requestID ) {
		BluetoothAdapter adapter = getAdapter();
		if( adapter != null && !adapter.isEnabled() ) {
			activity.startActivityForResult( new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE ), requestID );
		}
	}

	public static boolean isPaired( String deviceName, String deviceAddress ) {
		BluetoothAdapter adapter = getAdapter();
		if( !isBluetoothEnabled( adapter ) ) {
			return false;
		}
		Set<BluetoothDevice> devices = adapter.getBondedDevices();
		for( BluetoothDevice device : devices ) {
			if( deviceName.equals( device.getName() ) && deviceAddress.equals( device.getAddress() ) ) {
				return true;
			}
		}
		return false;
	}

	public static void makeDiscoverable( int seconds, Context context ) {
		context.startActivity( createMakeDiscoverableIntent( seconds ) );
	}

	public static void makeDiscoverable( int seconds, Activity activity, int requestID ) {
		activity.startActivityForResult( createMakeDiscoverableIntent( seconds ), requestID );
	}

	public static void discover() {
		BluetoothAdapter adapter = getAdapter();
		if( isBluetoothEnabled( adapter ) ) {
			adapter.startDiscovery();
		}
	}

	public static BluetoothAdapter getAdapter() {
		return BluetoothAdapter.getDefaultAdapter();
	}

	public static void cancelDiscovery() {
		BluetoothAdapter adapter = getAdapter();
		if( isBluetoothEnabled( adapter ) ) {
			adapter.cancelDiscovery();
		}
	}

	public static Intent createMakeDiscoverableIntent( int seconds ) {
		Intent intent = new Intent( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE );
		intent.putExtra( BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, seconds );
		return intent;
	}

}
