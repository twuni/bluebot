package org.twuni.bluebot.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.twuni.bluebot.R;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothClass;
import android.os.Build;
import android.os.ParcelUuid;

public class BluetoothDevice {

	private final android.bluetooth.BluetoothDevice device;

	public BluetoothDevice( android.bluetooth.BluetoothDevice device ) {
		this.device = device;
	}

	public String getName() {
		return device.getName();
	}

	public String getAddress() {
		return device.getAddress();
	}

	public android.bluetooth.BluetoothDevice getDevice() {
		return device;
	}

	@TargetApi( Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 )
	public List<UUID> getUUIDs() {
		List<UUID> ids = new ArrayList<UUID>();
		for( ParcelUuid id : device.getUuids() ) {
			ids.add( id.getUuid() );
		}
		return ids;
	}

	public int getIconResourceID() {
		switch( device.getBluetoothClass().getMajorDeviceClass() ) {
			case BluetoothClass.Device.Major.COMPUTER:
				return R.drawable.ic_hardware_computer;
			case BluetoothClass.Device.Major.NETWORKING:
				return R.drawable.ic_hardware_wireless;
			case BluetoothClass.Device.Major.AUDIO_VIDEO:
				return R.drawable.ic_hardware_video;
			case BluetoothClass.Device.Major.PHONE:
				return R.drawable.ic_hardware_phone;
		}
		return R.drawable.ic_hardware_unknown;
	}

	@Override
	public int hashCode() {
		return device.getAddress().hashCode();
	}

	@Override
	public boolean equals( Object other ) {
		return other != null && other.hashCode() == hashCode();
	}

}
