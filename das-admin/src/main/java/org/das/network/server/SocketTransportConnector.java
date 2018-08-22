package org.das.network.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.das.network.listener.NetworkMessageListener;
import org.das.network.listener.TransportListener;

public class SocketTransportConnector extends Thread implements TransportListener {
	
	private final static Logger LOG = Logger.getLogger(SocketTransportConnector.class);
	
	private boolean listen = false;
	
	private ServerSocketChannel serverSocket = null;
	
	private Selector selector = null;
	
	private Map<Integer, RequestProcessor> processors = new ConcurrentHashMap<>(3, 1.0f);
	
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
	                    i.remove();
	                    if(key.isAcceptable()) {
	                    	RequestProcessor processor = processors.get(SelectionKey.OP_ACCEPT);
	                    	processor.handleRequest(key);
	                    }
	                    if(key.isReadable()) {
	                    	RequestProcessor processor = processors.get(SelectionKey.OP_READ);
	                    	processor.handleRequest(key);
	                    }
	                    if(key.isWritable()) {
	                    	RequestProcessor processor = processors.get(SelectionKey.OP_WRITE);
	                    	processor.handleRequest(key);
	                    }
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
			serverSocket.socket().bind(inetSocketAddress);
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);
			
			processors.put(SelectionKey.OP_ACCEPT, new RequestAcceptor(selector));
			processors.put(SelectionKey.OP_READ, new RequestReader(selector));
			processors.put(SelectionKey.OP_WRITE, new RequestWriter(selector));
			listen = true;
			LOG.debug("Admin Server initialised sucessfully on port " + serverPort);
		} catch(IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public void setMessageListener(NetworkMessageListener listener) {}

	@Override
	public void stopListener() {
		listen = false;
		try {
			serverSocket.close();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

}