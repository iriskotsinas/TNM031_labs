package server;
// An example class that uses the secure server socket class

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.StringTokenizer;


public class SecureAdditionServer {
	private int port;
	// This is not a reserved port number
	static final int DEFAULT_PORT = 8189;
	static final String KEYSTORE = "src/server/LIUkeystore.ks";
	static final String TRUSTSTORE = "src/server/LIUtruststore.ks";
	static final String KEYSTOREPASS = "123456";
	static final String TRUSTSTOREPASS = "abcdef";
	
	/** Constructor
	 * @param port The port where the server
	 *    will listen for requests
	 */
	SecureAdditionServer( int port ) {
		this.port = port;
	}
	
	/** The method that does the work for the class */
	public void run() {
		try {
			KeyStore ks = KeyStore.getInstance( "JCEKS" );
			ks.load( new FileInputStream( KEYSTORE ), KEYSTOREPASS.toCharArray() );
			
			KeyStore ts = KeyStore.getInstance( "JCEKS" );
			ts.load( new FileInputStream( TRUSTSTORE ), TRUSTSTOREPASS.toCharArray() );
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
			kmf.init( ks, KEYSTOREPASS.toCharArray() );
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
			tmf.init( ts );
			
			SSLContext sslContext = SSLContext.getInstance( "TLS" );
			sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
			SSLServerSocketFactory sslServerFactory = sslContext.getServerSocketFactory();
			SSLServerSocket sss = (SSLServerSocket) sslServerFactory.createServerSocket( port );
			sss.setNeedClientAuth(true);
			sss.setEnabledCipherSuites( sss.getSupportedCipherSuites() );
			
			System.out.println("\n>>>> SecureAdditionServer: active ");
			SSLSocket incoming = (SSLSocket)sss.accept();

			BufferedReader in = new BufferedReader( new InputStreamReader( incoming.getInputStream() ) );
			PrintWriter out = new PrintWriter( incoming.getOutputStream(), true );			
			
			String str;
			while ( !(str = in.readLine()).equals("hejhej") ) { // FIXA
				System.out.println(str);
				
				if (str.equals("download")) {
					BufferedReader r = new BufferedReader(new FileReader("src/server/Files/" + in.readLine()));
					String line;
					while ((line = r.readLine()) != null) {
						out.println(line);
					}
					out.println("downloaded");
					
				} else if (str.equals("upload")) {
					FileWriter w = new FileWriter("src/server/Files/" + in.readLine());
					String inputString;
					
					while (!(inputString = in.readLine()).equals("|")) {
						w.write(inputString);
					}
					w.flush();
					
				} else if (str.equals("delete")) {
                    File deletedFile = new File("src/server/Files/" + in.readLine());
                    if (deletedFile.exists()) {
                    		deletedFile.delete();
                    		out.println("deleted");
                    } else {
                    		out.println("file not found");
                    }
				}
			}
			incoming.close();
		}
		catch( Exception x ) {
			System.out.println( x );
			x.printStackTrace();
		}
	}
	
	//DOWNLOADING FILE
	//UPLOADING FILE
	//DELETING FILE
	
	
	/** The test method for the class
	 * @param args[0] Optional port number in place of
	 *        the default
	 */
	public static void main( String[] args ) {
		int port = DEFAULT_PORT;
		if (args.length > 0 ) {
			port = Integer.parseInt( args[0] );
		}
		SecureAdditionServer addServe = new SecureAdditionServer( port );
		addServe.run();
	}
}

