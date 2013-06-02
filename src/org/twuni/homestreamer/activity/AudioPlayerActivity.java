package org.twuni.homestreamer.activity;

import java.io.IOException;

import org.twuni.homestreamer.Extra;
import org.twuni.homestreamer.R;
import org.twuni.homestreamer.view.AudioView;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;

public class AudioPlayerActivity extends Activity implements OnPreparedListener {

	private AudioView audio;
	private int currentPosition;
	private Uri nowPlaying;

	private void play( Uri uri ) {

		if( uri == null ) {
			return;
		}

		if( uri.equals( nowPlaying ) ) {
			return;
		}

		if( audio != null ) {
			audio.finish();
			audio = null;
		}

		audio = new AudioView( this );
		audio.setAnchorView( findViewById( R.id.content ) );
		audio.addOnPreparedListener( this );

		try {
			audio.start( this, uri );
			nowPlaying = uri;
		} catch( IOException exception ) {
			report( exception );
		}

	}

	private void handleIntent( Intent intent ) {
		if( intent == null ) {
			return;
		}
		play( intent.getData() );
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
		handleIntent( getIntent() );
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		audio.finish();
		audio = null;
	}

	@Override
	protected void onSaveInstanceState( Bundle outState ) {
		super.onSaveInstanceState( outState );
		if( audio != null ) {
			currentPosition = audio.getCurrentPosition();
		}
		Extra.POSITION.put( outState, currentPosition );
	}

	@Override
	protected void onRestoreInstanceState( Bundle savedInstanceState ) {
		super.onRestoreInstanceState( savedInstanceState );
		currentPosition = Extra.POSITION.getInt( savedInstanceState );
	}

	@Override
	public void onPrepared( MediaPlayer player ) {
		player.seekTo( currentPosition );
		audio.showControls();
	}

}
