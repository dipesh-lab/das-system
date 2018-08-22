package org.das.network.listener;

public interface TransportListener {

	public void init(int port);
	
	public void startConnector();
	
	public void setMessageListener(NetworkMessageListener listener);
	
	public void stopListener();
	
}