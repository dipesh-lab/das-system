package org.das.network.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.das.message.executor.IncomingRequestExecutor;
import org.das.network.listener.NetworkMessageListener;
import org.das.network.listener.TransportListener;

public class SocketTransportConnector extends Thread implements TransportListener {
	
	private final static Logger LOG = Logger.getLogger(SocketTransportConnector.class);
	
	private boolean listen = false;
	
	private ServerSocketChannel serverSocket = null;
	
	private Selector selector = null;
	
	private IncomingRequestExecutor requestExecutor = null;
	
	public SocketTransportConnector() {}
	
	@Override
	public void startConnector() {
		super.start();
	}

	@Override
	public void run() {
		LOG.debug("Admin Server listening started");
		while(listen) {
			try {
				int count = selector.select();
	            if(count > 0) {
	                for(Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();) {
	                    SelectionKey key = i.next();
	                    if(key.isValid()) {
	                    	if(key.isAcceptable()) {
		                    	try {
		                			ServerSocketChannel serverSocketChannel = 
		                					(ServerSocketChannel) key.channel();
		                			SocketChannel channel = serverSocketChannel.accept();
		                			if(Objects.nonNull(channel)) {
		                				channel.configureBlocking(false);
		                				requestExecutor.process(key, channel);
		                			}
		                		}catch(IOException ioe) {
		                			LOG.error(ioe.getMessage(), ioe);
		                		}
		                    }
	                    }
	                    i.remove();
	                }
	            }
			}catch(Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}
	
	@Override
	public void init(int serverPort) {
		try {
			selector = Selector.open();
			serverSocket = ServerSocketChannel.open();
			serverSocket.configureBlocking(false);
			InetSocketAddress inetSocketAddress = new InetSocketAddress(serverPort);
			serverSocket.bind(inetSocketAddress, 50000);
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);
			serverSocket.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
			
			requestExecutor = new IncomingRequestExecutor();
			listen = true;
			LOG.debug("Admin Server initialised sucessfully on port " + serverPort);
		} catch(IOException e) {
			LOG.error(e.getMessage(), e);
			stopListener();
		}
	}

	@Override
	public void setMessageListener(NetworkMessageListener listener) {}

	@Override
	public void stopListener() {
		listen = false;
		requestExecutor.shutdown();
		try {
			if(selector.isOpen()) selector.close();
			if(Objects.nonNull(serverSocket) && serverSocket.isOpen()) serverSocket.close();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

}