package org.twuni.bluebot.view;

import org.twuni.bluebot.R;
import org.twuni.bluebot.bluetooth.BluetoothDevice;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DeviceView extends RelativeLayout {

	public DeviceView( Context context ) {
		super( context );
	}

	public DeviceView( Context context, AttributeSet attributes ) {
		super( context, attributes );
	}

	public DeviceView( Context context, AttributeSet attributes, int defaultStyle ) {
		super( context, attributes, defaultStyle );
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
	
	public void setDeviceIcon( int drawableResourceID ) {
		ImageView view = (ImageView) findViewById( R.id.device_icon );
		view.setImageResource( drawableResourceID );
	}

	public void setDevice( BluetoothDevice device ) {
		if( device == null ) {
			return;
		}
		setDeviceName( device.getName() );
		setDeviceAddress( device.getAddress() );
		setDeviceIcon( device.getIconResourceID() );
	}

}
