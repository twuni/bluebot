package org.twuni.homestreamer.view;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

public class AudioView extends MediaPlayer implements OnPreparedListener, OnTouchListener, MediaPlayerControl {

	private final MediaController controller;
	private final List<OnPreparedListener> onPreparedListeners = new ArrayList<OnPreparedListener>();
	private boolean started;

	public AudioView( Context context ) {
		this( new MediaController( context ) );
	}

	public AudioView( MediaController controller ) {
		this.controller = controller;
	}

	public void addOnPreparedListener( OnPreparedListener onPreparedListener ) {
		onPreparedListeners.add( onPreparedListener );
	}

	public void removeOnPreparedListener( OnPreparedListener onPreparedListener ) {
		onPreparedListeners.remove( onPreparedListener );
	}

	public void clearOnPreparedListeners() {
		onPreparedListeners.clear();
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public int getBufferPercentage() {
		return 0;
	}

	@Override
	public void onPrepared( MediaPlayer player ) {
		for( int i = 0; i < onPreparedListeners.size(); i++ ) {
			OnPreparedListener onPreparedListener = onPreparedListeners.get( i );
			if( onPreparedListener != null ) {
				onPreparedListener.onPrepared( player );
			}
		}
	}

	public void setAnchorView( View view ) {
		controller.setAnchorView( view );
		controller.setMediaPlayer( this );
		view.setOnTouchListener( this );
	}

	@TargetApi( Build.VERSION_CODES.ICE_CREAM_SANDWICH )
	public void start( Context context, Uri uri, Map<String, String> headers ) throws IOException {
		setDataSource( context, uri, headers );
		prepareAndStart();
	}

	public void start( FileDescriptor fileDescriptor, long offset, long length ) throws IOException {
		setDataSource( fileDescriptor, offset, length );
		prepareAndStart();
	}

	public void start( String path ) throws IOException {
		setDataSource( path );
		prepareAndStart();
	}

	public void start( FileDescriptor fileDescriptor ) throws IOException {
		setDataSource( fileDescriptor );
		prepareAndStart();
	}

	public void start( Context context, Uri uri ) throws IOException {
		Log.d( getClass().getSimpleName(), String.format( "#start(%s,\"%s\")", context.getClass().getSimpleName(), uri ) );
		setDataSource( context, uri );
		prepareAndStart();
	}

	private void prepareAndStart() throws IOException {
		prepare();
		start();
		started = true;
	}

	public void finish() {
		controller.hide();
		if( started ) {
			stop();
			started = false;
		}
		release();
	}

	public void showControls() {
		showControls( 0 );
	}
	
	public void showControls( int timeout ) {
		controller.setEnabled( true );
		controller.show( timeout );
	}

	@Override
	public boolean onTouch( View view, MotionEvent event ) {
		showControls( 3000 );
		return false;
	}

}
