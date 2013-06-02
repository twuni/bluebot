package org.twuni.bluebot.bluetooth;

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

	@Override
	public int hashCode() {
		return device.getAddress().hashCode();
	}

	@Override
	public boolean equals( Object other ) {
		return other != null && other.hashCode() == hashCode();
	}

}
