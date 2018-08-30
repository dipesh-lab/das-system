package org.das.worker.config;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public final class AppConfiguration {
	
	private static final Logger LOG = Logger.getLogger(AppConfiguration.class);

	public static final AppConfiguration APP_COFIGURATION = new AppConfiguration();
		
	private Properties PROPERTIES = new Properties();
	
	public static AppConfiguration getInstance() {
		return APP_COFIGURATION;
	}
	
	private AppConfiguration() {
		init();
	}
	
	private void init() {
		try (InputStream inStream = getClass().getClassLoader().getResourceAsStream("configuration.properties")) {
			PROPERTIES.load(inStream);
		} catch(Exception e) {
			e.printStackTrace();
			LOG.error(e);
		}
	}
	
	public final String getProperty(final String key) {
		return PROPERTIES.getProperty(key);
	}	
	
}