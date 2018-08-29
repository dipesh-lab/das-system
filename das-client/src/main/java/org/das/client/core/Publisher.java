package org.das.client.core;

import org.das.client.exception.ConnectionDataLinkException;

public interface Publisher {
	
	void createSession() throws ConnectionDataLinkException;
	
	String pushMessage(final String message) throws ConnectionDataLinkException;
	
	void close();

}