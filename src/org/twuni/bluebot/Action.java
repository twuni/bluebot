package org.twuni.bluebot;

public enum Action {

	PLAY( 1 ),
	PAUSE( 0 ),
	STOP( 0 ),
	ENQUEUE( 1 ),
	DEQUEUE( 1 ),
	SHUFFLE( 1 ),
	REPEAT( 1 );

	private final int argumentCount;

	private Action( int argumentCount ) {
		this.argumentCount = argumentCount;
	}

	public int getArgumentCount() {
		return argumentCount;
	}

}
