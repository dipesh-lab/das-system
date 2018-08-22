package org.das.network.listener;

public interface NetworkMessageListener {
	
	Object messageReceived(final String data);

}