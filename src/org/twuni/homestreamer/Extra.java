package org.twuni.homestreamer;

import android.net.Uri;
import android.os.Bundle;

public enum Extra {

	URI,
	POSITION;

	public void put( Bundle bundle, int value ) {
		if( bundle == null ) {
			return;
		}
		bundle.putInt( name(), value );
	}

	public int getInt( Bundle bundle ) {
		if( bundle == null ) {
			return 0;
		}
		return bundle.getInt( name() );
	}

	public void put( Bundle bundle, String value ) {
		if( bundle == null || value == null ) {
			return;
		}
		bundle.putString( name(), value );
	}

	public void put( Bundle bundle, Uri value ) {
		if( bundle == null || value == null ) {
			return;
		}
		put( bundle, value.toString() );
	}

	public String getString( Bundle bundle ) {
		if( bundle == null ) {
			return null;
		}
		return bundle.getString( name() );
	}

	public Uri getUri( Bundle bundle ) {
		String value = getString( bundle );
		return value == null ? null : Uri.parse( value );
	}

}
