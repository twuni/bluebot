package org.twuni.bluebot.activity;

import java.io.IOException;

import org.twuni.bluebot.Extra;
import org.twuni.bluebot.R;
import org.twuni.bluebot.view.AudioView;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;

abstract class AudioPlayerActivity extends Activity implements OnPreparedListener {

	protected AudioView audio;
	private int currentPosition;
	private Uri nowPlaying;

	protected void play( final Uri uri ) {

		if( uri == null ) {
			return;
		}

		if( audio != null && audio.isPlaying() && uri == nowPlaying ) {
			return;
		}

		runOnUiThread( new Runnable() {

			@Override
			public void run() {

				destroyAudio();

				audio = new AudioView( getActivity() );
				audio.setAnchorView( findViewById( R.id.content ) );
				audio.addOnPreparedListener( AudioPlayerActivity.this );

				try {
					audio.start( getActivity(), uri );
					nowPlaying = uri;
				} catch( IOException exception ) {
					report( exception );
				}

			}

		} );

	}

	private void handleIntent( Intent intent ) {

		if( intent == null ) {
			return;
		}

		Uri uri = intent.getData();

		if( uri == null ) {
			return;
		}

		if( uri.equals( nowPlaying ) ) {
			return;
		}

		play( uri );

	}

	@Override
	protected void onNewIntent( Intent intent ) {
		super.onNewIntent( intent );
		setIntent( intent );
		handleIntent( intent );
	}

	@Override
	protected void onCreate( Bundle savedInstanceState ) {

		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity );

		if( nowPlaying != null ) {
			play( nowPlaying );
		} else {
			handleIntent( getIntent() );
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		destroyAudio();
	}

	private void destroyAudio() {
		if( audio != null ) {
			audio.finish();
			audio = null;
		}
	}

	@Override
	protected void onSaveInstanceState( Bundle outState ) {
		super.onSaveInstanceState( outState );
		if( audio != null ) {
			currentPosition = audio.getCurrentPosition();
		}
		Extra.URI.put( outState, nowPlaying );
		Extra.POSITION.put( outState, currentPosition );
	}

	@Override
	protected void onRestoreInstanceState( Bundle savedInstanceState ) {
		super.onRestoreInstanceState( savedInstanceState );
		nowPlaying = Extra.URI.getUri( savedInstanceState );
		currentPosition = Extra.POSITION.getInt( savedInstanceState );
	}

	@Override
	public void onPrepared( MediaPlayer player ) {
		player.seekTo( currentPosition );
		audio.showControls();
	}

}
