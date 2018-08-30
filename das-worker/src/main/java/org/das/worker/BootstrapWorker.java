package org.das.worker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.das.worker.config.AppConfiguration;
import org.das.worker.listener.RequestProcessor;
import org.das.worker.network.connector.NetworkChannelConnector;
import org.das.worker.processor.IncomingRequestProcessor;
import org.das.worker.processor.RequestProcessorImpl;
import org.das.worker.utils.CommonUtils;

public class BootstrapWorker {
	
	private static final Logger LOG = Logger.getLogger(BootstrapWorker.class);

	public static void main(String[] arg) {
		bootstrap();
	}
	
	private static void bootstrap() {
		AppConfiguration configuration = AppConfiguration.getInstance();
		final String adminHost = configuration.getProperty("admin.server.host");
		final String adminPort = configuration.getProperty("admin.server.port");
		
		if(CommonUtils.isEmpty(adminHost) || CommonUtils.isEmpty(adminPort)) return;
		
		final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<String>();
		IncomingRequestProcessor requestProcessor = new IncomingRequestProcessor(messageQueue);
		RequestProcessor processor = new RequestProcessorImpl();
		requestProcessor.setProcessor(processor);
		requestProcessor.start();
		
		NetworkChannelConnector connector = 
				new NetworkChannelConnector("Executor 1", adminHost, Integer.parseInt(adminPort), messageQueue);
		
		Thread thread = new Thread(connector);
		thread.start();
		LOG.debug("Analytic Client is running and listening.");
	}
}