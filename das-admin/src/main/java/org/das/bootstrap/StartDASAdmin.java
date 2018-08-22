package org.das.bootstrap;

import org.apache.log4j.Logger;
import org.das.config.AppConfiguration;
import org.das.network.listener.TransportListener;
import org.das.network.server.SocketTransportConnector;

public class StartDASAdmin {
	
	private static final Logger LOG = Logger.getLogger(StartDASAdmin.class);

	public static void main(String[] arg) {
		bootstrap();
	}
	
	private static void bootstrap() {
		AppConfiguration configuration = AppConfiguration.getInstance();
		
		final int adminPort = Integer.parseInt(configuration.getProperty("admin.socket.port"));
		LOG.debug("Reader Server Port " + adminPort);
		TransportListener socketListener = new SocketTransportConnector();
		socketListener.init(adminPort);
		socketListener.startConnector();
	}

}