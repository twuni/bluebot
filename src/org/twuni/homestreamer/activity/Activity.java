package org.twuni.homestreamer.activity;

import android.widget.Toast;

abstract class Activity extends android.app.Activity {

	protected void report( final Throwable throwable ) {
		runOnUiThread( new Runnable() {

			@Override
			public void run() {
				Toast.makeText( getBaseContext(), String.format( "[%s] %s", throwable.getClass().getSimpleName(), throwable.getMessage() ), Toast.LENGTH_SHORT ).show();
			}

		} );
	}

}
