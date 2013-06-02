package org.twuni.homestreamer.view;

import org.twuni.homestreamer.R;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DeviceView extends LinearLayout {

	public DeviceView( Context context ) {
		super( context );
	}

	public DeviceView( Context context, AttributeSet attrs ) {
		super( context, attrs );
	}

	@Override
	public void setTag( Object tag ) {
		super.setTag( tag );
		if( tag instanceof BluetoothDevice ) {
			setDevice( (BluetoothDevice) tag );
		}
	}

	private void setText( int viewResourceID, CharSequence text ) {
		TextView view = (TextView) findViewById( viewResourceID );
		if( view != null ) {
			view.setText( text );
		}
	}

	public void setDeviceName( CharSequence deviceName ) {
		setText( R.id.device_name, deviceName );
	}

	public void setDeviceAddress( CharSequence deviceAddress ) {
		setText( R.id.device_address, deviceAddress );
	}

	public void setDevice( BluetoothDevice device ) {
		if( device == null ) {
			return;
		}
		setDeviceName( device.getName() );
		setDeviceAddress( device.getAddress() );
	}

}