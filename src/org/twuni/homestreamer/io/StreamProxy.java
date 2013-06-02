package org.twuni.homestreamer.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.ParseException;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.DefaultClientConnection;
import org.apache.http.impl.conn.DefaultClientConnectionOperator;
import org.apache.http.impl.conn.DefaultResponseParser;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.ParserCursor;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;

public class StreamProxy implements Runnable {

	private int port = 0;

	public int getPort() {
		return port;
	}

	private boolean isRunning;
	private ServerSocket socket;
	private Thread thread;

	private void init() {
		try {
			socket = new ServerSocket( port, 0, InetAddress.getByAddress( new byte [] { 127, 0, 0, 1 } ) );
			socket.setSoTimeout( 5000 );
			port = socket.getLocalPort();
			// Do nothing
		} catch( UnknownHostException e ) {
			// Do nothing
		} catch( IOException e ) {
			// Do nothing
		}
	}

	public void start() {

		if( socket == null ) {
			init();
		}

		thread = new Thread( this );
		thread.start();
		isRunning = true;
	}

	public void stop() {
		isRunning = false;

		if( thread == null ) {
			return;
		}

		thread.interrupt();
		try {
			thread.join( 5000 );
		} catch( InterruptedException e ) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while( isRunning ) {
			try {
				Socket client = socket.accept();
				if( client == null ) {
					continue;
				}
				HttpRequest request = readRequest( client );
				processRequest( request, client );
			} catch( SocketTimeoutException e ) {
				// Do nothing
			} catch( IOException e ) {
				// Do nothing
			}
		}
	}

	private HttpRequest readRequest( Socket client ) {

		HttpRequest request = null;
		InputStream is;
		String firstLine;

		try {
			is = client.getInputStream();
			BufferedReader reader = new BufferedReader( new InputStreamReader( is ), 8192 );
			firstLine = reader.readLine();
		} catch( IOException e ) {
			return request;
		}

		if( firstLine == null ) {
			return request;
		}

		StringTokenizer st = new StringTokenizer( firstLine );
		String method = st.nextToken();
		String uri = st.nextToken();
		String realUri = uri.substring( 1 );
		request = new BasicHttpRequest( method, realUri );

		return request;

	}

	private HttpResponse download( String url ) {

		DefaultHttpClient seed = new DefaultHttpClient();
		SchemeRegistry registry = new SchemeRegistry();

		registry.register( new Scheme( "http", PlainSocketFactory.getSocketFactory(), 80 ) );

		SingleClientConnManager mgr = new MyClientConnManager( seed.getParams(), registry );
		DefaultHttpClient http = new DefaultHttpClient( mgr, seed.getParams() );
		HttpGet method = new HttpGet( url );
		HttpResponse response = null;

		try {
			response = http.execute( method );
		} catch( ClientProtocolException e ) {
			// Do nothing
		} catch( IOException e ) {
			// Do nothing
		}

		return response;

	}

	private void processRequest( HttpRequest request, Socket client ) throws IllegalStateException, IOException {

		if( request == null ) {
			return;
		}

		String url = request.getRequestLine().getUri();
		HttpResponse realResponse = download( url );

		if( realResponse == null ) {
			return;
		}

		InputStream data = realResponse.getEntity().getContent();
		StatusLine line = realResponse.getStatusLine();
		HttpResponse response = new BasicHttpResponse( line );
		response.setHeaders( realResponse.getAllHeaders() );

		StringBuilder httpString = new StringBuilder();
		httpString.append( response.getStatusLine().toString() );

		httpString.append( "\n" );
		for( Header h : response.getAllHeaders() ) {
			httpString.append( h.getName() ).append( ": " ).append( h.getValue() ).append( "\n" );
		}
		httpString.append( "\n" );

		try {
			byte [] buffer = httpString.toString().getBytes();
			int readBytes;

			client.getOutputStream().write( buffer, 0, buffer.length );

			byte [] buff = new byte [1024 * 50];
			while( isRunning && ( readBytes = data.read( buff, 0, buff.length ) ) != -1 ) {
				client.getOutputStream().write( buff, 0, readBytes );
			}
		} catch( Exception e ) {
			// Do nothing
		} finally {
			if( data != null ) {
				data.close();
			}
			client.close();
		}

	}

	private class IcyLineParser extends BasicLineParser {

		private static final String ICY_PROTOCOL_NAME = "ICY";

		private IcyLineParser() {
			super();
		}

		@Override
		public boolean hasProtocolVersion( CharArrayBuffer buffer, ParserCursor cursor ) {

			boolean superFound = super.hasProtocolVersion( buffer, cursor );

			if( superFound ) {
				return true;
			}

			int index = cursor.getPos();

			final int protolength = ICY_PROTOCOL_NAME.length();

			if( buffer.length() < protolength ) {
				return false;
			}

			if( index < 0 ) {
				index = buffer.length() - protolength;
			} else if( index == 0 ) {
				while( ( index < buffer.length() ) && HTTP.isWhitespace( buffer.charAt( index ) ) ) {
					index++;
				}
			}

			return index + protolength <= buffer.length() && buffer.substring( index, index + protolength ).equals( ICY_PROTOCOL_NAME );

		}

		@Override
		public ProtocolVersion parseProtocolVersion( CharArrayBuffer buffer, ParserCursor cursor ) throws ParseException {

			if( buffer == null ) {
				throw new IllegalArgumentException( "Char array buffer may not be null" );
			}
			if( cursor == null ) {
				throw new IllegalArgumentException( "Parser cursor may not be null" );
			}

			final int protolength = ICY_PROTOCOL_NAME.length();

			int indexFrom = cursor.getPos();
			int indexTo = cursor.getUpperBound();

			skipWhitespace( buffer, cursor );

			int i = cursor.getPos();

			if( i + protolength + 4 > indexTo ) {
				throw new ParseException( "Not a valid protocol version: " + buffer.substring( indexFrom, indexTo ) );
			}

			if( !buffer.substring( i, i + protolength ).equals( ICY_PROTOCOL_NAME ) ) {
				return super.parseProtocolVersion( buffer, cursor );
			}

			cursor.updatePos( i + protolength );

			return createProtocolVersion( 1, 0 );
		}

		@Override
		public StatusLine parseStatusLine( CharArrayBuffer buffer, ParserCursor cursor ) throws ParseException {
			return super.parseStatusLine( buffer, cursor );
		}
	}

	class MyClientConnection extends DefaultClientConnection {

		@Override
		protected HttpMessageParser createResponseParser( final SessionInputBuffer buffer, final HttpResponseFactory responseFactory, final HttpParams params ) {
			return new DefaultResponseParser( buffer, new IcyLineParser(), responseFactory, params );
		}

	}

	class MyClientConnectionOperator extends DefaultClientConnectionOperator {

		public MyClientConnectionOperator( final SchemeRegistry sr ) {
			super( sr );
		}

		@Override
		public OperatedClientConnection createConnection() {
			return new MyClientConnection();
		}

	}

	class MyClientConnManager extends SingleClientConnManager {

		private MyClientConnManager( HttpParams params, SchemeRegistry schreg ) {
			super( params, schreg );
		}

		@Override
		protected ClientConnectionOperator createConnectionOperator( final SchemeRegistry sr ) {
			return new MyClientConnectionOperator( sr );
		}

	}

}