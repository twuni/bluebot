package org.twuni.homestreamer;

import android.os.Bundle;

public enum Extra {

	URI,
	POSITION;

	public void put( Bundle bundle, int value ) {
		bundle.putInt( name(), value );
	}

	public int getInt( Bundle bundle ) {
		return bundle.getInt( name() );
	}

}
