package org.twuni.bluebot.io;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

	private static final byte [] BUFFER = new byte [64 * 1024];

	public static void pipe( InputStream in, OutputStream out ) throws IOException {
		pipe( in, out, BUFFER );
	}

	public static void pipe( InputStream in, OutputStream out, byte [] buffer ) throws IOException {
		for( int size = in.read( buffer ); size > 0; size = in.read( buffer, 0, size ) ) {
			out.write( buffer, 0, size );
		}
	}

	public static void close( Closeable... closeables ) {
		for( int i = 0; i < closeables.length; i++ ) {
			Closeable closeable = (Closeable) closeables[i];
			if( closeable != null ) {
				try {
					closeable.close();
				} catch( IOException ignore ) {
					// Ignore.
				}
			}
		}
	}

	public static void flush( Flushable... flushables ) {
		for( int i = 0; i < flushables.length; i++ ) {
			Flushable flushable = (Flushable) flushables[i];
			if( flushable != null ) {
				try {
					flushable.flush();
				} catch( IOException ignore ) {
					// Ignore.
				}
			}
		}
	}

}
