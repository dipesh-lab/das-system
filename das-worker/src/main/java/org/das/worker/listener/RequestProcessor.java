package org.das.worker.listener;

public interface RequestProcessor {

	void onRequest(final String data);
	
}