package com.zimbra.qa.selenium.staf;

import java.io.*;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.zimbra.qa.selenium.framework.util.HarnessException;
import com.zimbra.qa.selenium.framework.util.staf.*;

public class StafDevProperties {
    private static Logger logger = Logger.getLogger(StafDevProperties.class);
    
    private static final String ConfigPropertiesComments = "Auto Generated By STAF";
    
    protected Properties MyProperties = null;

	public StafDevProperties() {
		logger.info("new "+ StafDevProperties.class.getCanonicalName());
		
    	MyProperties = new Properties();

	}
	
	public void load(String host, String filename, String folder) throws IOException, HarnessException {
		logger.info("load(" + host + ", "+ filename +", "+ folder +")");

		
		// Get the remote file
    	String propertiesFilename = folder + "/" + System.currentTimeMillis() + "config.properties";
    	getRemoteFile(host, filename, propertiesFilename);

    	// Reset the properties
    	MyProperties = new Properties();

    	// Read the file
    	FileReader reader = null;
    	try {
    		
    		reader = new FileReader(propertiesFilename);
    		MyProperties.load(reader);

    	} finally {
    		if ( reader != null ) {
    			reader.close();
    			reader = null;
    		}
    	}

	}
	
	protected void getRemoteFile(String remotehost, String fromfile, String tofile) throws HarnessException {
		logger.info("getRemoteFile(" + remotehost + ", "+ fromfile +", "+ tofile +")");

		// Create the folder, if it doesn't exist
		File file = new File(tofile);
		file.getParentFile().mkdirs();
		
		// Command:
		// STAF <host> FS COPY FILE <fromfile> TOFILE <tofile> TOMACHINE <localhost>
		
		String localhost = getLocalMachineName();
		
		StafServiceFS staf = new StafServiceFS(remotehost);
		staf.execute(" COPY FILE "+ fromfile +" TOFILE "+ tofile +" TOMACHINE "+ localhost);
		
	}
	
	protected String getLocalMachineName() throws HarnessException {
		logger.info("getLocalMachineName()");

		// Command:
		// STAF local VAR GET SYSTEM VAR STAF/Config/Machine

		StafServiceVAR staf = new StafServiceVAR();
		staf.execute();
		return (staf.getStafResponse());
		
		
	}
	
    public String setProperty(String key, String value) {
		logger.info("setProperty(" + key + ", "+ value +")");

    	String original = (String)MyProperties.get(key);
    	MyProperties.setProperty(key, value);
    	return (original);
    }

    public String save(String foldername) throws IOException {
		logger.info("save(" + foldername +")");

		
    	String propertiesFilename = foldername + "/" + System.currentTimeMillis() + "config.properties";
    	
    	File f = new File(propertiesFilename);
    	f.getParentFile().mkdirs();
    	
    	FileWriter writer = null;
    	try {
    	
    		writer = new FileWriter(propertiesFilename);
        	MyProperties.store(writer, ConfigPropertiesComments);

    	} finally {
    		if ( writer != null ) {
    			writer.close();
    			writer = null;
    		}
    	}

    	return (propertiesFilename);
    }
	
	
}
